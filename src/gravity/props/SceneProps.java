package gravity.props;

public class SceneProps {
	
	private String sonne;
	private String wind;
	
	public SceneProps(String sonne, String wind)
	{
		this.sonne = sonne;
		this.wind = wind;
	}
	
	public String getSonne() {
		return sonne;
	}
	public void setSonne(String sonne) {
		this.sonne = sonne;
	}
	public String getWind() {
		return wind;
	}
	public void setWind(String wind) {
		this.wind = wind;
	}

}
