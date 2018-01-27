package gravity;

import java.util.ArrayList;
import java.util.Random;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

public class Craft extends FlyingObject {

	public final static Image BLUE_CRAFT_IMAGE = new Image("imgs/craftblue.png");
	public final static Image RED_CRAFT_IMAGE = new Image("imgs/craftred.png");
	public final static Image BLUE_SHIELD_IMAGE = new Image("imgs/shieldblue.png");
	public final static Image RED_SHIELD_IMAGE = new Image("imgs/shieldred.png");
	public final static Image GREEN_SHIELD_IMAGE = new Image("imgs/shieldgreen.png");

	private final Image craftImg;
	private final Image shieldImg;
	private final Color color;
	
	//Key codes
	private String keyFire;
	private String keyLeft;
	private String keyRight;
	private String keyThrust;
	private String keyShield;
	
	//shots per second
	protected double fireRate = 10;

	//projectile velocity pixel per second
	private double firePower = 500;
	
	//projectile mass
	private double fireImpact = 10;


	final static Image thrustImg = new Image("imgs/thrust.png");
	final static Image[] explosionImg = new Image[20];
	static {
		for (int i = 0; i < 20; i++) {
			explosionImg[i] = new Image("imgs/explosion" + i + ".png");
		}
	}

	final static Random random = new Random();
	long explosion = 0;
	int showthrust = 0;
	long shieldIsUp = 0;

	double craftRadius;
	double shieldRadius = 50;

	private final DoubleProperty health;
	private DoubleProperty shieldPower = new SimpleDoubleProperty(50.0);

	public Craft(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass,
			String keyLeft, String keyRight, String keyThrust, String keyShield, String keyFire, Image craftImg, Image shieldImg, Color color) {
		super(centerX, centerY, radius, xVelocity, yVelocity, mass);
		this.keyThrust = keyThrust;
		this.keyLeft = keyLeft;
		this.keyRight = keyRight;
		this.keyFire = keyFire;
		this.keyShield = keyShield;
		this.health = new SimpleDoubleProperty(50);
		this.craftRadius = radius;
		this.craftImg = craftImg;
		this.shieldImg = shieldImg;
		this.color = color;
	}

	public DoubleProperty getHealthProperty() {
		return health;
	}

	public DoubleProperty getShieldPowerProperty() {
		return shieldPower;
	}

	public Color getColor() {
		return color;
	}

	// Explosion explosion = null;

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {

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
		if (shieldIsUp > 0) {
			double secondsShieldUp = (timestamp - this.shieldIsUp) / 1_000_000_000.0;
			shieldPower.set(this.shieldPower.doubleValue() - secondsShieldUp / 5);
			gc.save();
			a = new Affine();
			a.appendRotation(orientation, getCenterX(), getCenterY());
			//a.appendTranslation(-shieldImg.getWidth() / 2, -shieldImg.getHeight() / 2);
			a.appendTranslation(-shieldRadius, -shieldRadius);
			gc.setTransform(a);
			gc.drawImage(shieldImg, getCenterX(), getCenterY(), shieldRadius*2, shieldRadius*2);
			gc.restore();
		} else {
			shieldPower.set(Math.min(this.shieldPower.doubleValue() + elapsedTime / 1_000_000_000.0, 50));
		}
	}

	public void rotateLeft(double angle) {
		orientation -= angle;
	}

	public void rotateRight(double angle) {
		orientation += angle;
	}

	public void accellerate(long timestamp) {
		if (shieldIsUp == 0) {
			this.addVelocity(Math.sin(Math.toRadians(orientation)) * 10, -Math.cos(Math.toRadians(orientation)) * 10);
			showthrust = 10;
		}
	}

	protected long lastFired = 0;

	public FlyingObject fire(long timestamp) {
		FlyingObject projectile = null;

		if ((shieldIsUp == 0) && (timestamp - lastFired) / 1_000_000_000.0 > 1 / fireRate) {
			double x = Math.sin(Math.toRadians(orientation)) * firePower;
			double y = -Math.cos(Math.toRadians(orientation)) * firePower;
			lastFired = timestamp;
			projectile = new Shot(getCenterX(), getCenterY(), getXVelocity() + x, getYVelocity() + y,
					fireImpact, timestamp, this, this.getColor());
			addVelocity(-x / 20, -y / 20);
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

		if (input.contains(keyThrust)) {
			accellerate(timestamp);
		}

		if (input.contains(keyFire)) {
			projectile = fire(timestamp);
		}

		if (input.contains(keyShield)) {
			shieldUp(timestamp);
		} else {
			shieldDown(timestamp);
		}

		return projectile;
	}

	public void damage(long timestamp, double d) {
		if (shieldIsUp == 0) {
			this.health.set(this.health.doubleValue() - d);
			if (health.doubleValue() <= 0 && explosion == 0) {
				this.explosion = timestamp;
				this.shieldPower.set(0);
				Gravity.playSound("explosion");
			}
		} else {
			this.shieldPower.set(shieldPower.doubleValue() - d / 2);
		}
	}

	public void shieldUp(long timestamp) {

		if ((shieldIsUp > 0 && shieldPower.doubleValue() > 0) || shieldPower.doubleValue() > 5) {
			if (shieldIsUp == 0) {
				this.shieldIsUp = timestamp;
				setRadius(shieldRadius);
			}
		} else {
			this.shieldIsUp = 0;
			setRadius(craftRadius);
		}
	}

	public void shieldDown(long timestamp) {
		this.shieldIsUp = 0;
		setRadius(craftRadius);
	}

	public boolean isShieldUp() {
		return (this.shieldIsUp > 0);
	}
}
