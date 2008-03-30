package examples;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import dt.memory.DomainSpec;

public class Car implements Externalizable{
	@DomainSpec(readingSeq = 0)
	private String buying;		//"vhigh", "high", "med", "low"
	@DomainSpec(readingSeq = 1)
	private String maint;		//"vhigh", "high", "med", "low"
	@DomainSpec(readingSeq = 2)
	private String doors;		//"2", "3", "4", "5more"
	@DomainSpec(readingSeq = 3)
	private String persons;		//"2", "4", "more"
	@DomainSpec(readingSeq = 4)
	private String lug_boot;	//"small", "med", "big"
	@DomainSpec(readingSeq = 5)
	private String safety; 		//"low", "med", "high"
	@DomainSpec(readingSeq = 6, target = true)
	private String target; 		//"unacc", "acc", "good", "vgood"
	
	
	public Car() {

	}
	public String getBuying() {
		return buying;
	}

	//@DomainSpec(readingSeq = 0)
	public void setBuying(String buying) {
		this.buying = buying;
	}

	public String getMaint() {
		return maint;
	}
	//@DomainSpec(readingSeq =1)
	public void setMaint(String maint) {
		this.maint = maint;
	}

	public String getDoors() {
		return doors;
	}
	//@DomainSpec(readingSeq =2)
	public void setDoors(String doors) {
		this.doors = doors;
	}

	public String getPersons() {
		return persons;
	}
	//@DomainSpec(readingSeq =3)
	public void setPersons(String persons) {
		this.persons = persons;
	}

	public String getLug_boot() {
		return lug_boot;
	}
	//@DomainSpec(readingSeq =4)
	public void setLug_boot(String lug_boot) {
		this.lug_boot = lug_boot;
	}

	public String getSafety() {
		return safety;
	}
	//@DomainSpec(readingSeq =5)
	public void setSafety(String safety) {
		this.safety = safety;
	}
	
	public String getTarget() {
		return target;
	}
	//@DomainSpec(readingSeq =6)
	public void setTarget(String carClass) {
		this.target = carClass;
	}
	
	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(buying);
		out.writeObject(maint);
//		out.writeObject();
//		out.writeObject();
//		out.writeObject();
//		out.writeObject();
//		out.writeObject();			
	}
	
	public String toString() {
		String out = "Car(buy:" +getBuying() + 
						" doors:"+getDoors()+ 
						" lug_boot:"+getLug_boot()+ 
						" maint:"+getMaint()+
						" persons:"+getPersons()+
						" safety:"+getSafety()+
						" target:"+getTarget();
		return out;
	}

	
}