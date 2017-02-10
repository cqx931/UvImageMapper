package uvm;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;

public class BrightnessCalculator extends PApplet{
	public static String DATA_FILE = "data/BerthaData20170205.txt";
	public static String BRIGHTNESS_FILE = "data/texture.jpeg";
	
	public static String IMAGE_DIR = "allImages/";
	public static	int MAX_NUM_IMGS_TO_LOAD = 2400;


	List<Quad> quads;
	List<UvImage> ads;
	PImage img;
	ArrayList<Float> brightnessData  = new ArrayList<Float>();
	int[] count = {0,0,0,0,0,0,0,0,0,0};
	
	ArrayList<Float> imageBrightnessData  = new ArrayList<Float>();
	int[] imageCount = {0,0,0,0,0,0,0,0,0,0};
	
	public void settings() {

		size(1000, 1000);
		img = loadImage(BRIGHTNESS_FILE);
		img.loadPixels();
	}

	public void setup() {

		quads = Quad.fromData(this, DATA_FILE);
		
		//Brightness Image
		image(img, 0, 0);
		img.resize(this.width, this.height);
		image(img, 0, 0);
		img.loadPixels();
		
		Quad.drawAll(quads);
		
		float max = 0, min = 255;
	  for (int x = 0; x < img.width; x++) {
	    for (int y = 0; y < img.height; y++ ) {
	      // Calculate the 1D location from a 2D grid
	      int loc = x + y*img.width;
	      float b = brightness(img.pixels[loc]);
	      
	      if(b > max) max = b;
	      if(b < min) min = b;
	    }
	  }
	 
	  System.out.println("Brightness range of the image: " + min + "-" +  max);

	  
		calculateAllBrightness();
		
		System.out.println("Brightness range of Quad: ");
		
	  for (int i = 0; i < count.length; i++) {
	  	System.out.println("0." + i + "- 0." + (i+1) + ":" + count[i]);
	  }
	  
    ads = UvImage.fromFolder(this, IMAGE_DIR, MAX_NUM_IMGS_TO_LOAD);
    calculateImagesBrightness();
    
	System.out.println("Brightness range of Images: ");
		
	  for (int i = 0; i < imageCount.length; i++) {
	  	System.out.println("0." + i + "- 0." + (i+1) + ":" + imageCount[i]);
	  }
	  
	}
	
	public void calculateImagesBrightness(){
		for (int i = 0; i < ads.size(); i++) {
			UvImage img = ads.get(i);	
      float b = img.brightness;
      
      imageBrightnessData.add(b);
//      System.out.println(b);
      int range = (int)Math.floor(b*10);
      imageCount[range]++;
      
		}
		
	}
	public void draw() {
		
//		Quad.mouseOver(quads);
	}
	
	public void calculateAllBrightness() {
		for (int i = 0; i < quads.size(); i++) {
			Quad quad = quads.get(i);	
      float b = quad.getBrightness(img, this);	
      
      brightnessData.add(b);
//      System.out.println(b);
      int range = (int)Math.floor(b*10);
      count[range]++;
      
		}
	}
	
	public static void main(String[] args) {

		PApplet.main(BrightnessCalculator.class.getName());
	}
	
	

}
