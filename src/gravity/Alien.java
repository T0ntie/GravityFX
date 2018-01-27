package gravity;

import static java.lang.Math.sqrt;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Alien extends Craft {

	public final static Image ALIEN_SHIP_IMAGE = new Image("imgs/alienship.png");
	public final static double ALIEN_SHIELD_RADIUS = 70.0;

	public Alien(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass) {
		super(centerX, centerY, radius, xVelocity, yVelocity, mass, null, null, null, null, null, ALIEN_SHIP_IMAGE, 
				GREEN_SHIELD_IMAGE, ALIEN_SHIELD_RADIUS, Color.GREENYELLOW);
	}

	Craft nearestCraft = null;
	double nearestCraftDistance = Double.MAX_VALUE;
	Point2D nearestCraftHeading = null;

	Shot nearestShot = null;
	double nearestShotDistance = Double.MAX_VALUE;
	Point2D nearestShotHeading = null;

	public void detectNearestObjects(ObservableList<FlyingObject> flyingObjects) {
		nearestCraft = null;
		nearestCraftDistance = Double.MAX_VALUE;
		nearestCraftHeading = null;
		nearestShot = null;
		nearestShotDistance = Double.MAX_VALUE;
		nearestShotHeading = null;

		for (FlyingObject fo : flyingObjects) {
			double deltaX = fo.getCenterX() - this.getCenterX();
			double deltaY = fo.getCenterY() - this.getCenterY();
			double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
			if (fo instanceof Craft && !(fo instanceof Alien)) {
				Craft craft = (Craft) fo;
				if (distance < nearestCraftDistance) {
					nearestCraft = craft;
					nearestCraftDistance = distance;
					nearestCraftHeading = new Point2D(deltaX, deltaY);
				}
			} else if (fo instanceof Shot) {
				Shot shot = (Shot) fo;
				if (distance < nearestShotDistance) {
					nearestShot = shot;
					nearestShotDistance = distance;
					nearestShotHeading = new Point2D(deltaX, deltaY);
				}
			}
		}
	}

	public FlyingObject react(long timestamp, ObservableList<FlyingObject> flyingObjects) {
		FlyingObject projectile = null;
		detectNearestObjects(flyingObjects);
		if (nearestCraft != null)
			autoPilot(nearestCraftHeading.getX(), nearestCraftHeading.getY());

			if (nearestShotDistance < 200 && nearestShot.getOwner() != this) {
				shieldUp(timestamp);
			} else if (nearestCraftDistance < 1000) {
				if ((shieldIsUp == 0) && (timestamp - lastFired) / 1_000_000_000.0 > 1 / fireRate * 2) {

					projectile = new Shot(getCenterX(), getCenterY(), nearestCraftHeading.getX()+ nearestCraft.getXVelocity(),
							nearestCraftHeading.getY()+nearestCraft.getYVelocity(), 10, timestamp, this);
					Gravity.playSound("shot");
					lastFired = timestamp;
				}
			}
	

		return projectile;
	}

	void autoPilot(double posX, double posY) {
		double angle;
		if (posY == 0) {
			angle = 90;
		} else {
			angle = Math.toDegrees(Math.acos(-posY / Math.sqrt(posX * posX + posY * posY)));
		}

		Gravity.STATUS_MSG = "angle: " + angle + " x: " + posX;

		if (posX == 0) {
			if (posY <= 0)
				angle = 0;
			else
				angle = 180;
		} else {
			if (posX < 0)
				angle = -angle;
		}

		if (Math.abs(this.orientation - angle) > 5) {
			this.orientation += (angle > this.orientation) ? 5 : -5;
		} else if (nearestCraftDistance > 400) {
			this.addVelocity(Math.sin(Math.toRadians(angle)) * 10, -Math.cos(Math.toRadians(angle)) * 10);
			this.showthrust = 10;
			this.orientation = angle;
		}
	}
}
