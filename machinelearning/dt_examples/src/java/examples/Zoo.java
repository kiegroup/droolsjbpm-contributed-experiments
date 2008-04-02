package examples;

import dt.memory.DomainSpec;

public class Zoo {
	/*
	   1. animal name:      Unique for each instance
	   2. hair		Boolean
	   3. feathers		Boolean
	   4. eggs		Boolean
	   5. milk		Boolean
	   6. airborne		Boolean
	   7. aquatic		Boolean
	   8. predator		Boolean
	   9. toothed		Boolean
	  10. backbone		Boolean
	  11. breathes		Boolean
	  12. venomous		Boolean
	  13. fins		Boolean
	  14. legs		Numeric (set of values: {0,2,4,5,6,8})
	  15. tail		Boolean
	  16. domestic		Boolean
	  17. catsize		Boolean
	  18. type		Numeric (integer values in range [1,7])
	  */
	@DomainSpec(readingSeq = 0, ignore= true)
	private String animal_name;	//      Unique for each instance
	@DomainSpec(readingSeq = 1)
	private boolean hair;		//		Boolean
	@DomainSpec(readingSeq = 2)
	private boolean feathers;	//		Boolean
	@DomainSpec(readingSeq = 3)
	private boolean eggs;		//		Boolean
	@DomainSpec(readingSeq = 4)
	private boolean milk;		//		Boolean
	@DomainSpec(readingSeq = 5)
	private boolean airborne; 	//		Boolean
	@DomainSpec(readingSeq = 6)
	private boolean aquatic;	//		Boolean
	@DomainSpec(readingSeq = 7)
	private boolean predator;	//		Boolean
	@DomainSpec(readingSeq = 8)
	private boolean toothed;	//		Boolean
	@DomainSpec(readingSeq = 9)
	private boolean backbone;	//		Boolean
	@DomainSpec(readingSeq = 10)
	private boolean breathes;	//		Boolean
	@DomainSpec(readingSeq = 11)
	private boolean venomous;	//		Boolean
	@DomainSpec(readingSeq = 12)
	private boolean fins;		//		Boolean
	@DomainSpec(readingSeq = 13, discrete = false, values = {"0","2","4","5","6","8"})
	private int legs;			//		Numeric (set of values: {0,2,4,5,6,8})
	@DomainSpec(readingSeq = 14)
	private boolean tail;		//		Boolean
	@DomainSpec(readingSeq = 15)
	private boolean domestic;	//		Boolean
	@DomainSpec(readingSeq = 16)
	private boolean vcatsize;	//		Boolean
	@DomainSpec(readingSeq = 17, target = true)
	private int type;			//		Numeric (integer values in range [1,7])
	
	public String getAnimal_name() {
		return animal_name;
	}
	public void setAnimal_name(String animal_name) {
		this.animal_name = animal_name;
	}
	public boolean isHair() {
		return hair;
	}
	public void setHair(boolean hair) {
		this.hair = hair;
	}
	public boolean isFeathers() {
		return feathers;
	}
	public void setFeathers(boolean feathers) {
		this.feathers = feathers;
	}
	public boolean isEggs() {
		return eggs;
	}
	public void setEggs(boolean eggs) {
		this.eggs = eggs;
	}
	public boolean isMilk() {
		return milk;
	}
	public void setMilk(boolean milk) {
		this.milk = milk;
	}
	public boolean isAirborne() {
		return airborne;
	}
	public void setAirborne(boolean airborne) {
		this.airborne = airborne;
	}
	public boolean isAquatic() {
		return aquatic;
	}
	public void setAquatic(boolean aquatic) {
		this.aquatic = aquatic;
	}
	public boolean isPredator() {
		return predator;
	}
	public void setPredator(boolean predator) {
		this.predator = predator;
	}
	public boolean isToothed() {
		return toothed;
	}
	public void setToothed(boolean toothed) {
		this.toothed = toothed;
	}
	public boolean isBackbone() {
		return backbone;
	}
	public void setBackbone(boolean backbone) {
		this.backbone = backbone;
	}
	public boolean isBreathes() {
		return breathes;
	}
	public void setBreathes(boolean breathes) {
		this.breathes = breathes;
	}
	public boolean isVenomous() {
		return venomous;
	}
	public void setVenomous(boolean venomous) {
		this.venomous = venomous;
	}
	public boolean isFins() {
		return fins;
	}
	public void setFins(boolean fins) {
		this.fins = fins;
	}
	public int getLegs() {
		return legs;
	}
	public void setLegs(int legs) {
		this.legs = legs;
	}
	public boolean isTail() {
		return tail;
	}
	public void setTail(boolean tail) {
		this.tail = tail;
	}
	public boolean isDomestic() {
		return domestic;
	}
	public void setDomestic(boolean domestic) {
		this.domestic = domestic;
	}
	public boolean isVcatsize() {
		return vcatsize;
	}
	public void setVcatsize(boolean vcatsize) {
		this.vcatsize = vcatsize;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}


}
