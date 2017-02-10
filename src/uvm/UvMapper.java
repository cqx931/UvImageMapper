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
	
	public static int MAX_NUM_QUADS_TO_LOAD = 100, MAX_NUM_IMGS_TO_LOAD = 2400; 
	public static int MAX_USAGES_PER_IMG = 1, MIN_ALLOWED_IMG_AREA = 10;

	public static String DATA_FILE = "data/BerthaData20170205.txt";
	public static String BRIGHTNESS_FILE = "data/BerthaBrightness.txt";
	public static String UV_NAME = "BarthaTest.png";
	public static String IMAGE_DIR = "allImages/", OUTPUT_DIR = "warp/";
	
	public static String CONVERT_CMD = "/usr/local/bin/convert ";
	public static String CONVERT_ARGS = " -matte -mattecolor transparent -virtual-pixel transparent -interpolate Spline +distort BilinearForward ";
  public static String IMAGE_BRIGHTNESS_ARGS = " -colorspace Gray -format \"%[fx:image.mean]\" info:"; 
	List<Quad> quads;
	
	public void settings() {

		size(14000, 14000);
	}

	public void setup() {

		List<UvImage> ads = UvImage.fromFolder(this, IMAGE_DIR, MAX_NUM_IMGS_TO_LOAD);
		quads = Quad.fromData(this, DATA_FILE);
		
		readAllBrightness(this, BRIGHTNESS_FILE);
		
		int processed = assignImages(ads, quads);
		System.out.println("\nProcessed " + processed + "/" + quads.size() + " Quads");
		
		Quad.drawAll(quads);

	}
	
	public void draw() {

		
//		Quad.mouseOver(quads);
	}
	
	public void keyPressed() {
		if (key=='s') saveToFile();
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
	
	public static void showProgress(int x) {
		
		if (SHOW_PROGRESS_DOTS) {
			
			System.out.print(".");
			if (x % 80 == 79)
				System.out.println();
		}
	}

	UvImage getBestFit(List<UvImage> images, Quad quad) {

		UvImage bestImg = null;
		float bestDist = Float.MAX_VALUE;

		//System.out.println("UvMapper.getBestFit()"+quad.id + " area="+quad.area());
		
		for (UvImage image : images) {

			if (image.acceptsQuad(quad)) {
				
				// System.out.println("I" + i + ": " + img.aspectRation());
				float cdistance = distance(image, quad);

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
	
	float distance(UvImage img, Quad q) {
		return Math.abs(img.aspectRation() - q.aspectRatio());
//		return Math.abs(img.brightness - q.brightness);
	}

	public static void main(String[] args) {

		PApplet.main(UvMapper.class.getName());
	}
}
