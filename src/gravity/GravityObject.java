package gravity;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GravityObject {

	protected final double mass;
	protected  double radius; //final
	protected final Circle view;
	
    public GravityObject(double centerX, double centerY, double radius, double mass) {

    	this.view = new Circle(centerX, centerY, radius);
	    this.mass = mass;
	    this.radius = radius;
	    view.setRadius(radius);
	 }

	public void show(GraphicsContext gc, long timestamp, long elapsedTime) {
		 gc.setStroke(Color.RED);
		 gc.strokeLine(getCenterX()-5, getCenterY()-5, getCenterX()+5, getCenterY()+5);
		 gc.strokeLine(getCenterX()-5, getCenterY()+5, getCenterX()+5, getCenterY()-5);
		 gc.setStroke(Color.AQUA);
		 gc.strokeLine(getCenterX(), getCenterY(),getCenterX()+1, getCenterY()+1);
		 gc.setStroke(Color.RED);
		 gc.strokeOval(getCenterX()-radius, getCenterY()-radius, radius*2, radius*2);
	 }

	public double getMass() {
	     return mass;
	 }

	public double getRadius() {
	     return radius;
	 }
	
	public void setRadius(double r)
	{
		this.radius = r;
	}

	public final double getCenterX() {
	     return view.getCenterX();
	 }

	public final void setCenterX(double centerX) {
	     view.setCenterX(centerX);
	 }

	public final DoubleProperty centerXProperty() {
	     return view.centerXProperty();
	 }

	public final double getCenterY() {
	     return view.getCenterY();
	 }

	public final void setCenterY(double centerY) {
	     view.setCenterY(centerY);
	 }

	public final DoubleProperty centerYProperty() {
	     return view.centerYProperty();
	 }

}
