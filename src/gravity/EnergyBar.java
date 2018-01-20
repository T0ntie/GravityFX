package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EnergyBar {
	
	private DoubleProperty value;
	private Color color;
	private double posX;
	private double posY;
	private double height;
	
	public EnergyBar (DoubleProperty value, Color color, double posX, double posY, double height)
	{
		this.value = value;
		this.color = color;
		this.posX = posX;
		this.posY = posY;
		this.height = height;
	}

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {
		gc.setFill(color);
		gc.fillRect(posX, posY, this.value.doubleValue()*5, this.height);
	}

}
