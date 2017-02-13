package uvm;

import java.util.*;
import java.text.SimpleDateFormat; 

import processing.core.PApplet;
import processing.core.PImage;

public class UvMapper extends PApplet {

	public static boolean CROP_IMGS_TO_QUADS = false;
	public static boolean STROKE_QUAD_OUTLINES = false;
	public static boolean FILL_QUAD_BRIGHTNESS = false;
	public static boolean SCALE_QUADS_TO_DISPLAY = true;
	
	public static boolean CHANGE_ORIGIN_TO_BOTTOM_LEFT = true;
	public static boolean DRAW_QUAD_DEBUG_DATA = false; 
	public static boolean SHOW_PROGRESS_DOTS = true;
	
	public static int MAX_NUM_QUADS_TO_LOAD =100, MAX_NUM_IMGS_TO_LOAD = 120; 
	public static int MAX_USAGES_PER_IMG = 1, MIN_ALLOWED_IMG_AREA = 10;
//BerthaData20170205
	public static String DATA_FILE = "data/FitnessTestData.txt";
	public static String BRIGHTNESS_FILE = "data/FitnessTestData.txt";
	public static String UV_NAME = "BarthaTest.png";
	public static String IMAGE_DIR = "allImages/images/", OUTPUT_DIR = "warp/";
	
	public static String CONVERT_CMD = "/usr/local/bin/convert ";
	public static String CONVERT_ARGS = " -matte -mattecolor transparent -virtual-pixel transparent -interpolate Spline +distort BilinearForward ";
  public static String IMAGE_BRIGHTNESS_ARGS = " -colorspace Gray -format \"%[fx:image.mean]\" info:"; 
	List<Quad> quads;
	
	int setImageMax = 1;
	float setQuadMax = 1;
	
	public void settings() {

		size(500, 500);
	}

	public void setup() {
		

		//3300, 104.12598  Bertha

		List<UvImage> ads = UvImage.fromFolder(this, IMAGE_DIR, MAX_NUM_IMGS_TO_LOAD);
		setImageMax = setImageMax == 1 ? getMaxImageLength(ads) : setImageMax;

		quads = Quad.fromData(this, DATA_FILE);
//		readAllBrightness(this, BsRIGHTNESS_FILE);
		quads = Quad.sortByArea(quads);

		setQuadMax = setQuadMax == 1 ? getMaxQuadLength(quads) : setQuadMax; 
		
		int processed = assignImages(ads, quads);
//		System.out.println("\nProcessed " + processed + "/" + quads.size() + " Quads");
//		
		Quad.drawAll(quads);

	}
	
	public void draw() {

		
//		Quad.mouseOver(quads);
	}
	
	public void keyPressed() {
		if (key=='s') saveToFile();
	}
	
	
	int maxLImage = 0;

int getMaxImageLength(List<UvImage> ads) {

		int maxImageL = 0;
		UvImage maxLAd = new UvImage();

		for (int i = 0; i < ads.size(); i++) {
			if (ads.get(i).width > maxImageL || ads.get(i).height > maxImageL) {
				maxLAd = ads.get(i);
				maxImageL = ads.get(i).width > ads.get(i).height ? ads.get(i).width : ads.get(i).height;
			}
		}
    
//		System.out.println("\nMax Area " + ads.get(0).imageName + ":" + ads.get(0).area());
		System.out.println("\nMax Lenth " + maxLAd.imageName + ":"+ maxImageL);
	
		return maxImageL;
	}
	
	float getMaxQuadLength(List<Quad> quads) {

		Quad maxLQuad = new Quad();
		float maxQuadLength = 0;

		for (int i = 0; i < quads.size(); i++) {

			float w = quads.get(i).bounds[2];
			float h = quads.get(i).bounds[3];

			if (w > maxQuadLength || h > maxQuadLength) {
				maxLQuad = quads.get(i);
				maxQuadLength = w > h ? w : h;
			}

		}

//		System.out.println("\nMax Area " + quads.get(0).id + ":" + quads.get(0).area());
		System.out.println("\nMax Lenth " + maxLQuad.id + ":" + maxQuadLength);

		return maxQuadLength;
	}
	
