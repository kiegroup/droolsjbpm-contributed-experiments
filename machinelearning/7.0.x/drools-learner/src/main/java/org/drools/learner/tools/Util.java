package org.drools.learner.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.drools.learner.Domain;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;

public class Util {

    public static final boolean PRINT_STATS = true;
    public static final String DRL_DIRECTORY = "src/main/rules/";
    public static final int NUM_TREES = 10;

    //	public static final boolean DEBUG = false;
    //	public static final boolean DEBUG_TEST = false;
    //	public static final boolean DEBUG_RULE_PRINTER = false;
    //	public static final boolean DEBUG_LEARNER = false;
    //	public static final boolean DEBUG_CATEGORIZER = false;
    //	public static final boolean DEBUG_ENTROPY = false;
    //	public static final boolean DEBUG_DIST = false;
    //	public static final boolean DEBUG_DECISION_TREE = false;

    public static int MAX_NUM_RULES = 1000000;
    public static boolean ONLY_ACTIVE_RULES = true; /*
                                                     * TODO into global settings
                                                     */

    public static boolean SORT_RULES_BY_RANK = true;
    public static boolean PRINT_RULES = true;

    private static Random BAGGING = new Random( System.currentTimeMillis() );
    //public static String log_file = "testing.log";
    public static double TRAINING_RATIO = 1.0, TESTING_RATIO = 0.0;;
    public static double DEFAULT_TRAINING_RATIO = 0.80, DEFAULT_TESTING_RATIO = 0.20;

