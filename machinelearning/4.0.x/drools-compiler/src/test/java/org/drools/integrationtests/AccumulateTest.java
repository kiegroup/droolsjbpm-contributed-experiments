package org.drools.integrationtests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.drools.Cheese;
import org.drools.Cheesery;
import org.drools.FactHandle;
import org.drools.OuterClass;
import org.drools.Person;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.RuntimeDroolsException;
import org.drools.StatefulSession;
import org.drools.common.InternalFactHandle;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.lang.descr.PackageDescr;
import org.drools.rule.Package;

public class AccumulateTest extends TestCase {
    protected RuleBase getRuleBase() throws Exception {

        return RuleBaseFactory.newRuleBase( RuleBase.RETEOO,
                                            null );
    }

    protected RuleBase getRuleBase(final RuleBaseConfiguration config) throws Exception {

        return RuleBaseFactory.newRuleBase( RuleBase.RETEOO,
                                            config );
    }

    private RuleBase loadRuleBase(final Reader reader) throws IOException,
                                                      DroolsParserException,
                                                      Exception {
        final DrlParser parser = new DrlParser();
        final PackageDescr packageDescr = parser.parse( reader );
        if ( parser.hasErrors() ) {
            System.out.println( parser.getErrors() );
            Assert.fail( "Error messages in parser, need to sort this our (or else collect error messages)" );
        }
        // pre build the package
        final PackageBuilder builder = new PackageBuilder();
        builder.addPackage( packageDescr );
        final Package pkg = builder.getPackage();

        // add the package to a rulebase
        RuleBase ruleBase = getRuleBase();
        ruleBase.addPackage( pkg );

        ruleBase = serializeRuleBase( ruleBase );

        // load up the rulebase
        return ruleBase;
    }

    /**
     * @param ruleBase
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private RuleBase serializeRuleBase(RuleBase ruleBase) throws IOException,
                                                         ClassNotFoundException {
        byte[] serializedRuleBase = serializeOut( ruleBase );
        ruleBase = (RuleBase) serializeIn( serializedRuleBase );
        return ruleBase;
    }

    /**
     * @param ruleBase
     * @param wm
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private StatefulSession serializeWorkingMemory(RuleBase ruleBase,
                                                   StatefulSession wm) throws IOException,
                                                                      ClassNotFoundException {
        byte[] serializedSession = serializeOut( wm );
        wm.dispose();
        wm = ruleBase.newStatefulSession( new ByteArrayInputStream( serializedSession ) );
        return wm;
    }

    /**
     * @param wm
     * @param handles
     */
    private void updateHandles(StatefulSession wm,
                               final FactHandle[] handles) {
        for ( int i = 0; i < handles.length; i++ ) {
            handles[i] = updateHandle( wm,
                                       (InternalFactHandle) handles[i] );
        }
    }

    /**
     * @param wm
     * @param cheeseHandles
     * @param index
     */
    private InternalFactHandle updateHandle(final StatefulSession wm,
                                            final InternalFactHandle handle) {
        for ( Iterator it = wm.iterateFactHandles(); it.hasNext(); ) {
            InternalFactHandle newHandle = (InternalFactHandle) it.next();
            if ( handle.getId() == newHandle.getId() ) {
                return newHandle;
            }
        }
        return null;
    }

