package examples;

import id3.ReadingSeq;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Car implements Externalizable{

	private String buying;		//"vhigh", "high", "med", "low"
	private String maint;		//"vhigh", "high", "med", "low"
	private String doors;		//"2", "3", "4", "5more"
	private String persons;		//"2", "4", "more"
	private String lug_boot;	//"small", "med", "big"
	private String safety; 		//"low", "med", "high"
	private String target; 		//"unacc", "acc", "good", "vgood"
	
	
	public Car() {

	}
	public String getBuying() {
		return buying;
	}

	@ReadingSeq(0)
	public void setBuying(String buying) {
		this.buying = buying;
	}

	public String getMaint() {
		return maint;
	}
	@ReadingSeq(1)
	public void setMaint(String maint) {
		this.maint = maint;
	}

	public String getDoors() {
		return doors;
	}
	@ReadingSeq(2)
	public void setDoors(String doors) {
		this.doors = doors;
	}

	public String getPersons() {
		return persons;
	}
	@ReadingSeq(3)
	public void setPersons(String persons) {
		this.persons = persons;
	}

	public String getLug_boot() {
		return lug_boot;
	}
	@ReadingSeq(4)
	public void setLug_boot(String lug_boot) {
		this.lug_boot = lug_boot;
	}

	public String getSafety() {
		return safety;
	}
	@ReadingSeq(5)
	public void setSafety(String safety) {
		this.safety = safety;
	}
	
	public String getTarget() {
		return target;
	}
	@ReadingSeq(6)
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
						" doors"+getDoors()+ 
						" lug_boot"+getLug_boot()+ 
						" maint"+getMaint()+
						" persons"+getPersons()+
						" safety"+getSafety()+
						" target"+getTarget();
		return out;
	}

	
}