    public static String ntimes( String s, int n ) {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < n; i++ ) {
            buf.append( s );
        }
        return buf.toString();
    }

    public static String sum() {
        return "sum";
    }

    public static double log2( double prob ) {
        return Math.log( prob ) / Math.log( 2 );
    }

    public static double ln( double prob ) {
        return Math.log( prob );
    }

    public static double exp( double prob ) {
        return Math.exp( prob );
    }

    public static double division( int x, int y ) {
        return (double) x / (double) y;
    }

    public static double division( int x, long y ) {
        return (double) x / (double) y;
    }

    public static boolean epsilon( double d, double epsilon ) {
        return Math.abs( d ) <= epsilon; //0.000001;
    }

    /* TODO make this all_fields arraylist as hashmap */
    public static void getSuperFields( Class<?> clazz, ArrayList<Field> allFields ) {
        if ( clazz == Object.class )
            return;

        //Field [] element_fields_ = clazz.getFields();
        Field[] elementFields = clazz.getDeclaredFields(); //clazz.getFields();
        for ( Field f : elementFields ) {
            allFields.add( f );
        }
        getSuperFields( clazz.getSuperclass(), allFields );

        return;
    }

    public static void decomposeStructuredFields( ArrayList<Field> elementFields, ArrayList<Field> decomposedFields ) {

        for ( Field f : elementFields ) {
            if ( isSimpleType( f.getType() ) )
                decomposedFields.add( f );
            else {
                // it is an structured attribute
                // u need to get its attributes
                //if the field is a collection of sth => BOK
                ArrayList<Field> fieldStructure = new ArrayList<Field>();
                Util.getSuperFields( f.getType(), fieldStructure );
                decomposeStructuredFields( fieldStructure, decomposedFields );
            }
        }
        return;
    }

    public static boolean getterExits( String mName ) {
        if ( mName.startsWith( "set" ) )
            return true;
        else
            return false;
    }

    public static boolean isSetter( String mName ) {
        if ( mName.startsWith( "set" ) )
            return true;
        else
            return false;
    }

    public static String getFReference( Class<?> objKlass, String fName ) {
        return objKlass.getName() + "@" + fName;
    }

    public static String getDecReference( Field f ) {
        Class<?> ownerClass = f.getDeclaringClass(); // i need the class that the field belongs to boooook
        return ownerClass.getName() + "_" + f.getType().getName() + "_" + f.getName();
    }

    public static String getFieldName( String methodName ) {
        if ( methodName.startsWith( "get" ) || methodName.startsWith( "set" ) )
            return methodName.substring( 3, methodName.length() ).toLowerCase();
        else if ( methodName.startsWith( "is" ) )
            return methodName.substring( 2, methodName.length() ).toLowerCase();
        else
            return null;
    }

    public static boolean isSimpleMethod( Class<?>[] typeName ) {
        if ( typeName.length == 1 ) {
            return isSimpleType( typeName[0] );
        } else
            return false;
    }

    public static DataType getDataType( Class<?> typeName ) {
        if ( isSimpleType( typeName ) )
            return DataType.PRIMITIVE;
        else {
            if ( isCollectionType( typeName ) ) {
                return DataType.COLLECTION;
            } else
                return DataType.STRUCTURED;
        }
    }

    public static boolean isSimpleType( Class<?> typeName ) {
        if ( typeName.isPrimitive() || typeName == String.class )
            return true;
        else if ( typeName == Boolean.class || typeName == Boolean.TYPE || typeName == Integer.class || typeName == Integer.TYPE || typeName == Long.class || typeName == Long.TYPE
                || typeName == Double.class || typeName == Double.TYPE || typeName == Float.class || typeName == Float.TYPE || typeName == String.class )
            return true;
        else if ( typeName.isEnum() ) {
            return true;
        } else
            return false;
    }

    public static boolean isCollectionType( Class<?> typeName ) {
        if ( typeName.isArray() )
            return true;
        else if ( typeName.isAssignableFrom( Iterable.class ) )
            return true;
        else
            return false;
    }

    public static FieldAnnotation getFieldAnnotations( Field f ) {
        Annotation[] annotations = f.getAnnotations();
        for ( Annotation a : annotations ) {
            if ( a instanceof FieldAnnotation ) {
                return (FieldAnnotation) a; // here it is !!!
            }
        }
        return null;
    }

    public static ClassAnnotation getDecClassAnnotations( Class<?> clazz ) {
        Annotation[] annotations = clazz.getDeclaredAnnotations(); // it should get the inherited annotations 
        for ( Annotation a : annotations ) {
            if ( a instanceof ClassAnnotation ) {
                return (ClassAnnotation) a; // here it is !!!
            }
        }
        return null;
    }

    public static ClassAnnotation getClassAnnotations( Class<?> clazz ) {
        Annotation[] annotations = clazz.getAnnotations(); // it should get the inherited annotations 
        for ( Annotation a : annotations ) {
            if ( a instanceof ClassAnnotation ) {
                return (ClassAnnotation) a; // here it is !!!
            }
        }
        return null;
    }

    // to process the type => it must be simple
    public static void processDomain( Domain fieldDomain, Class<?> clazzType ) {
        if ( clazzType == Boolean.TYPE
                || clazzType == Boolean.class ) { /* set discrete */
            //			fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
            fieldDomain.addCategory( Boolean.TRUE );
            fieldDomain.addCategory( Boolean.FALSE );
            fieldDomain.setFixed( true );
        } else if ( clazzType.isEnum() ) {//f.isEnumConstant()) {
            Class<?> enumF = clazzType;
            //for (E e:enum_f.getEnumConstants())
            //fieldDomain.setFixed(true);
        } else if ( clazzType == String.class ) {
            /* BY DEFAULT it is categorical */
        }

    }

    /* TODO make this all_fields arraylist as hashmap */
    private static void getAllFields( Class<?> clazz, ArrayList<Field> allFields, ArrayList<Class<?>> allClasses ) {
        if ( clazz == Object.class )
            return;
        allClasses.add( clazz );
        //Field [] element_fields_ = clazz.getFields();
        Field[] elementFields = clazz.getDeclaredFields(); //clazz.getFields();
        for ( Field f : elementFields ) {
            allFields.add( f );
        }
        getAllFields( clazz.getSuperclass(), allFields, allClasses );

        return;
    }

    public static Object calculateMidPoint( Class<?> fClass, Object cpI, Object cpINext ) {
        if ( fClass.isAssignableFrom( Integer.class ) || fClass == Integer.TYPE ) {
            return ( (Integer) cpI + (Integer) cpINext ) / 2;
        } else if ( fClass.isAssignableFrom( Short.class ) || fClass == Short.TYPE ) {
            return ( (Short) cpI + (Short) cpINext ) / 2;
        } else if ( fClass.isAssignableFrom( Long.class ) || fClass == Long.TYPE ) {
            return ( (Long) cpI + (Long) cpINext ) / 2;
        } else if ( fClass.isAssignableFrom( Double.class ) || fClass == Double.TYPE ) {
            return ( (Double) cpI + (Double) cpINext ) / 2;
        } else if ( fClass.isAssignableFrom( Float.class ) || fClass == Float.TYPE ) {
            return ( (Float) cpI + (Float) cpINext ) / 2;
        } else {
            System.out.print( "What should i do then Categorizer.find_a_split() for " + fClass );
            System.exit( 0 );
        }
        return 0.0;
    }

    private static int getType( Class<?>[] typeName ) {
        /*
         * simpletype.contains(type_name) how do you get the simple type
         */
        if ( typeName.length == 1 ) {
            if ( typeName[0].getName().equalsIgnoreCase( "boolean" ) || typeName[0].getName().equalsIgnoreCase( "int" ) || typeName[0].getName().equalsIgnoreCase( "double" )
                    || typeName[0].getName().equalsIgnoreCase( "float" ) )
                return 1;
            else if ( typeName[0].getName().equalsIgnoreCase( "java.lang.String" ) )
                return 2;
            else
                return -1;
        } else
            return 0;
    }

    public static int[] bagWRep( int k, int N ) {
        int[] bag = new int[k];
        int i = 0;
        while ( i < k ) {
            bag[i++] = BAGGING.nextInt( N );
        }
        return bag;
    }

    public static int[] bagWoRep( int k, int N ) {
        int[] bag = new int[k];
        boolean[] selected = new boolean[N];
        int i = 0;
        while ( i < k ) {
            int newI = BAGGING.nextInt( N-- );
            //if (Util.DEBUG)	System.out.print("new_i "+ new_i +"\t");
            int selectedJ = 0, j = 0;
            while ( j < newI || selected[j] ) {
                if ( selected[j] ) {
                    newI++;
                    selectedJ = j;
                }
                j++;
            }
            selected[j] = true;
            bag[i] = j;
            i++;
        }
        return bag;
    }

    private static Object convertPrimitiveType( Object primitiveValue ) {
        Class klass = primitiveValue.getClass();
        if ( klass.equals( Boolean.TYPE ) ) {
            return (Boolean) primitiveValue;

        }
        return null;

    }

    public boolean bok = false;

    public Boolean bokkafa = Boolean.FALSE;
    public String sbok = "x";

    public static final void main( final String[] args ) throws Exception {
        System.out.println( Util.convertPrimitiveType( false ) );

        Util u = new Util();
        Field f = u.getClass().getField( "bok" );
        System.out.println( Util.convertPrimitiveType( f.get( u ) ) );
        if ( f.getType().isPrimitive() )
            System.out.println( "Primitive" + f.get( u ) );
        else
            System.out.println( "Not Primitive" + f.get( u ) );
        Field fkafa = u.getClass().getField( "bokkafa" );
        if ( fkafa.getType().isPrimitive() )
            System.out.println( "Primitive" + fkafa.get( u ) );
        else
            System.out.println( "Not Primitive kafa" + fkafa.get( u ) );
        Field fS = u.getClass().getField( "sbok" );
        if ( fS.getType() == String.class )
            System.out.println( fS.get( u ) );

        String abd = "abd";
        String abcm = "abcm";
        ArrayList<String> x = new ArrayList<String>( 2 );
        x.add( abd );
        x.add( abcm );
        x.add( "q" );
        x.add( "abdm" );
        x.add( "abc" );
        System.out.println( "the result: " + abd.compareTo( abcm ) );

        Collections.sort( x );

        for ( String s : x ) {
            System.out.println( s );
        }

    }
}
