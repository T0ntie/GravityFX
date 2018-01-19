package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HealthBar {
	
	private DoubleProperty health;
	private int posX;
	private int posY;
	
	public HealthBar (Craft craft, int posX, int posY)
	{
		health = craft.getHealthProperty();
		this.posX = posX;
		this.posY = posY;
	}

	public void show(GraphicsContext gc, long timestamp) {
		gc.setFill(Color.DARKORCHID);
		gc.fillRoundRect(posX, posY, this.health.doubleValue()*5, 10, 5, 5);
	}

}
