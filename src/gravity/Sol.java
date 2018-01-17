package gravity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Sol extends GravityObject{
	
	final static Image solImg = new Image("sol.png");
	final static double RADIUS_RATIO = 70/30; //ratio between the outer circle of the image and the effective circle of the displayed sun
    private final double imageSide;

	
	public Sol(double centerX, double centerY, double radius, double mass)
	{
		super(centerX, centerY, radius, mass);
        imageSide = 2*radius*RADIUS_RATIO/Math.sqrt(2);
	}
	
	public void show(GraphicsContext gc, long elapsedTime) {
		gc.strokeOval(getCenterX()-radius, getCenterY()-radius, radius*2, radius*2);
		gc.drawImage(solImg, getCenterX()-imageSide/2, getCenterY()-imageSide/2, imageSide, imageSide);
	}
}
