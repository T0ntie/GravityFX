package gravity;

import static java.lang.Math.sqrt;

import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;

public class FlyingObject extends GravityObject {

	private final DoubleProperty xVelocity; // pixels per second
	private final DoubleProperty yVelocity;
	private final ReadOnlyDoubleWrapper speed;
	double orientation = 0;
	private boolean toBeDespawned = false;


	public FlyingObject(double centerX, double centerY, double radius, double xVelocity, double yVelocity,
			double mass) {

		super(centerX, centerY, radius, mass);

		this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
		this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
		this.speed = new ReadOnlyDoubleWrapper(this, "speed");

		speed.bind(Bindings.createDoubleBinding(new Callable<Double>() {

			@Override
			public Double call() throws Exception {
				final double xVel = getXVelocity();
				final double yVel = getYVelocity();
				return sqrt(xVel * xVel + yVel * yVel);
			}
		}, this.xVelocity, this.yVelocity));
	}

	public final void addVelocity(double x, double y) {
		double ox = getXVelocity();
		double oy = getYVelocity();
		setXVelocity(ox + x);
		setYVelocity(oy + y);
	}

	public final double getXVelocity() {
		return xVelocity.get();
	}

	public final void setXVelocity(double xVelocity) {
		this.xVelocity.set(xVelocity);
	}

	public final DoubleProperty xVelocityProperty() {
		return xVelocity;
	}

	public final double getYVelocity() {
		return yVelocity.get();
	}

	public final void setYVelocity(double yVelocity) {
		this.yVelocity.set(yVelocity);
	}

	public final DoubleProperty yVelocityProperty() {
		return yVelocity;
	}

	public final double getSpeed() {
		return speed.get();
	}

	public final ReadOnlyDoubleProperty speedProperty() {
		return speed.getReadOnlyProperty();
	}

	public final void addOrientation(double delta) {
		this.orientation += delta;
	}

	public final double getOrientation() {
		return this.orientation;
	}
	
	public final void setOrientation(double orientation)
	{
		this.orientation = orientation;
	}
	public boolean isToBeDespawned()
	{
		return this.toBeDespawned;
	}
	
	public void despawn()
	{
		this.toBeDespawned = true;
	}
	
}
