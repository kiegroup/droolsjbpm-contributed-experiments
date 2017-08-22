package org.drools.examples.learner.models;

import org.drools.learner.tools.FieldAnnotation;

public class ShoppingClasses {

    public static class Customer {

        private String name;
        @FieldAnnotation(discrete = false)
        private int    discount;

        @FieldAnnotation(target = true)
        private boolean eligible;

        public Customer(String name,
                        int discount) {
            this.name = name;
            this.discount = discount;
        }

        public String getName() {
            return name;
        }

        public void setName(String _name) {
            name = _name;
        }

        public int getDiscount() {
            return discount;
        }

        public void setDiscount(int discount) {
            this.discount = discount;
        }

        public boolean isEligible() {
            return eligible;
        }

        public void setEligible(boolean eligible) {
            this.eligible = eligible;
        }
    }

    public static class Person {

        private Dress  skirt;
        // attribute 1
        private double salary;

        public Dress getSkirt() {
            return skirt;
        }

        public void setSkirt(Dress skirt) {
            this.skirt = skirt;
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }
    }

    // target object
    public static class Dress {

        double color;
        // target attribute
        String value;//classification valuable, normal, cheap

        public double getColor() {
            return color;
        }

        public void setColor(double color) {
            this.color = color;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Discount {

        private Customer customer;
        private int      amount;

        public Discount(Customer customer,
                        int amount) {
            this.customer = customer;
            this.amount = amount;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    public static class Product {

        private String name;
        private float  price;

        public Product(String name,
                       float price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String _name) {
            name = _name;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float _price) {
            price = _price;
        }
    }

    public static class Purchase {

        private Customer customer;
        private Product  product;

        public Purchase(Customer customer,
                        Product product) {
            this.customer = customer;
            this.product = product;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer _customer) {
            customer = _customer;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product _product) {
            product = _product;
        }
    }
}
