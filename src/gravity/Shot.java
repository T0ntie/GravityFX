package gravity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Shot extends FlyingObject {

	private final long timeOfBirth;
	public final static double SECONDS_TO_LIVE = 5.0;
	private final static Image burstImg = new Image("imgs/shotexplode.png");
	private final Craft owner;
	private Color color;
	
	public boolean explode = false; 

	public Shot(double centerX, double centerY, double xVelocity, double yVelocity, double mass, long timeOfBirth, Craft owner, Color color) {

		super(centerX, centerY, 4, xVelocity, yVelocity, mass);
		this.timeOfBirth = timeOfBirth;
		this.owner = owner;
		this.color = color;
		
	}

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {

		double secondsLiving = (timestamp - timeOfBirth) / 1_000_000_000.0;

		if (secondsLiving < SECONDS_TO_LIVE) {
			gc.save();
			//gc.setStroke(Color.LIGHTBLUE);
			//gc.strokeLine(getCenterX(), getCenterY(), getCenterX() + 1, getCenterY() + 1);
			gc.setStroke(Color.ANTIQUEWHITE);
			gc.setFill(color);
			if (!explode) {
				gc.strokeOval(getCenterX()-getRadius(), getCenterY()-getRadius(), getRadius(), getRadius());
				gc.fillOval(getCenterX()-getRadius(), getCenterY()-getRadius(), getRadius(), getRadius());
		}else
				gc.drawImage(burstImg, getCenterX()-burstImg.getWidth()/2, getCenterY()-burstImg.getHeight()/2);
			gc.restore();
		} else {
			despawn();
		}
	}
	
	public Craft getOwner()
	{
		return this.owner;
	}
	

}
