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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Gravity extends Application {

	public final static String SCENARIO_CENTRAL_SUN = "Central Sun";
	public final static String SCENARIO_DOGFIGHT = "Dogfight";
	public final static String SCENARIO_TWO_SUNS = "Two Suns";
	public final static String SCENARIO_ALIEN = "Alien";

	public String scenario = SCENARIO_DOGFIGHT;

	public final static double BOUNCE_MODERATION = 0.1;
	public final static double WALL_DAMAGE = 0.05;
	public final static double CRASH_DAMAGE = 1;
	public final static double SHOT_DAMAGE_PER_MASS = 0.5;

	public final static double MAX_GRAVITY = 100;

	public static String STATUS_MSG = "Gravity 1.0";

	private final FrameStats frameStats = new FrameStats();
	private ObservableList<FlyingObject> flyingObjects = FXCollections.observableArrayList();
	private ObservableList<Sol> sols = FXCollections.observableArrayList();
	private ObservableList<Craft> crafts = FXCollections.observableArrayList();
	private ObservableList<Alien> aliens = FXCollections.observableArrayList();
	private ObservableList<EnergyBar> energyBars = FXCollections.observableArrayList();

	private final static Sound sound = new Sound();

	private boolean worldCreated = false;

	ArrayList<String> input = new ArrayList<String>();

	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle("Gravity");
		final BorderPane root = new BorderPane();

		Scene theScene = new Scene(root, 800, 600);
		primaryStage.setScene(theScene);

		theScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				String code = e.getCode().toString();
				// only add once... prevent duplicates
				if (!input.contains(code))
					input.add(code);
			}
		});

		theScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
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

		createWorld();

		root.setTop(createMenus(primaryStage));

		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreen(true);
		primaryStage.show();

		primaryStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				Platform.exit();
			}
		});

		canvas.setHeight(canvasPane.getHeight());
		canvas.setWidth(canvasPane.getWidth());

		startAnimation(canvas);
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
			createWorld();
		}

		for (Sol so : sols) {
			so.show(gx, timestamp, elapsedTime);
		}

		for (EnergyBar bar : energyBars) {
			bar.show(gx, timestamp, elapsedTime);
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
				final double gravity = Math.min(so.getMass() * fo.getMass() / (distance * distance), MAX_GRAVITY);

				fo.addVelocity(deltaX / distance * gravity, deltaY / distance * gravity);
			}

			FlyingObject projectile = null;

			if (fo instanceof Alien) {
				projectile = ((Alien) fo).react(timestamp, flyingObjects);
			} else if (fo instanceof Craft) {
				projectile = ((Craft) fo).steer(input, timestamp);
			}
			if (projectile != null) {
				toBeAdded.add(projectile);
			}

			fo.show(gx, timestamp, elapsedTime);

			if (fo.isToBeDespawned()) {
				toBeRemoved.add(fo);
			}
		}

		flyingObjects.removeAll(toBeRemoved);
		flyingObjects.addAll(toBeAdded);
		crafts.removeAll(toBeRemoved);
		aliens.removeAll(toBeRemoved);
	}

	private void checkCollisions(long timestamp, double maxX, double maxY) {

		for (ListIterator<FlyingObject> slowIt = flyingObjects.listIterator(); slowIt.hasNext();) {
			FlyingObject fo1 = slowIt.next();

			// check wall collisions:
			double xVel = fo1.getXVelocity();
			double yVel = fo1.getYVelocity();

			if (fo1 instanceof Craft) {
				Craft craft1 = (Craft) fo1;
				boolean wallHit = false;

				if ((craft1.getCenterX() - craft1.getRadius() <= 0 && xVel < 0)
						|| (craft1.getCenterX() + craft1.getRadius() >= maxX && xVel > 0)) {
					craft1.setXVelocity(-xVel * BOUNCE_MODERATION);
					wallHit = true;
				}
				if ((craft1.getCenterY() - craft1.getRadius() <= 0 && yVel < 0)
						|| (craft1.getCenterY() + craft1.getRadius() >= maxY && yVel > 0)) {
					craft1.setYVelocity(-yVel * BOUNCE_MODERATION);
					wallHit = true;
				}
				if (wallHit) {
					if (craft1.isShieldUp()) {
						Gravity.playSound("hitshield");
					} else {
						craft1.damage(timestamp, WALL_DAMAGE);
						Gravity.playSound("wall");
					}
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
					//bounce
					bounce(fo1, fo2, deltaX, deltaY);
					//cause damage
					if (fo1 instanceof Craft) {
						if (fo2 instanceof Shot) {
							Craft cr1 = (Craft) fo1;
							Shot sh2 = (Shot) fo2;
							cr1.damage(timestamp, sh2.getMass() * SHOT_DAMAGE_PER_MASS);
							if ((cr1).isShieldUp() == false) {
								sh2.explode = true;
								sh2.despawn();
								Gravity.playSound("hitcraft");
							} else {
								Gravity.playSound("hitshield");
							}
						} else {
							Gravity.playSound("crash");
							((Craft) fo1).damage(timestamp, CRASH_DAMAGE);
							((Craft) fo2).damage(timestamp, CRASH_DAMAGE);
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
						((Craft) fo1).damage(timestamp, 0.1);
						Gravity.sound.playSound("crackle");
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

	private void createWorld() {

		switch (this.scenario) {
		case SCENARIO_DOGFIGHT:
			createDogfightScenario();
			break;
		case SCENARIO_CENTRAL_SUN:
			createCentralSunScenario();
			break;

		case SCENARIO_TWO_SUNS:
			createTwoSunsScenario();
			break;

		case SCENARIO_ALIEN:
			createAlienScenario();
			break;
		}
		worldCreated = true;
	}

	private void createDogfightScenario() {

		Rectangle2D vb = Screen.getPrimary().getVisualBounds();

		Craft craft1;
		Craft craft2;
		if (crafts.isEmpty()) {
			craft1 = new Craft(500, vb.getHeight() / 2, 20, 10, -200, 40, "A", "D", "W", "S", "SPACE",
					Craft.BLUE_CRAFT_IMAGE, Craft.BLUE_SHIELD_IMAGE, Craft.SHIELD_RADIUS, Color.MEDIUMBLUE);
			craft2 = new Craft(vb.getWidth() - 500, vb.getHeight() / 2, 20, -10, 200, 40, "LEFT", "RIGHT", "UP",
					"CLEAR", "INSERT", Craft.RED_CRAFT_IMAGE, Craft.RED_SHIELD_IMAGE, Craft.SHIELD_RADIUS,
					Color.DARKRED);
			crafts.addAll(craft1, craft2);
		} else {
			craft1 = crafts.get(0);
			craft2 = crafts.get(1);
		}

		flyingObjects.addAll(craft1, craft2);
		layoutEnergyBars();
	}

	private void layoutEnergyBars() {

		Rectangle2D vb = Screen.getPrimary().getVisualBounds();
		double[] xpos = { 20.0, vb.getWidth() - 270, vb.getWidth() / 2 - (5 * 50) / 2 };

		for (int i = 0; i < crafts.size(); i++) {
			Craft craft = crafts.get(i);

			EnergyBar hbar = new EnergyBar(craft.getHealthProperty(), craft.getColor(), xpos[i], vb.getHeight() - 30,
					50 * 5, 10);
			EnergyBar sbar = new EnergyBar(craft.getShieldPowerProperty(), craft.getColor().brighter(), xpos[i],
					vb.getHeight() - 15, 50 * 5, 5);
			energyBars.addAll(hbar, sbar);
		}

		for (int i = 0; i < aliens.size(); i++) {
			Alien alien = (Alien) aliens.get(i);
			EnergyBar hbar = new EnergyBar(alien.getHealthProperty(), alien.getColor(), xpos[2], vb.getHeight() - 30,
					50 * 5, 10);
			EnergyBar sbar = new EnergyBar(alien.getShieldPowerProperty(), alien.getColor().brighter(), xpos[2],
					vb.getHeight() - 15, 50 * 5, 5);
			energyBars.addAll(hbar, sbar);
		}
	}

	private void createCentralSunScenario() {
		Rectangle2D vb = Screen.getPrimary().getVisualBounds();
		createDogfightScenario();
		sols.add(new Sol((vb.getWidth() / 2), (vb.getHeight() / 2), 60, 20000));
	}

	private void createTwoSunsScenario() {
		Rectangle2D vb = Screen.getPrimary().getVisualBounds();
		createDogfightScenario();

		sols.add(new Sol((vb.getWidth() / 3), (vb.getHeight() / 3), 50, 10000));
		sols.add(new Sol((vb.getWidth() * 2 / 3), (vb.getHeight() * 2 / 3), 50, 10000));
	}

	private void createAlienScenario() {
		Rectangle2D vb = Screen.getPrimary().getVisualBounds();

		Alien alien = new Alien(vb.getWidth() / 2, vb.getHeight() / 2, 30, 0, 0, 400);
		flyingObjects.addAll(alien);
		aliens.addAll(alien);
		createDogfightScenario();

	}

	private void restart() {
		energyBars.clear();
		flyingObjects.clear();
		sols.clear();
		crafts.clear();
		aliens.clear();
		worldCreated = false;
	}

	private void quit() {
		Platform.exit();
	}

	private void setScenario(String scenario) {
		this.scenario = scenario;
		restart();
	}

	private MenuBar createMenus(Stage stage) {
		MenuBar menuBar = new MenuBar();
		Menu menuGame = new Menu("Game");
		MenuItem restart = new MenuItem("Restart");
		MenuItem quit = new MenuItem("Quit");

		restart.setOnAction(e -> restart());
		quit.setOnAction(e -> quit());

		menuGame.getItems().addAll(restart, quit);

		Menu menuScenario = new Menu("Scenario");

		ToggleGroup radioGroup = new ToggleGroup();

		RadioMenuItem miSdf = new RadioMenuItem(SCENARIO_DOGFIGHT);
		miSdf.setSelected(true);
		miSdf.setToggleGroup(radioGroup);

		miSdf.setOnAction(e -> setScenario(SCENARIO_DOGFIGHT));

		RadioMenuItem miScs = new RadioMenuItem(SCENARIO_CENTRAL_SUN);
		miScs.setOnAction(e -> setScenario(SCENARIO_CENTRAL_SUN));
		miScs.setToggleGroup(radioGroup);

		RadioMenuItem miSts = new RadioMenuItem(SCENARIO_TWO_SUNS);
		miSts.setOnAction(e -> setScenario(SCENARIO_TWO_SUNS));
		miSts.setToggleGroup(radioGroup);

		RadioMenuItem miSal = new RadioMenuItem(SCENARIO_ALIEN);
		miSal.setOnAction(e -> setScenario(SCENARIO_ALIEN));
		miSal.setToggleGroup(radioGroup);

		menuScenario.getItems().addAll(miSdf, miScs, miSts, miSal);

		// Menu menuProps = new Menu("Properties");
		//
		// int i = 1;
		// for (Craft craft : crafts) {
		// CraftProps props = craft.getProperties();
		// MenuItem miCp = new MenuItem("Craft Player " + i++);
		// //ImageView iv = new ImageView(props.getCraftImg());
		// //iv.setPreserveRatio(true);
		// //iv.setFitHeight(25);
		// //miCp.setGraphic(iv);
		// miCp.setOnAction(e -> props.showPropertyDialog(stage));
		// menuProps.getItems().add(miCp);
		// }
		//
		menuBar.getMenus().addAll(menuGame, menuScenario);// , menuProps);
		return menuBar;
	}

	private static class FrameStats {
		private long frameCount;
		private double meanFrameInterval; // millis
		private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(this, "text", "FPS: 0");

		public double getMeanFrameInterval() {
			return meanFrameInterval;
		}

		public void addFrame(long frameDurationNanos) {
			meanFrameInterval = (meanFrameInterval * frameCount + frameDurationNanos / 1_000_000_000.0)
					/ (frameCount + 1);
			frameCount++;
			text.set(toString());
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
