package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HealthBar {
	
	private DoubleProperty value;
	private Color color;
	private int posX;
	private int posY;
	
	public HealthBar (DoubleProperty value, Color color, int posX, int posY)
	{
		this.value = value;
		this.color = color;
		this.posX = posX;
		this.posY = posY;
	}

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {
		gc.setFill(color);
		gc.fillRoundRect(posX, posY, this.value.doubleValue()*5, 10, 5, 5);
	}

}
