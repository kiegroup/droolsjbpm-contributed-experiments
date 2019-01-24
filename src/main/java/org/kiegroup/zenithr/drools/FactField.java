package org.kiegroup.zenithr.drools;

import java.util.Date;
import java.util.Objects;

public class FactField {
    private String name;
    private String stringValue;
    private boolean booleanValue;
    private int intValue;
    private long longValue;
    private double doubleValue;
    private Date dateValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactField factField = (FactField) o;
        return booleanValue == factField.booleanValue &&
                intValue == factField.intValue &&
                longValue == factField.longValue &&
                Double.compare(factField.doubleValue, doubleValue) == 0 &&
                Objects.equals(name, factField.name) &&
                Objects.equals(stringValue, factField.stringValue) &&
                Objects.equals(dateValue, factField.dateValue);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, stringValue, booleanValue, intValue, longValue, doubleValue, dateValue);
    }

    @Override
    public String toString() {
        return "FactField{" +
                "name='" + name + '\'' +
                ", stringValue='" + stringValue + '\'' +
                ", booleanValue=" + booleanValue +
                ", intValue=" + intValue +
                ", longValue=" + longValue +
                ", doubleValue=" + doubleValue +
                ", dateValue=" + dateValue +
                '}';
    }
}
