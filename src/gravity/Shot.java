package gravity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Shot extends FlyingObject {

	private final long timeOfBirth;
	public final static double SECONDS_TO_LIVE = 5.0;
	//public final static double MASS = 50;

	public Shot(double centerX, double centerY, double xVelocity, double yVelocity, double mass, long timeOfBirth) {

		super(centerX, centerY, 4, xVelocity, yVelocity, mass);
		this.timeOfBirth = timeOfBirth;
	}

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {

		double secondsLiving = (timestamp - timeOfBirth) / 1_000_000_000.0;

		// System.out.println(""+ elapsedTime);

		if (secondsLiving < SECONDS_TO_LIVE) {
			gc.save();
			//gc.setStroke(Color.LIGHTBLUE);
			//gc.strokeLine(getCenterX(), getCenterY(), getCenterX() + 1, getCenterY() + 1);
			gc.setFill(Color.GAINSBORO);
			gc.fillOval(getCenterX()-getRadius(), getCenterY()-getRadius(), getRadius(), getRadius());
			gc.restore();
		} else {
			despawn();
		}
	}
	

}
