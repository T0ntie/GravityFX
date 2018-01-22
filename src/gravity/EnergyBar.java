package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EnergyBar {
	
	private DoubleProperty value;
	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	private Color color;
	private double posX;
	private double posY;
	private double height;
	private double maxWidth;

	public EnergyBar(DoubleProperty value)
	{
		this.value = value;
	}
	
	public EnergyBar (DoubleProperty value, Color color, double posX, double posY, double maxWidth, double height)
	{
		this.value = value;
		this.color = color;
		this.posX = posX;
		this.posY = posY;
		this.height = height;
		this.maxWidth = maxWidth;
	}

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {
		gc.setFill(color);
		gc.fillRect(posX, posY, this.value.doubleValue()*5, this.height);
		gc.setStroke(color);
		gc.strokeRect(posX, posY, this.maxWidth, this.height);
	}

}
