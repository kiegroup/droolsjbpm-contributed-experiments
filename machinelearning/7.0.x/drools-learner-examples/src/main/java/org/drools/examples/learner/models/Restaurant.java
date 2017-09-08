package org.drools.examples.learner.models;

import org.drools.learner.tools.FieldAnnotation;

//import dt.memory.DomainSpec;

public class Restaurant {

    private boolean alternate;    //yesno
    private boolean bar;        //yesno
    private boolean friSat;    //yesno
    private boolean hungry;        //yesno
    private String  patrons;    //String[]{"None","Some","Full"});
    private int     price;        //",new String[]{"$","$$","$$$"});
    private boolean raining;    //yesno
    private boolean reservation; //yesno
    private String  type;        //",new String[]{"French","Italian","Thai","Burger"});
    private String  waitEstimate;    //",new String[]{"0-10","10-30","30-60",">60"});
    @FieldAnnotation(target = true, readingSeq = 0)
    private boolean willWait;    //yesno
    
    public Restaurant() {}

    public boolean getAlternate() {
        return alternate;
    }

    public void setAlternate(boolean alternate) {
        this.alternate = alternate;
    }

    public boolean getBar() {
        return bar;
    }

    public void setBar(boolean bar) {
        this.bar = bar;
    }

    public boolean getFriSat() {
        return friSat;
    }

    public void setFriSat(boolean fri_sat) {
        this.friSat = fri_sat;
    }

    public boolean getHungry() {
        return hungry;
    }

    public void setHungry(boolean hungry) {
        this.hungry = hungry;
    }

    public String getPatrons() {
        return patrons;
    }

    public void setPatrons(String patrons) {
        this.patrons = patrons;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean getRaining() {
        return raining;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    public boolean getReservation() {
        return reservation;
    }

    public void setReservation(boolean reservation) {
        this.reservation = reservation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWaitEstimate() {
        return waitEstimate;
    }

    public void setWaitEstimate(String waitEstimate) {
        this.waitEstimate = waitEstimate;
    }

    public boolean getWillWait() {
        return willWait;
    }

    public void setWillWait(boolean willWait) {
        this.willWait = willWait;
    }

    @Override
    public String toString() {
        return "Restaurant [alternate=" + alternate + ", bar=" + bar + ", friSat=" + friSat + ", hungry=" + hungry + ", patrons=" + patrons + ", price=" + price + ", raining=" + raining + ", reservation=" + reservation + ", type=" + type + ", waitEstimate=" + waitEstimate + ", willWait=" + willWait + "]";
    }
    
    
}