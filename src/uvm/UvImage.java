package uvm;

import java.io.File;
import java.util.*;

import processing.core.*;

public class UvImage {

	public PImage image;
	public int quadId = -1;
	public int width, height;
	public String imageName, warpName;
	public ArrayList<Float> appliedAreas;
	public float brightness = -1;

	public UvImage(PImage image, String imageName) {
		
		this.image = image;
		this.imageName = imageName;
		this.width = this.image.width;
		this.height = this.image.height;
		this.appliedAreas = new ArrayList<Float>();
		this.warpName = changeExt(imageName, ".png");
		//Image Brightness
		this.brightness = calculateImageBrightness();
		
	}

	public String changeExt(String fileName, String ext) {
		
		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		return tokens[0] + ext;
	}

	public float aspectRation() {
		float r = image.width / (float) image.height;
		if (r < 1) r = -1 / r;
		return r;
	}

	public String toString() {

		return "{ name: " + imageName + ", width: " + width + ", height: " 
				+ height + (quadId > -1 ? (", quad: " + quadId) : " ") + "}";
	}

	public float area() {

		return width * height;
	}
	
	public static ArrayList<UvImage> loadFolder(PApplet p, String dir) {
		return fromFolder(p, dir, Integer.MAX_VALUE);
	}
	
	public float calculateImageBrightness() {
		
		String command =  UvMapper.CONVERT_CMD + UvMapper.IMAGE_DIR + this.imageName + UvMapper.IMAGE_BRIGHTNESS_ARGS;
//		System.out.println(this.imageName);
		this.brightness = Terminal.execToFloat(command);
	
		return this.brightness;
	}
	
	public static ArrayList<UvImage> fromFolder(PApplet p, String dir, int maxNum) {

		// Load images into UvImage objects
		String[] files = new File(dir).list();
		
		System.out.println("Found "+files.length+" image files");
		
		ArrayList<UvImage> ads = new ArrayList<UvImage>();

		for (int i = 0; i < files.length; i++) {
			//brightness trouble with gif
			if (files[i].matches(".*\\.(png|jpg|jpeg)")) {
				
				//System.out.println("Trying "+dir+"/"+files[i]);
				PImage pimg = p.loadImage(dir+"/"+files[i]);
				if (pimg == null || (p.width * p.height) < UvMapper.MIN_ALLOWED_IMG_AREA) {
					System.out.println("[WARN] "+ (pimg == null ? "Unable to load image: " 
							: "Image too small") +":"+ dir+"/"+files[i]);
					System.exit(1);
					continue;
				}
				ads.add(new UvImage(pimg, files[i]));
				
				UvMapper.showProgress(ads.size());
			}
			if (ads.size() >= maxNum)
				break;
		}

	  // sort the images
		sortImagesByArea (ads);
//		sortImagesByBrightness (ads);
//		
		System.out.println("\nLoaded "+ads.size()+" images");
		
		
		return ads;
	}
  
	public static void sortImagesByArea (ArrayList<UvImage> ads) {
		ads.sort(new Comparator<UvImage>() {

			public int compare(UvImage img1, UvImage img2) {
				
				if (img1.area() > img2.area())
					return -1;
				else if (img1.area() == img2.area())
					return 0;
				else
					return 1;
			}
		});
	}
	
	public static void sortImagesByBrightness (ArrayList<UvImage> ads) {
		ads.sort(new Comparator<UvImage>() {

			public int compare(UvImage img1, UvImage img2) {
				
				if (img1.brightness > img2.brightness)
					return -1;
				else if (img1.brightness == img2.brightness)
					return 0;
				else
					return 1;
			}
		});
	}
	
	// True if we have more applications remaining 
	// and we haven't accepted a quad with this area before
	public boolean acceptsQuad(Quad quad) {
		
		if (appliedAreas.size() >= UvMapper.MAX_USAGES_PER_IMG)
			return false;

		float check = Math.round(quad.area()); // round to get near misses
		boolean accepted = !appliedAreas.contains(check);
//		if (!accepted) System.out.println("Quad#"+quad.id+" rejected for "+imageName);
		return true;
	}
	

}
