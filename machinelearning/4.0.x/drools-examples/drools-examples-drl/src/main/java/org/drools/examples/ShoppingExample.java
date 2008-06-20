package org.drools.examples;

import java.io.InputStreamReader;

import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.PackageBuilder;
import org.drools.learner.tools.FieldAnnotation;

public class ShoppingExample {

    public static final void main(String[] args) throws Exception {
        final PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( ShoppingExample.class.getResourceAsStream( "Shopping.drl" ) ) );

        final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        final StatefulSession session = ruleBase.newStatefulSession();

        Customer mark = new Customer( "mark",
                                      0 );
//        session.insert( mark );

        Product shoes = new Product( "shoes",
                                     60 );
//        session.insert( shoes );

        Product hat = new Product( "hat",
                                   60 );
//        session.insert( hat );

        session.insert( new Purchase( mark,
                                      shoes ) );
        
//        session.insert( new Purchase( new Customer("gizil", 30),
//                		new Product("skirt", 30) ) );
        
        FactHandle hatPurchaseHandle = session.insert( new Purchase( mark,
                                                                     hat ) );

        session.fireAllRules();

        session.retract( hatPurchaseHandle );
        System.out.println( "Customer mark has returned the hat" );
        session.fireAllRules();
    }
    
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
