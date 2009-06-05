package org.drools.examples;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.base.ValueType;
import org.drools.base.evaluators.EvaluatorRegistry;
import org.drools.common.InternalWorkingMemory;
import org.drools.examples.lotrc.evaluators.TowardsEvaluatorDefinition;
import org.drools.examples.lotrc.model.Board;
import org.drools.examples.lotrc.model.Region;
import org.drools.rule.Declaration;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.InternalReadAccessor;
import org.jmock.Expectations;
import org.jmock.Mockery;

public class TowardsEvaluatorTest extends TestCase {

    private Mockery mockery = new Mockery();

    private EvaluatorRegistry registry = new EvaluatorRegistry();
    private Region[][] regions = new Board().getRegions();
    
    @Override
    protected void setUp() throws Exception {
        registry.addEvaluatorDefinition( new TowardsEvaluatorDefinition() );
    }
    
    public void testTowardsShire() {
        final List<Object[]> dataList = new ArrayList<Object[]>();
        for( Region[] row : regions ) {
            for( Region fromRegion : row ) {
                for( Region[] trow : regions ) {
                    for( Region toRegion : trow ) {
                        Object[] tcase = new Object[4];
                        tcase[0] = toRegion;
                        tcase[1] = "towardsShire";
                        tcase[2] = fromRegion;
                        boolean result = (toRegion.getRow() - fromRegion.getRow()) == -1;
                        if( fromRegion.getRow() > 3 ) {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()-1)) );
                        } else {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()+1)) );
                        }
                        tcase[3] = Boolean.valueOf( result );
                        dataList.add( tcase );
                    }
                }
            }
        }
        
        final Object[][] data = new Object[dataList.size()][];
        int i = 0;
        for( Object[] tcase : dataList ) {
            data[i++] = tcase;
        }

        runEvaluatorTest( data,
                          ValueType.OBJECT_TYPE );

    }

    public void testNotTowardsShire() {
        final List<Object[]> dataList = new ArrayList<Object[]>();
        for( Region[] row : regions ) {
            for( Region fromRegion : row ) {
                for( Region[] trow : regions ) {
                    for( Region toRegion : trow ) {
                        Object[] tcase = new Object[4];
                        tcase[0] = toRegion;
                        tcase[1] = "not towardsShire";
                        tcase[2] = fromRegion;
                        boolean result = (toRegion.getRow() - fromRegion.getRow()) == -1;
                        if( fromRegion.getRow() > 3 ) {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()-1)) );
                        } else {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()+1)) );
                        }
                        tcase[3] = Boolean.valueOf( !result );
                        dataList.add( tcase );
                    }
                }
            }
        }
        
        final Object[][] data = new Object[dataList.size()][];
        int i = 0;
        for( Object[] tcase : dataList ) {
            data[i++] = tcase;
        }

        runEvaluatorTest( data,
                          ValueType.OBJECT_TYPE );

    }

    public void testTowardsMordor() {
        final List<Object[]> dataList = new ArrayList<Object[]>();
        for( Region[] row : regions ) {
            for( Region fromRegion : row ) {
                for( Region[] trow : regions ) {
                    for( Region toRegion : trow ) {
                        Object[] tcase = new Object[4];
                        tcase[0] = toRegion;
                        tcase[1] = "towardsMordor";
                        tcase[2] = fromRegion;
                        boolean result = (toRegion.getRow() - fromRegion.getRow()) == +1;
                        if( fromRegion.getRow() < 3 ) {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()-1)) );
                        } else {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()+1)) );
                        }
                        tcase[3] = Boolean.valueOf( result );
                        dataList.add( tcase );
                    }
                }
            }
        }
        
        final Object[][] data = new Object[dataList.size()][];
        int i = 0;
        for( Object[] tcase : dataList ) {
            data[i++] = tcase;
        }

        runEvaluatorTest( data,
                          ValueType.OBJECT_TYPE );

    }

    public void testNotTowardsMordor() {
        final List<Object[]> dataList = new ArrayList<Object[]>();
        for( Region[] row : regions ) {
            for( Region fromRegion : row ) {
                for( Region[] trow : regions ) {
                    for( Region toRegion : trow ) {
                        Object[] tcase = new Object[4];
                        tcase[0] = toRegion;
                        tcase[1] = "not towardsMordor";
                        tcase[2] = fromRegion;
                        boolean result = (toRegion.getRow() - fromRegion.getRow()) == +1;
                        if( fromRegion.getRow() < 3 ) {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()-1)) );
                        } else {
                            result = result && ((fromRegion.getCol()==toRegion.getCol())|| (fromRegion.getCol()==(toRegion.getCol()+1)) );
                        }
                        tcase[3] = Boolean.valueOf( !result );
                        dataList.add( tcase );
                    }
                }
            }
        }
        
        final Object[][] data = new Object[dataList.size()][];
        int i = 0;
        for( Object[] tcase : dataList ) {
            data[i++] = tcase;
        }

        runEvaluatorTest( data,
                          ValueType.OBJECT_TYPE );

    }

    /**
     * Test utility to play the data through the evaluators.
     * @param data The data to try out : Array of {arg1, operator, arg2}
     * @param valueType The Evaluator.**_TYPE to test
     */
    private void runEvaluatorTest(final Object[][] data,
                                  final ValueType valueType) {
        final InternalReadAccessor extractor = mockery.mock( InternalReadAccessor.class );
        for ( int i = 0; i < data.length; i++ ) {
            final Object[] row = data[i];
            mockery.checking( new Expectations() {{
                allowing(extractor).getValue( with( row[0] ) ); will( returnValue( row[0] ) );
                allowing(extractor).getValue( with( row[2] ) ); will( returnValue( row[2] ) );
                allowing(extractor).getValue( with( any( InternalWorkingMemory.class) ), with( row[0] ) ); will( returnValue( row[0] ) );
                allowing(extractor).getValue( with( any( InternalWorkingMemory.class) ), with( row[2] ) ); will( returnValue( row[2] ) );
            }} );
            
            boolean isNegated = ((String) row[1]).startsWith( "not " );
            String evaluatorStr = isNegated ? ((String) row[1]).substring( 4 ) : (String) row[1];
            final Evaluator evaluator = (Evaluator) registry.getEvaluatorDefinition( evaluatorStr ).getEvaluator( valueType,
                                                                                                      evaluatorStr,
                                                                                                      isNegated,
                                                                                                      null );
            checkEvaluatorMethodCachedRight( valueType,
                                             extractor,
                                             row,
                                             evaluator );
            checkEvaluatorMethodCachedLeft( valueType,
                                            extractor,
                                            row,
                                            evaluator );
            checkEvaluatorMethodWith2Extractors( valueType,
                                                 extractor,
                                                 row,
                                                 evaluator );

            assertEquals( valueType,
                          evaluator.getValueType() );

        }
    }

    /**
     * @param valueType
     * @param extractor
     * @param row
     * @param evaluator
     */
    private void checkEvaluatorMethodCachedRight(final ValueType valueType,
                                                 final InternalReadAccessor extractor,
                                                 final Object[] row,
                                                 final Evaluator evaluator) {
        final VariableContextEntry context = this.getContextEntry( evaluator,
                                                                   extractor,
                                                                   valueType,
                                                                   row );
        final boolean result = evaluator.evaluateCachedRight( null,
                                                              context,
                                                              row[2] );
        final String message = "The evaluator type: [" + valueType + "] with CachedRight incorrectly returned " + result + " for [" + row[0] + " " + row[1] + " " + row[2] + "]. It was asserted to return " + row[3];

        if ( row[3] == Boolean.TRUE ) {
            assertTrue( message,
                        result );
        } else {
            assertFalse( message,
                         result );
        }
    }

    /**
     * @param valueType
     * @param extractor
     * @param row
     * @param evaluator
     */
    private void checkEvaluatorMethodCachedLeft(final ValueType valueType,
                                                final InternalReadAccessor extractor,
                                                final Object[] row,
                                                final Evaluator evaluator) {
        final VariableContextEntry context = this.getContextEntry( evaluator,
                                                                   extractor,
                                                                   valueType,
                                                                   row );
        final boolean result = evaluator.evaluateCachedLeft( null,
                                                             context,
                                                             row[0] );
        final String message = "The evaluator type: [" + valueType + "] with CachedLeft incorrectly returned " + result + " for [" + row[0] + " " + row[1] + " " + row[2] + "]. It was asserted to return " + row[3];

        if ( row[3] == Boolean.TRUE ) {
            assertTrue( message,
                        result );
        } else {
            assertFalse( message,
                         result );
        }
    }

    /**
     * @param valueType
     * @param extractor
     * @param row
     * @param evaluator
     */
    private void checkEvaluatorMethodWith2Extractors(final ValueType valueType,
                                                     final InternalReadAccessor extractor,
                                                     final Object[] row,
                                                     final Evaluator evaluator) {
        final boolean result = evaluator.evaluate( null,
                                                   extractor,
                                                   row[0],
                                                   extractor,
                                                   row[2] );
        final String message = "The evaluator type: [" + valueType + "] with 2 extractors incorrectly returned " + result + " for [" + row[0] + " " + row[1] + " " + row[2] + "]. It was asserted to return " + row[3];

        if ( row[3] == Boolean.TRUE ) {
            assertTrue( message,
                        result );
        } else {
            assertFalse( message,
                         result );
        }
    }

    private VariableContextEntry getContextEntry(final Evaluator evaluator,
                                                 final InternalReadAccessor extractor,
                                                 final ValueType valueType,
                                                 final Object[] row) {
        final Declaration declaration = new Declaration( "test",
                                                         extractor,
                                                         null );

        final ObjectVariableContextEntry context = new ObjectVariableContextEntry( extractor,
                                                                                   declaration,
                                                                                   evaluator );
        if ( row[2] == null ) {
            context.leftNull = true;
        } else {
            context.left = row[2];
        }

        if ( row[0] == null ) {
            context.rightNull = true;
        } else {
            context.right = row[0];
        }
        return context;
    }

}
