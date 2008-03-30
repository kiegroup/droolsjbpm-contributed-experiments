package examples;

import dt.memory.DomainSpec;

public class Golf {
	
	
	@DomainSpec(readingSeq=0, values= {"sunny", "overcast", "rain"})
	private String outlook; //: sunny, overcast, rain.
	@DomainSpec(readingSeq=1, discrete=false)
	private int temperature; //: continuous.
	@DomainSpec(readingSeq=2, discrete=false)
	private int humidity; //: continuous.
	@DomainSpec(readingSeq=3)
	private boolean windy;
	
	@DomainSpec(readingSeq=4, target = true)
	private String decision; //Play, Don't Play.
	
	public Golf() {
		
	}

	public String getOutlook() {
		return outlook;
	}

	public void setOutlook(String outlook) {
		this.outlook = outlook;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public boolean isWindy() {
		return windy;
	}

	public void setWindy(boolean windy) {
		this.windy = windy;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}
	
	

}
