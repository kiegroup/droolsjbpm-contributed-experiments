package org.drools.tutorials.banking;

import java.util.Date;

import org.drools.learner.tools.ClassAnnotation;

@ClassAnnotation(label_element = "getLabel")
public class Cashflow {

    private Date   date;

    private double amount;

    public Cashflow() {
    }

    public Cashflow(Date date,
                    double amount) {
        this.date = date;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean getLabel() {
    	Date deadline = new Date(System.currentTimeMillis());
    	return date.before(deadline) && amount<500.00;
    }
    public String toString() {
        return "Cashflow[date=" + date + ",amount=" + amount + "]";
    }
}
