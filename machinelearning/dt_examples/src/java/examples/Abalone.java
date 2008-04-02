package examples;

public class Abalone {
	/*
	Sex		nominal			M, F, and I (infant)
	Length		continuous	mm	Longest shell measurement
	Diameter	continuous	mm	perpendicular to length
	Height		continuous	mm	with meat in shell
	Whole weight	continuous	grams	whole abalone
	Shucked weight	continuous	grams	weight of meat
	Viscera weight	continuous	grams	gut weight (after bleeding)
	Shell weight	continuous	grams	after being dried
	Rings		integer			+1.5 gives the age in years
	*/
	
	private String sex; 			//	nominal			M, F, and I (infant)
	private double length;			//	continuous	mm	Longest shell measurement
	private double diameter;		//	continuous	mm	perpendicular to length
	private double height;			//	continuous	mm	with meat in shell
	private double whole_weight;	//	continuous	grams	whole abalone
	private double shucked_weight;	//	continuous	grams	weight of meat
	private double viscera_weight;	//	continuous	grams	gut weight (after bleeding)
	private double shell_weight;	//	continuous	grams	after being dried
	private int rings;				//	integer			+1.5 gives the age in years

	private int target;

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWhole_weight() {
		return whole_weight;
	}

	public void setWhole_weight(double whole_weight) {
		this.whole_weight = whole_weight;
	}

	public double getShucked_weight() {
		return shucked_weight;
	}

	public void setShucked_weight(double shucked_weight) {
		this.shucked_weight = shucked_weight;
	}

	public double getViscera_weight() {
		return viscera_weight;
	}

	public void setViscera_weight(double viscera_weight) {
		this.viscera_weight = viscera_weight;
	}

	public double getShell_weight() {
		return shell_weight;
	}

	public void setShell_weight(double shell_weight) {
		this.shell_weight = shell_weight;
	}

	public int getRings() {
		return rings;
	}

	public void setRings(int rings) {
		this.rings = rings;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}
	
	
	
}