    public void testAccumulateModify() throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateModify.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            5 ), new Cheese( "brie",
                                                                                                             15 ), new Cheese( "brie",
                                                                                                                               16 ), new Cheese( "provolone",
                                                                                                                                                 8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final FactHandle[] cheeseHandles = new FactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = wm.insert( cheese[i] );
        }
        FactHandle bobHandle = wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 24,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 31,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testAccumulate() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_Accumulate.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        wm.insert( new Person( "Bob",
                               "stilton",
                               20 ) );
        wm.insert( new Person( "Mark",
                               "provolone" ) );
        wm.insert( new Cheese( "stilton",
                               10 ) );
        wm.insert( new Cheese( "brie",
                               5 ) );
        wm.insert( new Cheese( "provolone",
                               150 ) );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );

        wm.fireAllRules();

        Assert.assertEquals( new Integer( 165 ),
                             results.get( 0 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results.get( 1 ) );
        Assert.assertEquals( new Integer( 150 ),
                             results.get( 2 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results.get( 3 ) );
        Assert.assertEquals( new Integer( 210 ),
                             results.get( 4 ) );
    }

    public void testMVELAccumulate() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateMVEL.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        wm.insert( new Person( "Bob",
                               "stilton",
                               20 ) );
        wm.insert( new Person( "Mark",
                               "provolone" ) );
        wm.insert( new Cheese( "stilton",
                               10 ) );
        wm.insert( new Cheese( "brie",
                               5 ) );
        wm.insert( new Cheese( "provolone",
                               150 ) );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );

        wm.fireAllRules();

        Assert.assertEquals( new Integer( 165 ),
                             results.get( 0 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results.get( 1 ) );
        Assert.assertEquals( new Integer( 150 ),
                             results.get( 2 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results.get( 3 ) );
        Assert.assertEquals( new Integer( 210 ),
                             results.get( 4 ) );
    }

    public void testAccumulateModifyMVEL() throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateModifyMVEL.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            5 ), new Cheese( "brie",
                                                                                                             15 ), new Cheese( "brie",
                                                                                                                               16 ), new Cheese( "provolone",
                                                                                                                                                 8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final FactHandle[] cheeseHandles = new FactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = wm.insert( cheese[i] );
        }
        FactHandle bobHandle = wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 24,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 31,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testAccumulateReverseModify() throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateReverseModify.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            5 ), new Cheese( "brie",
                                                                                                             15 ), new Cheese( "brie",
                                                                                                                               16 ), new Cheese( "provolone",
                                                                                                                                                 8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final FactHandle[] cheeseHandles = new FactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = wm.insert( cheese[i] );
        }
        FactHandle bobHandle = wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 24,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 31,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testAccumulateReverseModifyMVEL() throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateReverseModifyMVEL.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            5 ), new Cheese( "brie",
                                                                                                             15 ), new Cheese( "brie",
                                                                                                                               16 ), new Cheese( "provolone",
                                                                                                                                                 8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final FactHandle[] cheeseHandles = new FactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = wm.insert( cheese[i] );
        }
        FactHandle bobHandle = wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 24,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 31,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testAccumulateWithFromChaining() throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateWithFromChaining.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         8 ), new Cheese( "stilton",
                                                                          10 ), new Cheese( "stilton",
                                                                                            9 ), new Cheese( "brie",
                                                                                                             4 ), new Cheese( "brie",
                                                                                                                              1 ), new Cheese( "provolone",
                                                                                                                                               8 )};

        Cheesery cheesery = new Cheesery();

        for ( int i = 0; i < cheese.length; i++ ) {
            cheesery.addCheese( cheese[i] );
        }

        FactHandle cheeseryHandle = wm.insert( cheesery );

        final Person bob = new Person( "Bob",
                                       "stilton" );

        FactHandle bobHandle = wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        cheeseryHandle = updateHandle( wm,
                                       (InternalFactHandle) cheeseryHandle );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // one fire, as per rule constraints
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 3,
                             ((List) results.get( results.size() - 1 )).size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setType( "brie" );
        wm.update( cheeseryHandle,
                   cheesery );
        wm.fireAllRules();

        // no fire
        Assert.assertEquals( 1,
                             results.size() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 3,
                             ((List) results.get( results.size() - 1 )).size() );

        // ---------------- 4th scenario
        cheesery.getCheeses().remove( cheese[3] );
        wm.update( cheeseryHandle,
                   cheesery );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testMVELAccumulate2WM() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateMVEL.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm1 = ruleBase.newStatefulSession();
        List results1 = new ArrayList();

        wm1.setGlobal( "results",
                       results1 );

        StatefulSession wm2 = ruleBase.newStatefulSession();
        List results2 = new ArrayList();

        wm2.setGlobal( "results",
                       results2 );

        wm1.insert( new Person( "Bob",
                                "stilton",
                                20 ) );
        wm1.insert( new Person( "Mark",
                                "provolone" ) );

        wm2.insert( new Person( "Bob",
                                "stilton",
                                20 ) );
        wm2.insert( new Person( "Mark",
                                "provolone" ) );

        wm1.insert( new Cheese( "stilton",
                                10 ) );
        wm1.insert( new Cheese( "brie",
                                5 ) );
        wm2.insert( new Cheese( "stilton",
                                10 ) );
        wm1.insert( new Cheese( "provolone",
                                150 ) );
        wm2.insert( new Cheese( "brie",
                                5 ) );
        wm2.insert( new Cheese( "provolone",
                                150 ) );
        
        ruleBase = serializeRuleBase( ruleBase );
        wm1 = serializeWorkingMemory( ruleBase,
                                      wm1 );
        results1 = (List) wm1.getGlobal( "results" );
        wm2 = serializeWorkingMemory( ruleBase,
                                      wm2 );
        results2 = (List) wm2.getGlobal( "results" );

        wm1.fireAllRules();

        wm2.fireAllRules();

        Assert.assertEquals( new Integer( 165 ),
                             results1.get( 0 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results1.get( 1 ) );
        Assert.assertEquals( new Integer( 150 ),
                             results1.get( 2 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results1.get( 3 ) );
        Assert.assertEquals( new Integer( 210 ),
                             results1.get( 4 ) );

        Assert.assertEquals( new Integer( 165 ),
                             results2.get( 0 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results2.get( 1 ) );
        Assert.assertEquals( new Integer( 150 ),
                             results2.get( 2 ) );
        Assert.assertEquals( new Integer( 10 ),
                             results2.get( 3 ) );
        Assert.assertEquals( new Integer( 210 ),
                             results2.get( 4 ) );
    }

    public void testAccumulateInnerClass() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateInnerClass.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        wm.insert( new OuterClass.InnerClass( 10 ) );
        wm.insert( new OuterClass.InnerClass( 5 ) );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        wm.fireAllRules();

        Assert.assertEquals( new Integer( 15 ),
                             results.get( 0 ) );
    }

    public void testAccumulateReturningNull() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateReturningNull.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        try {
            wm.insert( new Cheese( "stilton",
                                   10 ) );

            fail( "Should have raised an exception because accumulate is returning null" );
        } catch ( RuntimeDroolsException rde ) {
            // success, working fine
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Should have raised a DroolsRuntimeException instead of " + e );
        }

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        wm.fireAllRules();
        
    }

    public void testAccumulateReturningNullMVEL() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateReturningNullMVEL.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        try {
            wm.insert( new Cheese( "stilton",
                                   10 ) );

            fail( "Should have raised an exception because accumulate is returning null" );
        } catch ( RuntimeDroolsException rde ) {
            // success, working fine
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Should have raised a DroolsRuntimeException instead of " + e );
        }

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        wm.fireAllRules();
    }

    public void testAccumulateSumJava() throws Exception {
        execTestAccumulateSum( "test_AccumulateSum.drl" );
    }

    public void testAccumulateSumMVEL() throws Exception {
        execTestAccumulateSum( "test_AccumulateSumMVEL.drl" );
    }

    public void testAccumulateMultiPatternWithFunctionJava() throws Exception {
        execTestAccumulateSum( "test_AccumulateMultiPatternFunctionJava.drl" );
    }

    public void testAccumulateMultiPatternWithFunctionMVEL() throws Exception {
        execTestAccumulateSum( "test_AccumulateMultiPatternFunctionMVEL.drl" );
    }

    public void testAccumulateCountJava() throws Exception {
        execTestAccumulateCount( "test_AccumulateCount.drl" );
    }

    public void testAccumulateCountMVEL() throws Exception {
        execTestAccumulateCount( "test_AccumulateCountMVEL.drl" );
    }

    public void testAccumulateAverageJava() throws Exception {
        execTestAccumulateAverage( "test_AccumulateAverage.drl" );
    }

    public void testAccumulateAverageMVEL() throws Exception {
        execTestAccumulateAverage( "test_AccumulateAverageMVEL.drl" );
    }

    public void testAccumulateMinJava() throws Exception {
        execTestAccumulateMin( "test_AccumulateMin.drl" );
    }

    public void testAccumulateMinMVEL() throws Exception {
        execTestAccumulateMin( "test_AccumulateMinMVEL.drl" );
    }

    public void testAccumulateMaxJava() throws Exception {
        execTestAccumulateMax( "test_AccumulateMax.drl" );
    }

    public void testAccumulateMaxMVEL() throws Exception {
        execTestAccumulateMax( "test_AccumulateMaxMVEL.drl" );
    }

    public void testAccumulateMultiPatternJava() throws Exception {
        execTestAccumulateReverseModifyMultiPattern( "test_AccumulateMultiPattern.drl" );
    }

    public void testAccumulateMultiPatternMVEL() throws Exception {
        execTestAccumulateReverseModifyMultiPattern( "test_AccumulateMultiPatternMVEL.drl" );
    }

    public void execTestAccumulateSum(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         8 ), new Cheese( "stilton",
                                                                          10 ), new Cheese( "stilton",
                                                                                            9 ), new Cheese( "brie",
                                                                                                             11 ), new Cheese( "brie",
                                                                                                                               4 ), new Cheese( "provolone",
                                                                                                                                                8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 27,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 3 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 20,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        Assert.assertEquals( 3,
                             results.size() );
        Assert.assertEquals( 15,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 4th scenario
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 3,
                             results.size() );

    }

    public void execTestAccumulateCount(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         8 ), new Cheese( "stilton",
                                                                          10 ), new Cheese( "stilton",
                                                                                            9 ), new Cheese( "brie",
                                                                                                             4 ), new Cheese( "brie",
                                                                                                                              1 ), new Cheese( "provolone",
                                                                                                                                               8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 3,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 3 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 3,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 3,
                             results.size() );
        Assert.assertEquals( 2,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 3,
                             results.size() );

    }

    public void execTestAccumulateAverage(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            11 ), new Cheese( "brie",
                                                                                                              15 ), new Cheese( "brie",
                                                                                                                                17 ), new Cheese( "provolone",
                                                                                                                                                  8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 10,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 16,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.retract( cheeseHandles[4] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void execTestAccumulateMin(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         8 ), new Cheese( "stilton",
                                                                          10 ), new Cheese( "stilton",
                                                                                            9 ), new Cheese( "brie",
                                                                                                             4 ), new Cheese( "brie",
                                                                                                                              1 ), new Cheese( "provolone",
                                                                                                                                               8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 3 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 3,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 1,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.retract( cheeseHandles[4] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void execTestAccumulateMax(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         4 ), new Cheese( "stilton",
                                                                          2 ), new Cheese( "stilton",
                                                                                           3 ), new Cheese( "brie",
                                                                                                            15 ), new Cheese( "brie",
                                                                                                                              17 ), new Cheese( "provolone",
                                                                                                                                                8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 9,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 17,
                             ((Number) results.get( results.size() - 1 )).intValue() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.retract( cheeseHandles[4] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void execTestAccumulateReverseModifyMultiPattern(String fileName) throws Exception {
        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( fileName ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        final Cheese[] cheese = new Cheese[]{new Cheese( "stilton",
                                                         10 ), new Cheese( "stilton",
                                                                           2 ), new Cheese( "stilton",
                                                                                            5 ), new Cheese( "brie",
                                                                                                             15 ), new Cheese( "brie",
                                                                                                                               16 ), new Cheese( "provolone",
                                                                                                                                                 8 )};
        final Person bob = new Person( "Bob",
                                       "stilton" );
        final Person mark = new Person( "Mark",
                                        "provolone" );

        final InternalFactHandle[] cheeseHandles = new InternalFactHandle[cheese.length];
        for ( int i = 0; i < cheese.length; i++ ) {
            cheeseHandles[i] = (InternalFactHandle) wm.insert( cheese[i] );
        }
        InternalFactHandle bobHandle = (InternalFactHandle) wm.insert( bob );
        InternalFactHandle markHandle = (InternalFactHandle) wm.insert( mark );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );

        results = (List) wm.getGlobal( "results" );
        updateHandles( wm,
                       cheeseHandles );
        bobHandle = updateHandle( wm,
                                  (InternalFactHandle) bobHandle );

        // ---------------- 1st scenario
        wm.fireAllRules();
        // no fire, as per rule constraints
        Assert.assertEquals( 0,
                             results.size() );

        // ---------------- 2nd scenario
        final int index = 1;
        cheese[index].setPrice( 9 );
        wm.update( cheeseHandles[index],
                   cheese[index] );
        wm.fireAllRules();

        // 1 fire
        Assert.assertEquals( 1,
                             results.size() );
        Assert.assertEquals( 32,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 3rd scenario
        bob.setLikes( "brie" );
        wm.update( bobHandle,
                   bob );
        wm.fireAllRules();

        // 2 fires
        Assert.assertEquals( 2,
                             results.size() );
        Assert.assertEquals( 39,
                             ((Cheesery) results.get( results.size() - 1 )).getTotalAmount() );

        // ---------------- 4th scenario
        wm.retract( cheeseHandles[3] );
        wm.fireAllRules();

        // should not have fired as per constraint
        Assert.assertEquals( 2,
                             results.size() );

    }

    public void testAccumulateWithPreviouslyBoundVariables() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulatePreviousBinds.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );

        wm.insert( new Cheese( "stilton",
                               10 ) );
        wm.insert( new Cheese( "brie",
                               5 ) );
        wm.insert( new Cheese( "provolone",
                               150 ) );
        wm.insert( new Cheese( "brie",
                               20 ) );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );

        wm.fireAllRules();

        assertEquals( 1,
                      results.size() );
        assertEquals( new Integer( 45 ),
                      results.get( 0 ) );
    }

    public void testAccumulateGlobals() throws Exception {

        // read in the source
        final Reader reader = new InputStreamReader( getClass().getResourceAsStream( "test_AccumulateGlobals.drl" ) );
        RuleBase ruleBase = loadRuleBase( reader );

        StatefulSession wm = ruleBase.newStatefulSession();
        List results = new ArrayList();

        wm.setGlobal( "results",
                      results );
        wm.setGlobal( "globalValue",
                      new Integer( 50 ) );

        wm.insert( new Cheese( "stilton",
                               10 ) );
        wm.insert( new Cheese( "brie",
                               5 ) );
        wm.insert( new Cheese( "provolone",
                               150 ) );
        wm.insert( new Cheese( "brie",
                               20 ) );

        ruleBase = serializeRuleBase( ruleBase );
        wm = serializeWorkingMemory( ruleBase,
                                     wm );
        results = (List) wm.getGlobal( "results" );

        wm.fireAllRules();

        assertEquals( 1,
                      results.size() );
        assertEquals( new Integer( 100 ),
                      results.get( 0 ) );
    }

    protected Object serializeIn(final byte[] bytes) throws IOException,
                                                    ClassNotFoundException {
        final ObjectInput in = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
        final Object obj = in.readObject();
        in.close();
        return obj;
    }

    protected byte[] serializeOut(final Object obj) throws IOException {
        // Serialize to a byte array
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutput out = new ObjectOutputStream( bos );
        out.writeObject( obj );
        out.close();

        // Get the bytes of the serialized object
        final byte[] bytes = bos.toByteArray();
        return bytes;
    }

}