	public void readAllBrightness(PApplet p, String dataFilePath) {
		
		String[] lines = p.loadStrings(dataFilePath);
		System.out.println("assign brightness from File");
		for (int i = 0; i < lines.length; i++) {
			String data = lines[i].split("]")[1];
			Quad quad = quads.get(i);	
			float b = Float.parseFloat(data);
			quad.setBrightness(b);
			System.out.print(".");
		}
		
	}
  
	double getFitness (UvImage image, Quad quad) {
		int imageUnit = setImageMax;
		float quadUnit =  setQuadMax;
		boolean fitnessLog = false;
		double fit = 0;
		// normalized points
		double [] i = new double[8], q = new double[8];
		if(fitnessLog) System.out.println("\n[FITNESS CALCULATION]");
		 
	  i[0] = i[1] = i[3] = i[6] = 0;
	  i[2] =  i[4] = image.width/(double)imageUnit;
	  i[5] = i[7] =  image.height/(double)imageUnit;
	  
	  if(fitnessLog) System.out.print("IMAGE:width " + image.width + ", height " +  image.height + "\nImage Points:");
	  
	  for (int j = 0; j < i.length; j++) {
	  	String split = j%2 == 0 ? ", ":";  ";
	  	if(fitnessLog) System.out.print(i[j] + split);
	  }
	  
	  for (int j = 0; j < q.length; j++) {
	  	q[j] = (double) quad.points[j];
	  	//central point switch to minx, miny
	  	if(j%2 == 0) q[j] -= quad.bounds[0];
	  	else q[j] -= quad.bounds[1];
	  	//scale
	  	q[j] = q[j]/quadUnit;
	  }
	  
	  if(fitnessLog) System.out.print("\nQuad:Bound width " + quad.bounds[2] + ", Bound height " +  quad.bounds[3] +"\nQuad points:");
	  
	  for (int j = 0; j < q.length; j++) {
	  	 
	  	 String split = j%2 == 0 ? ", ":";  ";
	  	 if(fitnessLog) System.out.print(q[j] + split);
	  }
	  
	  fit = dist(i[0],i[1],q[0],q[1]) + dist(i[2],i[3],q[2],q[3]) + dist(i[4],i[5],q[4],q[5]) + dist(i[6],i[7],q[6],q[7]);

//	  double[] dists = {dist(i[0],i[1],q[0],q[1]), dist(i[2],i[3],q[2],q[3]), dist(i[4],i[5],q[4],q[5]), dist(i[6],i[7],q[6],q[7])};
//	  
//	  for (int j = 0; j < dists.length; j++) {
//	    if (dists[j] > fit) {
//	        fit = dists[j];
//	    }
//	}
	  
	  if(fitnessLog) System.out.println("\nFit:" + fit);

		return fit;
	}
	
	double dist(double x1, double y1, double x2, double y2){
		return Math.hypot(x1-x2, y1-y2);
	}
	// Assigning best fiting ad image to each quad
	int assignImages(List<UvImage> images, List<Quad> quads) {

		int successes = 0;
		if (images != null) {

			for (int i = 0; i < quads.size(); i++) {
				
				Quad quad = quads.get(i);
				UvImage bestImg = getBestFit(images, quad);
				
				if (bestImg == null) {
					
					System.err.println("Quad#"+quad.id+" null image!\n");
					continue;
				}
				
				if (!quad.assignImage(bestImg)) {
					
					System.err.println("Quad#"+quad.id+" unable to assign image: "+bestImg.warpName+"/"+bestImg.warpName+"\n");
					
					if (++quad.tries < 3) // max 3 tries for any quad
						i--; // retry 
					else 
						System.err.println("Giving up on Quad#"+quad.id+"\n"+quad);
					
					continue;
				}
				
//				System.out.println("Quad#"+quad.id+" gets: "+bestImg);
				
				showProgress(++successes);
			}
		}

		return successes;
	}
	
//Assigning best fiting ad image to each quad
	int assignImagesForBrightness(List<UvImage> images, List<Quad> quads) {

		int successes = 0;
		if (images != null) {

			for (int i = 0; i < quads.size(); i++) {
				
				Quad quad = quads.get(i);
				UvImage bestImg = getBestFitForBrightness(images, quad, 1000);
				
				if (bestImg == null) {
					
					System.err.println("Quad#"+quad.id+" null image!\n");
					continue;
				}
				
				if (!quad.assignImage(bestImg)) {
					
					System.err.println("Quad#"+quad.id+" unable to assign image: "+bestImg.warpName+"/"+bestImg.warpName+"\n");
					
					if (++quad.tries < 3) // max 3 tries for any quad
						i--; // retry 
					else 
						System.err.println("Giving up on Quad#"+quad.id+"\n"+quad);
					
					continue;
				}
				
//				System.out.println("Quad#"+quad.id+" gets: "+bestImg);
				
				showProgress(++successes);
			}
		}

		return successes;
	}

	
	int assignImages2Step(List<UvImage> images, List<Quad> quads) {

		int successes = 0;
		List<Quad> quads1 = new ArrayList<Quad>(), quads2 = new ArrayList<Quad>();
		if (images != null) {
      
			//Step 0;
			//split the quads into two groups
			for (int i = 0; i < quads.size(); i++) {
				Quad quad = quads.get(i);				
        if (quad.brightness >= 0.6) quads2.add(quad);
        else quads1.add(quad);
			}
			
			quads1 = Quad.sortByArea(quads1);
			quads2 = Quad.sortByArea(quads2);
			System.out.println("Assign for Brightness" + quads1.size());
			//Step 1
			assignImagesForBrightness(images, quads1);
			System.out.println("Assign for the rest"+ quads2.size());
			//Step 2
			assignImages(images, quads2);
			//draw
			Quad.drawAll(quads1);
			Quad.drawAll(quads2);
			
			
		}

		return successes;
	}
	
