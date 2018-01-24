package gravity;

import static java.lang.Math.sqrt;

import java.util.Random;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Alien extends Craft {
	
	public final static Image ALIEN_SHIP_IMAGE = new Image("imgs/alienship.png");
	private final static Random random = new Random();
	
	private Point2D target;
	
	public Alien(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass)
	{
		super(centerX, centerY, radius, xVelocity, yVelocity, mass, null, null, null, null, null, ALIEN_SHIP_IMAGE, BLUE_SHIELD_IMAGE, Color.GREENYELLOW);
	}
	
	public void show(GraphicsContext gc, long timestamp, long elapsedTime)
	{
		super.show(gc,  timestamp, elapsedTime);
		gc.strokeLine(getCenterX(), getCenterY(), getCenterX() + target.getX(), getCenterY() + target.getY());
	}
	
	
	public FlyingObject react(long timestamp, ObservableList<Craft> crafts)
	{
		FlyingObject projectile = null;
		int targetIndex = 0;
		double nearest = Double.MAX_VALUE;
		double[] distance = new double[crafts.size()];
		double[] deltaX = new double[crafts.size()];
		double[] deltaY = new double[crafts.size()];
		int i = 0;
		for (Craft craft: crafts)
		{
			deltaX[i] = craft.getCenterX() - this.getCenterX();
			deltaY[i] = craft.getCenterY() - this.getCenterY();
			distance[i] = sqrt(deltaX[i] * deltaX[i] + deltaY[i] * deltaY[i]);
			if (distance[i] < nearest)
			{
				nearest = distance[i];
				targetIndex = i;
			}
			i++;
		}

		this.target = new Point2D(deltaX[targetIndex], deltaY[targetIndex]);
		Craft targetCraft = crafts.get(targetIndex);
		double aimX = deltaX[targetIndex] + targetCraft.getXVelocity();//*distance[targetIndex];
		double aimY = deltaY[targetIndex] + targetCraft.getYVelocity();//*distance[targetIndex];
		
		
		if (distance[targetIndex] < 1000)
		{
			if ((shieldIsUp == 0) && (timestamp - lastFired) / 1_000_000_000.0 > 1 / fireRate) {
				
			projectile = new Shot(getCenterX(), getCenterY(), aimX, aimY, 1, timestamp);
			Gravity.playSound("shot");
			lastFired = timestamp;
			}
		}
		
		return projectile;
	}
}
