package org.drools.examples.learner;

import org.drools.learner.tools.FieldAnnotation;

public class ShoppingClasses {
	   public static class Customer {
	        private String name;
	        
	        private int discount;
	        
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

	        public int getDiscount() {
	            return discount;
	        }

	        public boolean isEligible() {
				return eligible;
			}

			public void setEligible(boolean eligible) {
				this.eligible = eligible;
			}

			public void setDiscount(int discount) {
	            this.discount = discount;
	        }
	                       
	    }
	    
	    
	    public static class Person{
	    	private Dress skirt;
	    	// attribute 1
	    	private double salary;
	    	
	    }
	   
	    // target object
	    public static class Dress{
	    	double color;
	    	// target attribute
	    	String value;//classification valuable, normal, cheap
	    }
	    
	    public static class Discount {
	        private Customer customer;
	        private int amount;

	        public Discount(Customer customer,
	                        int amount) {
	            this.customer = customer;
	            this.amount = amount;
	        }    
	        
	        public Customer getCustomer() {
	            return customer;
	        }

	        public int getAmount() {
	            return amount;
	        }
	        
	    }

	    public static class Product {
	        private String name;
	        private float price;
	        
	        public Product(String name,
	                       float price) {
	            this.name = name;
	            this.price = price;
	        }
	        
	        public String getName() {
	            return name;
	        }
	        
	        public float getPrice() {
	            return price;
	        }
	        
	        
	    }
	    
	    public static class Purchase {
	        private Customer customer;
	        private Product product;
	        
	        public Purchase(Customer customer,
	                        Product product) {
	            this.customer = customer;
	            this.product = product;
	        }
	        
	        public Customer getCustomer() {
	            return customer;
	        }
	        
	        public Product getProduct() {
	            return product;
	        }            
	    }   
}
