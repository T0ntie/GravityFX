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
	
	//shots per second
	public static double fireRate = 10;
	
	//speed pixel per second
	public static double firePower = 500;
	
	//mass of projectile
	public static double fireImpact = 10;

	final Image[] craftImgA = { new Image("craftred.png"), new Image("craftblue.png") };
	final Image[] shieldImgA = { new Image("shieldred.png"), new Image("shieldblue.png") };

	final Image craftImg;
	final Image shieldImg;

	final static int MAX_PLAYERS_DEFINED = 1;

	final static Image thrustImg = new Image("thrust.png");
	final static Image[] explosionImg = new Image[20];
	static {
		for (int i = 0; i < 20; i++) {
			explosionImg[i] = new Image("explosion" + i + ".png");
		}
	}

	final Color[] color = { Color.DARKRED, Color.ROYALBLUE };

	final static Random random = new Random();
	long explosion = 0;
	int showthrust = 0;
	long shieldIsUp = 0;
	int player = 0;

	double craftRadius;
	double shieldRadius = 50;

	private DoubleProperty health = new SimpleDoubleProperty(50.0);
	private DoubleProperty shieldPower = new SimpleDoubleProperty(50.0);

	private String keyLeft;
	private String keyRight;
	private String keyForward;
	private String keyFire;
	private String keyShield;

	public Craft(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass,
			String keyLeft, String keyRight, String keyForward, String keyShield, String keyFire, double healthbarX,
			double healthbarY, int player) {
		super(centerX, centerY, radius, xVelocity, yVelocity, mass);

		this.keyLeft = keyLeft;
		this.keyRight = keyRight;
		this.keyForward = keyForward;
		this.keyShield = keyShield;
		this.keyFire = keyFire;
		this.craftRadius = radius;
		this.player = Math.min(player, MAX_PLAYERS_DEFINED);
		this.craftImg = craftImgA[player];
		this.shieldImg = shieldImgA[player];
	}

	public DoubleProperty getHealthProperty() {
		return health;
	}

	public DoubleProperty getShieldPowerProperty() {
		return shieldPower;
	}

	public Color getColor() {
		return color[this.player];
	}

	// Explosion explosion = null;

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {

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
		if (shieldIsUp > 0) {
			double secondsShieldUp = (timestamp - this.shieldIsUp) / 1_000_000_000.0;
			shieldPower.set(this.shieldPower.doubleValue() - secondsShieldUp / 5);
			gc.save();
			a = new Affine();
			a.appendRotation(orientation, getCenterX(), getCenterY());
			a.appendTranslation(-shieldImg.getWidth() / 2, -shieldImg.getHeight() / 2);
			gc.setTransform(a);
			gc.drawImage(shieldImg, getCenterX(), getCenterY());
			gc.restore();
		} else {
			shieldPower.set(Math.min(this.shieldPower.doubleValue() + elapsedTime / 1_000_000_000.0, 50));
		}
	}

	public void rotateLeft(double speed) {
		orientation -= speed;
	}

	public void rotateRight(double speed) {
		orientation += speed;
	}

	public void accellerate(long timestamp) {
		if (shieldIsUp == 0) {
			this.addVelocity(Math.sin(Math.toRadians(orientation)) * 10, -Math.cos(Math.toRadians(orientation)) * 10);
			showthrust = 10;
		}
	}

	private long lastFired = 0;

	public FlyingObject fire(long timestamp) {
		FlyingObject projectile = null;

		//if ((shieldIsUp == 0) && (timestamp - lastFired) / 1_000_000_000.0 > 0.1) {
		if ((shieldIsUp == 0) && (timestamp - lastFired) / 1_000_000_000.0 > 1 / fireRate ) {
			double x = Math.sin(Math.toRadians(orientation)) * firePower;
			double y = -Math.cos(Math.toRadians(orientation)) * firePower;
			lastFired = timestamp;
			projectile = new Shot(getCenterX(), getCenterY(), getXVelocity() + x, getYVelocity() + y, fireImpact, timestamp);
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

		if (input.contains(keyForward)) {
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
