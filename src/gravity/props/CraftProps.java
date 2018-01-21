package gravity.props;

public class CraftProps {
	
	private double fireRate;
	private double firePower;
	private double fireImpact;
	
	public CraftProps(double fireRate, double firePower, double fireImpact) {
		super();
		this.fireRate = fireRate;
		this.firePower = firePower;
		this.fireImpact = fireImpact;
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
}
