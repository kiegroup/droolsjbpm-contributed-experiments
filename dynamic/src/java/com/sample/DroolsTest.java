package com.sample;

import java.beans.IntrospectionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.rule.Package;

/**
 * This is a sample file to launch a rule package from a rule source file.
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {
            ClassBuilder builder = new ClassBuilder();
            Thread.currentThread().setContextClassLoader( builder.getDynamicClassesClassLoader() );
            Package pkg = new Package( "com.jbpm.action" );
            PackageBuilder pkgBuilder = new PackageBuilder( pkg );
            // Defining OrderLine
            ClassDefinition lineitemClassDef = new ClassDefinition( "com.sample.LineItem" );
            FieldDefinition lineId = new FieldDefinition( "lineId",
                                                          "int" );
            lineId.setKey( true );
            FieldDefinition product = new FieldDefinition( "product",
                                                           "java.lang.String" );
            FieldDefinition amount = new FieldDefinition( "amount",
                                                          "double" );
            FieldDefinition processed = new FieldDefinition( "processed",
                                                             "boolean" );
            lineitemClassDef.addField( lineId );
            lineitemClassDef.addField( product );
            lineitemClassDef.addField( amount );
            lineitemClassDef.addField( processed );
            Class lineItemClass = defineAndLoadClass( pkgBuilder,
                                                      lineitemClassDef,
                                                      builder );
            // Defining the class 
            ClassDefinition classdef = new ClassDefinition( "com.sample.Order" );
            FieldDefinition orderId = new FieldDefinition( "orderId",
                                                           "int" );
            orderId.setKey( true );
            FieldDefinition customer = new FieldDefinition( "customer",
                                                            "com.sample.Customer" );
            FieldDefinition lineItems = new FieldDefinition( "lineItems",
                                                             "java.util.List" );
            FieldDefinition totalAmount = new FieldDefinition( "totalAmount",
                                                               "double" );
            classdef.addField( orderId );
            classdef.addField( customer );
            classdef.addField( lineItems );
            classdef.addField( totalAmount );
            Class orderClass = defineAndLoadClass( pkgBuilder,
                                                   classdef,
                                                   builder );
            //load up the rulebase
            ReteooRuleBase ruleBase = (ReteooRuleBase) readRule( pkgBuilder );
            WorkingMemory workingMemory = ruleBase.newWorkingMemory();
            Object line1 = lineItemClass.newInstance();
            lineId.setValue( line1,
                             1 );
            product.setValue( line1,
                              "cheese" );
            amount.setValue( line1,
                             25.10 );
            Object line2 = lineItemClass.newInstance();
            lineId.setValue( line2,
                             2 );
            product.setValue( line2,
                              "chocolate" );
            amount.setValue( line2,
                             12.50 );
            List items = new ArrayList();
            items.add( line1 );
            items.add( line2 );
            Customer cust = new Customer( 1,
                                          "Fred",
                                          new Integer( 5 ),
                                          new Integer( 25 ),
                                          new Long( 100000 ) );
            // Instantiating the class
            Object order = orderClass.newInstance();
            orderId.setValue( order,
                              10 );
            customer.setValue( order,
                               cust );
            lineItems.setValue( order,
                                items );
            workingMemory.assertObject( order );
            workingMemory.assertObject( cust );
            workingMemory.assertObject( line1 );
            workingMemory.assertObject( line2 );
            workingMemory.fireAllRules();
        } catch ( Exception e ) {
            e.printStackTrace();
        }        

    }

    /**
     * @param classdef
     * @param builder
     * @return
     * @throws IOException
     * @throws IntrospectionException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     * @throws FileNotFoundException
     */
    private static Class defineAndLoadClass(PackageBuilder pkgBuilder,
                                            ClassDefinition classdef,
                                            ClassBuilder builder) throws IOException,
                                                                 IntrospectionException,
                                                                 ClassNotFoundException,
                                                                 NoSuchMethodException,
                                                                 IllegalAccessException,
                                                                 InvocationTargetException,
                                                                 InstantiationException,
                                                                 NoSuchFieldException,
                                                                 FileNotFoundException {
        //        FileOutputStream fos = new FileOutputStream( "generated/"+ classdef.getClassNameAsInternal()+".class" );
        //        fos.write( generatedClass );
        //        fos.close();

        // loading the class
        Class orderClass = builder.buildAndLoadClass( classdef );
        orderClass.getMethods();
        return orderClass;
    }

    /**
     * Please note that this is the "low level" rule assembly API.
     */
    private static RuleBase readRule(PackageBuilder builder) throws Exception {
        //read in the source
        Reader source = new InputStreamReader( DroolsTest.class.getResourceAsStream( "/Shipper.drl" ) );

        builder.addPackageFromDrl( source );

        Package pkg = builder.getPackage();

        //add the package to a rulebase (deploy the rule package).
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        return ruleBase;
    }

}
