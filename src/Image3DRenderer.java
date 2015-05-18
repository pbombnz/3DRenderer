import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.event.ChangeEvent;

/**
 * A simple example of how to extend the GUI class. The converBitmapToImage
 * method is particularly useful.
 */
public class Image3DRenderer extends GUI {
	
	Pipeline pipeline;

	public Image3DRenderer() {
		super();
		pipeline = new Pipeline();
	}

	@Override
	protected void onSliderChanged(ChangeEvent e) {
	}
	
	@Override
	protected void onLoad(File file) {
		pipeline.clear();
		resetGUI();

		try {
			BufferedReader rawData = new BufferedReader(new FileReader(file));
			String[] data = rawData.readLine().split(" ");
			Vector3D lightSource =  new Vector3D(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));		 
			pipeline.setLightSource(lightSource);
			//pipeline.addLightSource(lightSource);
			
		    
			String line;
		    while ((line = rawData.readLine()) != null) {
		    	if(line.length() > 1) {
			    	pipeline.addPolygon(loadPolygon(line));
		    	}
		    }
		    pipeline.runFirstTimeTransforms();
		    rawData.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private Polygon loadPolygon(String polygonLine) {
		String[] data = polygonLine.split(" ");
		return new Polygon(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]),
						   Float.parseFloat(data[3]), Float.parseFloat(data[4]), Float.parseFloat(data[5]),
						   Float.parseFloat(data[6]), Float.parseFloat(data[7]), Float.parseFloat(data[8]),
						   Integer.parseInt(data[9]), Integer.parseInt(data[10]), Integer.parseInt(data[11]));
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
    }

	@Override
	protected BufferedImage render() {
		pipeline.setAmbientLight(getAmbientLight());
		pipeline.setIntensity(getLightIntensity());
		pipeline.setDrawWireframe(getDrawWireframe());
		
		float thY = (float) ((getRotationY() * Math.PI)/180);
		float thX = (float) ((getRotationX() * Math.PI)/180);
		float thZ = (float) ((getRotationZ() * Math.PI)/180);
		
		pipeline.rotateY(thY);
		pipeline.rotateX(thX);
		pipeline.rotateZ(thZ);
		
		Color[][] bitmap = pipeline.render();
		return convertBitmapToImage(bitmap);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				try {
					image.setRGB(x, y, bitmap[x][y].getRGB());
				}
				catch (NullPointerException e) {
					System.out.println("x:"+x+" y:"+y+" bitmap[x][y]:"+bitmap[x][y]);
				}
			}
		}
		return image;
	}
	
	

	public static void main(String[] args) {
		new Image3DRenderer();
	}

}

// code for COMP261 assignments