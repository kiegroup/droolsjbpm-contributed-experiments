package org.drools.learner;

import org.drools.learner.tools.FieldAnnotation;

public class Car {
    private String model;
    private int doors;
    private String size;
    private String fuel;

    @FieldAnnotation(target = true)
    private String value;

    public Car(String model, int doors, String size, String fuel, String value) {
        this.model = model;
        this.doors = doors;
        this.size = size;
        this.fuel = fuel;
        this.value = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getDoors() {
        return doors;
    }

    public void setDoors(int doors) {
        this.doors = doors;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Car car = (Car) o;

        if (doors != car.doors) {
            return false;
        }
        if (!model.equals(car.model)) {
            return false;
        }
        if (!size.equals(car.size)) {
            return false;
        }
        if (!fuel.equals(car.fuel)) {
            return false;
        }
        return value.equals(car.value);
    }

    @Override public int hashCode() {
        int result = model.hashCode();
        result = 31 * result + doors;
        result = 31 * result + size.hashCode();
        result = 31 * result + fuel.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
