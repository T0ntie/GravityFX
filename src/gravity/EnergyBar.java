package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EnergyBar {
	
	private DoubleProperty value;
	private Color color;
	private int posX;
	private int posY;
	private int height;
	
	public EnergyBar (DoubleProperty value, Color color, int posX, int posY, int height)
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
