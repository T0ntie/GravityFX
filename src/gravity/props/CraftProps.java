package gravity.props;

import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
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

	public Image getCraftImg() {
		return craftImg;
	}

	public void setCraftImg(Image craftImg) {
		this.craftImg = craftImg;
	}

	public CraftProps()
	{
		super();
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

		Slider slFr = new Slider();
		slFr.setMin(0);
		slFr.setMax(30);
		slFr.setValue(this.fireRate);
		slFr.setShowTickLabels(true);
		slFr.setShowTickMarks(true);
		slFr.setMajorTickUnit(10);
		slFr.setMinorTickCount(1);
		slFr.setBlockIncrement(5);

		Slider slFp = new Slider();
		slFp.setMin(0);
		slFp.setMax(1500);
		slFp.setValue(this.firePower);
		slFp.setShowTickLabels(true);
		slFp.setShowTickMarks(true);
		slFp.setMajorTickUnit(500);
		slFp.setMinorTickCount(200);
		slFp.setBlockIncrement(500);

		Slider slFi = new Slider();
		slFi.setMin(0);
		slFi.setMax(100);
		slFi.setValue(this.fireImpact);
		slFi.setShowTickLabels(true);
		slFi.setShowTickMarks(true);
		slFi.setMajorTickUnit(10);
		slFi.setMinorTickCount(5);
		slFi.setBlockIncrement(10);

		
		VBox vb = new VBox();

		vb.getChildren().addAll(new Label("Shots per second"), slFr, new Label("velocity of projectile"), slFp, new Label("mass of projectile"), slFi);

		dialog.getDialogPane().setContent(vb);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				this.fireRate = slFr.getValue();
				this.firePower = slFp.getValue();
				this.fireImpact = slFi.getValue();
				return this;
			}
			return null;
		});

		dialog.initOwner(owner);
		dialog.showAndWait();
		
	}
}
