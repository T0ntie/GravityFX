package gravity.props;

import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CraftProps {
	
	//shots per second
	private double fireRate = 10;

	//projectile velocity pixel per second
	private double firePower = 500;
	
	//projectile mass
	private double fireImpact = 10;
	
	//Image of the craft
	private Image craftImg;
	
	//Key codes
	private String keyFire;
	private String keyLeft;
	private String keyRight;
	private String keyThrust;
	private String keyShield;

	public CraftProps()
	{
		super();
	}
	
	public String getKeyLeft() {
		return keyLeft;
	}

	public void setKeyLeft(String keyLeft) {
		this.keyLeft = keyLeft;
	}

	public String getKeyRight() {
		return keyRight;
	}

	public void setKeyRight(String keyRight) {
		this.keyRight = keyRight;
	}

	public String getKeyThurst() {
		return keyThrust;
	}

	public void setKeyThrust(String keyForward) {
		this.keyThrust = keyForward;
	}

	public String getKeyShield() {
		return keyShield;
	}

	public void setKeyShield(String keyShield) {
		this.keyShield = keyShield;
	}

	public Image getCraftImg() {
		return craftImg;
	}

	public void setCraftImg(Image craftImg) {
		this.craftImg = craftImg;
	}

	public double getFireImpact() {
		return fireImpact;
	}

	public void setFireImpact(double fireImpact) {
		this.fireImpact = fireImpact;
	}

	public double getFireRate() {
		return fireRate;
	}

	public double getFirePower() {
		return firePower;
	}

	public void setFirePower(double firePower) {
		this.firePower = firePower;
	}

	public void setFireRate(double fireRate) {
		this.fireRate = fireRate;
	}

	public CraftProps(double fireRate, double firePower)
	{
		this.fireRate = fireRate;
		this.firePower = firePower;
	}
	
	public void showPropertyDialog(Stage owner)
	{
		Dialog<CraftProps> dialog = new Dialog<>();

		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		VBox vb = new VBox();
		
		
		Slider slFr = new Slider();
		slFr.setMin(0);
		slFr.setMax(30);
		slFr.setValue(this.fireRate);
		slFr.setShowTickLabels(true);
		slFr.setShowTickMarks(true);
		slFr.setMajorTickUnit(10);
		slFr.setMinorTickCount(1);
		slFr.setBlockIncrement(5);
		vb.getChildren().addAll(new Label("Shots per second"), slFr);
		

		Slider slFp = new Slider();
		slFp.setMin(0);
		slFp.setMax(1500);
		slFp.setValue(this.firePower);
		slFp.setShowTickLabels(true);
		slFp.setShowTickMarks(true);
		slFp.setMajorTickUnit(500);
		slFp.setMinorTickCount(200);
		slFp.setBlockIncrement(500);
		vb.getChildren().addAll(new Label("Projectile Velocity"), slFp);

		Slider slFi = new Slider();
		slFi.setMin(0);
		slFi.setMax(100);
		slFi.setValue(this.fireImpact);
		slFi.setShowTickLabels(true);
		slFi.setShowTickMarks(true);
		slFi.setMajorTickUnit(10);
		slFi.setMinorTickCount(5);
		slFi.setBlockIncrement(10);
		vb.getChildren().addAll(new Label("Projectile Impact"), slFi);

		TextField tfTk = new TextField();
		tfTk.setText(getKeyThurst());
		tfTk.setEditable(false);
		tfTk.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				tfTk.setText(e.getCode().toString());
			}
		});
		vb.getChildren().addAll(new Label("Key Thurst"), tfTk);

		TextField tfLk = new TextField();
		tfLk.setText(getKeyLeft());
		tfLk.setEditable(false);
		tfLk.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				tfLk.setText(e.getCode().toString());
			}
		});
		vb.getChildren().addAll(new Label("Key Steer Left"), tfLk);

		TextField tfRk = new TextField();
		tfRk.setText(getKeyRight());
		tfRk.setEditable(false);
		tfRk.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				tfRk.setText(e.getCode().toString());
			}
		});
		vb.getChildren().addAll(new Label("Key Steer Right"), tfRk);

		TextField tfFk = new TextField();
		tfFk.setText(getKeyFire());
		tfFk.setEditable(false);
		tfFk.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				tfFk.setText(e.getCode().toString());
			}
		});
		vb.getChildren().addAll(new Label("Key Fire"), tfFk);

		TextField tfSk = new TextField();
		tfSk.setText(getKeyShield());
		tfSk.setEditable(false);
		tfSk.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				tfSk.setText(e.getCode().toString());
			}
		});
		vb.getChildren().addAll(new Label("Key Shield"), tfSk);

		dialog.getDialogPane().setContent(vb);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				this.fireRate = slFr.getValue();
				this.firePower = slFp.getValue();
				this.fireImpact = slFi.getValue();
				this.keyThrust = tfTk.getText();
				this.keyLeft = tfLk.getText();
				this.keyRight = tfRk.getText();
				this.keyFire = tfFk.getText();
				this.keyShield = tfSk.getText();
				return this;
			}
			return null;
		});
		

		dialog.initOwner(owner);
		dialog.showAndWait();
		
	}

	public String getKeyFire() {
		return keyFire;
	}

	public void setKeyFire(String keyFire) {
		this.keyFire = keyFire;
	}
}
