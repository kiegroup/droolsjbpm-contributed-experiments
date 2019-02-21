package org.kiegroup.zenithr.drools;

import java.util.Date;
import java.util.Objects;

public class FactField {
    private String name;
    private String stringValue;
    private Boolean booleanValue;
    private Integer intValue;
    private Long longValue;
    private Double doubleValue;
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

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
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
        return Objects.equals(name, factField.name) &&
                Objects.equals(stringValue, factField.stringValue) &&
                Objects.equals(booleanValue, factField.booleanValue) &&
                Objects.equals(intValue, factField.intValue) &&
                Objects.equals(longValue, factField.longValue) &&
                Objects.equals(doubleValue, factField.doubleValue) &&
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
