package gravity;

import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

public class Craft extends FlyingObject {

	final static Image craftImg = new Image("craft.png");
	final static Image thrustImg = new Image("thrust.png");
	final static Image[] explosionImg = new Image[20];
	
	//find me
	
	static 
	{
		for (int i = 0; i < 20; i++) {
			explosionImg[i] = new Image("explosion" + i + ".png");
		}
	}

	long explosion = 0;
	int showthrust = 0;

	private DoubleProperty health = new SimpleDoubleProperty(50.0);
	private double healthbarX;
	private double healthbarY;

	private String keyLeft;
	private String keyRight;
	private String keyForward;
	private String keyFire;

	public Craft(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass,
			String keyLeft, String keyRight, String keyForward, String keyFire, double healthbarX, double healthbarY) {
		super(centerX, centerY, radius, xVelocity, yVelocity, mass);

		this.keyLeft = keyLeft;
		this.keyRight = keyRight;
		this.keyForward = keyForward;
		this.keyFire = keyFire;
		this.healthbarX = healthbarX;
		this.healthbarY = healthbarY;
	}

	public DoubleProperty getHealthProperty() {
		return health;
	}

//	Explosion explosion = null;

	public void show(GraphicsContext gc, long timestamp) {

//		if (explosion != null) {
//			explosion.show(gc, timestamp, getCenterX(), getCenterY());
//		} else {
		
			Image img;
			if (explosion > 0)
			{
				double secondsLiving = (timestamp - explosion) / 1_000_000_000.0;
				int index = (int) (secondsLiving/0.04);
				img = explosionImg[Math.min(index, 19)];
			}
			else
			{
				img = craftImg;
			}
			
			Affine a = new Affine();
			a.appendRotation(orientation, getCenterX(), getCenterY());
			//a.appendTranslation(-craftImg.getWidth() / 2, -craftImg.getHeight() / 2);
			a.appendTranslation(-img.getWidth() / 2, -img.getHeight() / 2);
			gc.save();
			gc.setTransform(a);
			//gc.drawImage(craftImg, getCenterX(), getCenterY());
			gc.drawImage(img, getCenterX(), getCenterY());
			if (explosion == 0 && showthrust-- > 0) {
				gc.drawImage(thrustImg, getCenterX() + craftImg.getWidth() / 2 - thrustImg.getWidth() / 2,
						getCenterY() + craftImg.getHeight());
			}
			gc.restore();
//		}
	}

	public void rotateLeft(double speed) {
		orientation -= speed;
	}

	public void rotateRight(double speed) {
		orientation += speed;
	}

	public void accellerate() {
		this.addVelocity(Math.sin(Math.toRadians(orientation)) * 10, -Math.cos(Math.toRadians(orientation)) * 10);
		showthrust = 10;

		// System.out.println("winkel: " + orientation + " " sinus: " +
		// Math.sin(Math.toRadians(orientation)*10000));
	}

	private long lastfired = 0;

	public FlyingObject fire(long timestamp)// , long loaded)
	{
		FlyingObject projectile = null;

		if ((timestamp - lastfired) / 1_000_000_000.0 > 0.1) {
			double x = Math.sin(Math.toRadians(orientation)) * 500;
			double y = -Math.cos(Math.toRadians(orientation)) * 500;
			lastfired = timestamp;
			projectile = new Shot(getCenterX(), getCenterY(), getXVelocity() + x, getYVelocity() + y, 10, timestamp);
			addVelocity(-x / 10, -y / 10);
			Gravity.playSound("shot");

		}
		return projectile;
	}

	public FlyingObject steer(ArrayList<String> input, long timestamp) {

		FlyingObject projectile = null;

		if (input.contains(keyLeft)) {
			rotateLeft(5);
		}

		if (input.contains(keyRight)) {
			rotateRight(5);
		}

		if (input.contains(keyForward)) {
			accellerate();
		}
		if (input.contains(keyFire)) {
			projectile = fire(timestamp);
			Gravity.STATUS_MSG = "fire!";
		}

		return projectile;
	}

	public void damage(long timestamp, double d) {
		this.health.set(this.health.doubleValue() - d);
		if (health.doubleValue() <= 0) {
			//this.explosion = new Explosion(timestamp);
			this.explosion = timestamp;
		}
	}

	public String toString() {
		return "craft";
	}

}