	public static void showProgress(int x) {
		
		if (SHOW_PROGRESS_DOTS) {
			
			System.out.print(".");
			if (x % 80 == 79)
				System.out.println();
		}
	}

	UvImage getBestFit(List<UvImage> images, Quad quad) {

		UvImage bestImg = null;
		double bestDist = Float.MAX_VALUE;

		System.out.println("UvMapper.getBestFit()"+quad.id + " area="+quad.area());
		
		for (UvImage image : images) {

			if (image.acceptsQuad(quad)) {
				
				// System.out.println("I" + i + ": " + img.aspectRation());
				double cdistance;
//				cdistance = aspectRatioDistance(image, quad);
					cdistance = getFitness(image, quad);
//				 System.out.println(cdistance + " " + bestDist);
				if (cdistance < bestDist) {
					
					 System.out.println("NEW BEST!  "+cdistance);
					bestImg = image;
					bestDist = cdistance;
				}
			}
		}

		if (bestImg == null) {
			System.err.println("[WARN] No image found for Quad#" + quad.id);
		}
		
		return bestImg;
	}
	
	//Get Best Fit for Brightness, from last element
	UvImage getBestFitForBrightness(List<UvImage> images, Quad quad, int limit) {

		UvImage bestImg = null;
		float bestDist = Float.MAX_VALUE;

		//System.out.println("UvMapper.getBestFit()"+quad.id + " area="+quad.area());
		
		for (int i = images.size() - 1 ; i >= images.size() - limit ; i--) {
			
      UvImage image = images.get(i);
      
			if (image.acceptsQuad(quad)) {
				
				// System.out.println("I" + i + ": " + img.aspectRation());
				float cdistance;
				
				//Brightness
				cdistance = brightnessDistance(image, quad);
				
				// System.out.println(cdistance + " " + bestDist);
				if (cdistance < bestDist) {
					
					// System.out.println("NEW BEST!  "+cdistance);
					bestImg = image;
					bestDist = cdistance;
				}
			}
		}

		if (bestImg == null) {
			System.err.println("[WARN] No image found for Quad#" + quad.id);
		}
		
		return bestImg;
	}

	void saveToFile() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String fname  = (dateFormat.format(new Date()) + "_" + UV_NAME).replaceAll(" ", "_");
		save(fname);
		System.out.println("Wrote " + fname);
	}
	
	float aspectRatioDistance(UvImage img, Quad q) {
		return Math.abs(img.aspectRation() - q.aspectRatio());
	}
	
	float brightnessDistance(UvImage img, Quad q) {
		return Math.abs(img.brightness - q.brightness);
	}

	public static void main(String[] args) {

		PApplet.main(UvMapper.class.getName());
	}
}
