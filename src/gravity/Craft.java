package gravity;

import java.util.ArrayList;
import java.util.Random;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

public class Craft extends FlyingObject {

	final static Image craftImg = new Image("craft.png");
	final static Image thrustImg = new Image("thrust.png");
	final static Image shieldImg = new Image("shield.png");
	final static Image[] explosionImg = new Image[20];
	static {
		for (int i = 0; i < 20; i++) {
			explosionImg[i] = new Image("explosion" + i + ".png");
		}
	}

	final static Random random = new Random();
	long explosion = 0;
	int showthrust = 0;
	boolean shieldIsUp = false;

	double craftRadius;
	double shieldRadius = 50;

	private DoubleProperty health = new SimpleDoubleProperty(50.0);

	private String keyLeft;
	private String keyRight;
	private String keyForward;
	private String keyFire;
	private String keyShield;

	public Craft(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass,
			String keyLeft, String keyRight, String keyForward, String keyShield, String keyFire, double healthbarX,
			double healthbarY) {
		super(centerX, centerY, radius, xVelocity, yVelocity, mass);

		this.keyLeft = keyLeft;
		this.keyRight = keyRight;
		this.keyForward = keyForward;
		this.keyShield = keyShield;
		this.keyFire = keyFire;
		this.craftRadius = radius;
	}

	public DoubleProperty getHealthProperty() {
		return health;
	}

	// Explosion explosion = null;

	public void show(GraphicsContext gc, long timestamp) {

		// if (explosion != null) {
		// explosion.show(gc, timestamp, getCenterX(), getCenterY());
		// } else {

		Image img;
		if (explosion > 0) {
			double secondsLiving = (timestamp - explosion) / 1_000_000_000.0;
			int index = (int) (secondsLiving / 0.04);
			img = explosionImg[Math.min(index, 19)];
			if (index == 19) {
				despawn();
			}
		} else {
			img = craftImg;
		}

		Affine a = new Affine();
		a.appendRotation(orientation, getCenterX(), getCenterY());
		a.appendTranslation(-img.getWidth() / 2, -img.getHeight() / 2);
		gc.save();
		gc.setTransform(a);
		gc.drawImage(img, getCenterX(), getCenterY());
		if (explosion == 0 && showthrust-- > 0) {
			gc.drawImage(thrustImg, getCenterX() + craftImg.getWidth() / 2 - thrustImg.getWidth() / 2,
					getCenterY() + craftImg.getHeight());
		}
		gc.restore();
		if (shieldIsUp) {
			gc.save();
			a = new Affine();
			a.appendRotation(orientation, getCenterX(), getCenterY());
			a.appendTranslation(-shieldImg.getWidth() / 2, -shieldImg.getHeight() / 2);
			gc.setTransform(a);
			gc.drawImage(shieldImg, getCenterX(), getCenterY());
			gc.restore();
		}
	}

	public void rotateLeft(double speed) {
		orientation -= speed;
	}

	public void rotateRight(double speed) {
		orientation += speed;
	}

	public void accellerate() {
		if (!shieldIsUp) {
			this.addVelocity(Math.sin(Math.toRadians(orientation)) * 10, -Math.cos(Math.toRadians(orientation)) * 10);
			showthrust = 10;
		}
	}

	private long lastfired = 0;

	public FlyingObject fire(long timestamp) {
		FlyingObject projectile = null;

		if (!shieldIsUp && (timestamp - lastfired) / 1_000_000_000.0 > 0.1) {
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

		if (input.contains(keyShield)) {
			shieldUp();
		} else {
			shieldDown();
		}

		return projectile;
	}

	public void damage(long timestamp, double d) {
		if (!shieldIsUp) {
			this.health.set(this.health.doubleValue() - d);
			if (health.doubleValue() <= 0) {
				this.explosion = timestamp;
				Gravity.playSound("explosion");
			}
		}
	}

	public void shieldUp() {
		this.shieldIsUp = true;
		setRadius(shieldRadius);
	}

	public void shieldDown() {
		this.shieldIsUp = false;
		setRadius(craftRadius);
	}

	public boolean isShieldUp() {
		return this.shieldIsUp;
	}
}
