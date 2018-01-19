package gravity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

public class Explosion {

	final static Image[] explosionImg = new Image[20];
	private final long timeOfBirth;

	public Explosion(long timeOfBirth) {
		for (int i = 0; i < 20; i++) {
			explosionImg[i] = new Image("explosion" + i + ".png");
		}
		this.timeOfBirth = timeOfBirth;
	}

	int lastIndex = 0;

	public void show(GraphicsContext gc, long timestamp, double x, double y) {
			double secondsLiving = (timestamp - timeOfBirth) / 1_000_000_000.0;
			System.out.println("seconds living" + secondsLiving /0.04);
			//lastIndex = Math.max((int) ((secondsLiving /0.03)), lastIndex);
			int index = (int) (secondsLiving/0.04);
			System.out.println(index);
			if (index < 20)
			{
				Affine a = new Affine();
				a.appendTranslation(-explosionImg[index].getWidth() / 2, -explosionImg[index].getHeight() / 2);
				gc.save();
				gc.setTransform(a);
				gc.drawImage(explosionImg[index], x, y);
				gc.restore();
			}
		}

}
