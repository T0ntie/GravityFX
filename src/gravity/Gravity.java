package gravity;

import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Gravity extends Application {

	public final static double BOUNCE_MODERATION = 0.1;

	public static String STATUS_MSG = "";

	private final FrameStats frameStats = new FrameStats();
	private ObservableList<FlyingObject> flyingObjects = FXCollections.observableArrayList();
	private ObservableList<Sol> sols = FXCollections.observableArrayList();
	private ObservableList<HealthBar> healthBars = FXCollections.observableArrayList();

	private final static Sound sound = new Sound(2);

	private boolean worldCreated = false;

	ArrayList<String> input = new ArrayList<String>();

	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle("Gravity");
		final BorderPane root = new BorderPane();
		// final Group root = new Group();

		Scene theScene = new Scene(root, 800, 600);
		primaryStage.setScene(theScene);

		theScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				String code = e.getCode().toString();

				// only add once... prevent duplicates
				if (!input.contains(code))
					input.add(code);

				Gravity.STATUS_MSG = code;
			}
		});

		theScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				String code = e.getCode().toString();
				input.remove(code);
			}
		});

		Canvas canvas = new Canvas(500, 500);

		final Label stats = new Label();
		stats.setTextFill(Color.RED);

		stats.textProperty().bind(frameStats.textProperty());

		final Pane canvasPane = new Pane();
		canvasPane.getChildren().add(canvas);

		root.setCenter(canvasPane);
		root.setBottom(stats);

		// applyOnResizeListener(theScene, canvas, x);

		startAnimation(canvas);

		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreen(true);
		primaryStage.show();

		primaryStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				sound.shutdown();
				Platform.exit();
			}
		});

		canvas.setHeight(canvasPane.getHeight());
		canvas.setWidth(canvasPane.getWidth());
	}

	private void startAnimation(final Canvas gameCanvas) {
		final LongProperty lastUpdateTime = new SimpleLongProperty(0);
		final AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long timestamp) {
				if (lastUpdateTime.get() > 0) {
					long elapsedTime = timestamp - lastUpdateTime.get();
					checkCollisions(timestamp, gameCanvas.getWidth(), gameCanvas.getHeight());
					updateWorld(timestamp, elapsedTime, gameCanvas);
					frameStats.addFrame(elapsedTime);
				}
				lastUpdateTime.set(timestamp);
			}

		};
		timer.start();
	}

	private void updateWorld(long timestamp, long elapsedTime, final Canvas canvas) {
		double elapsedSeconds = elapsedTime / 1_000_000_000.0;

		GraphicsContext gx = canvas.getGraphicsContext2D();
		gx.setFill(Color.BLACK);
		gx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		if (!worldCreated) {
			createWorld(timestamp);
		}

		for (Sol so : sols) {
			so.show(gx, timestamp);
		}

		for (HealthBar bar : healthBars) {
			bar.show(gx, timestamp);
		}

		List<FlyingObject> toBeRemoved = new ArrayList<FlyingObject>();
		List<FlyingObject> toBeAdded = new ArrayList<FlyingObject>();

		for (FlyingObject fo : flyingObjects) {

			fo.setCenterX(fo.getCenterX() + elapsedSeconds * fo.getXVelocity());
			fo.setCenterY(fo.getCenterY() + elapsedSeconds * fo.getYVelocity());

			for (Sol so : sols) {
				final double deltaX = so.getCenterX() - fo.getCenterX();
				final double deltaY = so.getCenterY() - fo.getCenterY();
				final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
				final double gravity = Math.min(so.getMass() * fo.getMass() / (distance * distance), 1000);

				fo.addVelocity(deltaX / distance * gravity, deltaY / distance * gravity);
			}

			if (fo instanceof Craft) {
				FlyingObject projectile = ((Craft) fo).steer(input, timestamp);

				if (projectile != null) {
					toBeAdded.add(projectile);
				}
			}

			fo.show(gx, timestamp);

			if (fo.isToBeDespawned())
			{
				toBeRemoved.add(fo);
			}
			
//			if (fo instanceof Shot) {
//				if (((Shot) fo).isToBeDespawned()) {
//					toBeRemoved.add(fo);
//				}
//			}
		}

		flyingObjects.removeAll(toBeRemoved);
		flyingObjects.addAll(toBeAdded);

		// canvas.setHeight(((BorderPane) canvas.getParent()).getHeight());
		// canvas.setWidth(((BorderPane) canvas.getParent()).getWidth());
	}

	private void checkCollisions(long timestamp, double maxX, double maxY) {

		for (ListIterator<FlyingObject> slowIt = flyingObjects.listIterator(); slowIt.hasNext();) {
			FlyingObject fo1 = slowIt.next();

			// check wall collisions:
			double xVel = fo1.getXVelocity();
			double yVel = fo1.getYVelocity();

			if (fo1 instanceof Craft) {

				if ((fo1.getCenterX() - fo1.getRadius() <= 0 && xVel < 0)
						|| (fo1.getCenterX() + fo1.getRadius() >= maxX && xVel > 0)) {
					fo1.setXVelocity(-xVel * BOUNCE_MODERATION);
					Gravity.playSound("wall");
				}
				if ((fo1.getCenterY() - fo1.getRadius() <= 0 && yVel < 0)
						|| (fo1.getCenterY() + fo1.getRadius() >= maxY && yVel > 0)) {
					fo1.setYVelocity(-yVel * BOUNCE_MODERATION);
					Gravity.playSound("wall");
				}
			}

			for (ListIterator<FlyingObject> fastIt = flyingObjects.listIterator(slowIt.nextIndex()); fastIt
					.hasNext();) {
				FlyingObject fo2 = fastIt.next();
				// performance hack: both colliding(...) and bounce(...) need deltaX and deltaY,
				// so compute them once here:
				final double deltaX = fo2.getCenterX() - fo1.getCenterX();
				final double deltaY = fo2.getCenterY() - fo1.getCenterY();

				if (colliding(fo1, fo2, deltaX, deltaY)) {
					bounce(fo1, fo2, deltaX, deltaY);
					if (fo1 instanceof Craft) {
						if (fo2 instanceof Shot) {
							((Craft) fo1).damage(timestamp, 5);
							if (((Craft) fo1).isShieldUp() == false) {
								((Shot) fo2).despawn();
								//Gravity.playSound("hitcraft" + new Random().nextInt(2));
								Gravity.playSound("hitcraft");
							}
							else
							{
								//Gravity.playSound("hitshield" + new Random().nextInt(2));
								Gravity.playSound("hitshield");
							}

						} else {
							Gravity.playSound("crash");
							((Craft)fo1).damage(timestamp, 3);
							((Craft)fo2).damage(timestamp, 3);
						}

					}
				}
			}

			for (ListIterator<Sol> solIt = sols.listIterator(); solIt.hasNext();) {
				Sol so = solIt.next();
				final double radiusSum = fo1.getRadius() + so.getRadius();
				final double deltaX = fo1.getCenterX() - so.getCenterX();
				final double deltaY = fo1.getCenterY() - so.getCenterY();
				if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
					if (fo1 instanceof Craft) {
						// Gravity.sound.playSound("fire");
					}
				}

			}
		}

	}

	public boolean colliding(final FlyingObject fo1, final FlyingObject fo2, final double deltaX, final double deltaY) {
		// square of distance between flying objects is s^2 = (x2-x1)^2 + (y2-y1)^2
		// flying objects are "overlapping" if s^2 < (r1 + r2)^2
		// We also check that distance is decreasing, i.e.
		// d/dt(s^2) < 0:
		// 2(x2-x1)(x2'-x1') + 2(y2-y1)(y2'-y1') < 0

		final double radiusSum = fo1.getRadius() + fo2.getRadius();
		if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
			if (deltaX * (fo2.getXVelocity() - fo1.getXVelocity())
					+ deltaY * (fo2.getYVelocity() - fo1.getYVelocity()) < 0) {
				return true;
			}
		}
		return false;
	}

	private void bounce(final FlyingObject fo1, final FlyingObject fo2, final double deltaX, final double deltaY) {
		final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
		final double unitContactX = deltaX / distance;
		final double unitContactY = deltaY / distance;

		final double xVelocity1 = fo1.getXVelocity();
		final double yVelocity1 = fo1.getYVelocity();
		final double xVelocity2 = fo2.getXVelocity();
		final double yVelocity2 = fo2.getYVelocity();

		final double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY; // velocity of ball 1 parallel to
																					// contact vector
		final double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY; // same for ball 2

		final double massSum = fo1.getMass() + fo2.getMass();
		final double massDiff = fo1.getMass() - fo2.getMass();

		final double v1 = (2 * fo2.getMass() * u2 + u1 * massDiff) / massSum; // These equations are derived for
																				// one-dimensional collision by
		final double v2 = (2 * fo1.getMass() * u1 - u2 * massDiff) / massSum; // solving equations for conservation of
																				// momentum and conservation of energy

		final double u1PerpX = xVelocity1 - u1 * unitContactX; // Components of ball 1 velocity in direction
																// perpendicular
		final double u1PerpY = yVelocity1 - u1 * unitContactY; // to contact vector. This doesn't change with collision
		final double u2PerpX = xVelocity2 - u2 * unitContactX; // Same for ball 2....
		final double u2PerpY = yVelocity2 - u2 * unitContactY;

		fo1.setXVelocity(v1 * unitContactX + u1PerpX);
		fo1.setYVelocity(v1 * unitContactY + u1PerpY);
		fo2.setXVelocity(v2 * unitContactX + u2PerpX);
		fo2.setYVelocity(v2 * unitContactY + u2PerpY);

	}

	public static void playSound(String sound) {
		Gravity.sound.playSound(sound);
	}

	private void createWorld(long timestamp) {
		worldCreated = true;
		// flyingObjects.add(new FlyingObject(300, 300, 20, 10, 1, 0));
		Craft craft1 = new Craft(500, 200, 20, 10, 1, 10, "LEFT", "RIGHT", "UP", "DOWN", "INSERT", 100, 5);
		Craft craft2 = new Craft(500, 500, 20, 10, 1, 60, "A", "D", "W", "S", "SPACE", 100, 20);
		flyingObjects.add(craft1);
		flyingObjects.add(craft2);
		HealthBar bar1 = new HealthBar(craft1, 10, 5);
		HealthBar bar2 = new HealthBar(craft2, 1400, 5);
		healthBars.add(bar1);
		healthBars.add(bar2);
		// flyingObjects.add(new Shot(550, 200, 60, 0, timestamp));
		sols.add(new Sol(1000, 500, 60, 2000));
		sols.add(new Sol(800, 500, 50, 2000));
	}

	// private void applyOnResizeListener(final Scene scene, final Canvas canvas,
	// final Pane pane) {
	//
	// ChangeListener cl = new ChangeListener<Number>() {
	// public void changed(ObservableValue<? extends Number> observable, Number
	// oldValue, Number newValue) {
	// pane.setPrefWidth(scene.getWidth());
	// pane.setPrefHeight(scene.getHeight());
	// canvas.setWidth(pane.getWidth());
	// canvas.setHeight(pane.getHeight());
	// }
	// };
	//
	// scene.widthProperty().addListener(cl);
	// scene.heightProperty().addListener(cl);
	// }

	private static class FrameStats {
		private long frameCount;
		private double meanFrameInterval; // millis
		private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(this, "text", "FPS: 0");

		public long getFrameCount() {
			return frameCount;
		}

		public double getMeanFrameInterval() {
			return meanFrameInterval;
		}

		public void addFrame(long frameDurationNanos) {
			meanFrameInterval = (meanFrameInterval * frameCount + frameDurationNanos / 1_000_000_000.0)
					/ (frameCount + 1);
			frameCount++;
			text.set(toString());
		}

		public String getText() {
			return text.get();
		}

		public ReadOnlyStringProperty textProperty() {
			return text.getReadOnlyProperty();
		}

		@Override
		public String toString() {

			return String.format("FPS: %.1f - %s", 1 / getMeanFrameInterval(), STATUS_MSG);
			// return String.format("Frame count: %,d Average frame interval: %.3f
			// milliseconds", getFrameCount(),
			// getMeanFrameInterval());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
