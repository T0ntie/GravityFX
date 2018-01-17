package gravity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Shot extends FlyingObject {

	private final long timeOfBirth;
	public final static double SECONDS_TO_LIVE = 5.0;
	//public final static double MASS = 50;
	private boolean toBeDespawned = false;

	public Shot(double centerX, double centerY, double xVelocity, double yVelocity, double mass, long timeOfBirth) {

		super(centerX, centerY, 1, xVelocity, yVelocity, mass);
		this.timeOfBirth = timeOfBirth;
	}

	public void show(GraphicsContext gc, long timestamp) {

		double secondsLiving = (timestamp - timeOfBirth) / 1_000_000_000.0;

		// System.out.println(""+ elapsedTime);

		if (secondsLiving < SECONDS_TO_LIVE) {
			gc.save();
			gc.setStroke(Color.LIGHTBLUE);
			gc.strokeLine(getCenterX(), getCenterY(), getCenterX() + 1, getCenterY() + 1);
			gc.restore();
		} else {
			toBeDespawned = true;
		}
	}
	
	public boolean isToBeDespawned()
	{
		return this.toBeDespawned;
	}

}
