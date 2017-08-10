package org.drools.learner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;
import org.drools.core.spi.ReadAccessor;
import org.kie.api.runtime.KieSession;

public class InstanceFactory {

    private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger( InstanceFactory.class, SimpleLogger.DEFAULT_LEVEL );
    private static SimpleLogger slog = LoggerFactory.getSysOutLogger( InstanceFactory.class, SimpleLogger.DEFAULT_LEVEL );

    private Schema schema;

    public InstanceFactory( Schema schema ) {
        this.schema = schema;
    }

    public Instance createInstance( Object obj ) {
        try {
            //if (schema.getClassStructure().containsKey(_obj.getClass())) {
            if ( schema.getObjectClass().isAssignableFrom( obj.getClass() ) ) {
                Instance i = new Instance();
                boolean bok = instantiateAttributes( i, obj, obj.getClass() );
                if ( !bok ) {
                    if ( slog.error() != null )
                        slog.error().log( "What is going on, how come it is wrong : " );
                    System.exit( 0 );
                }
                return i; // perfectly instantiated
            } else
                return null;// could not instantiate

            //			if (instantiateAttributes(i, _obj, _obj.getClass()))
            //				return i;	// perfectly instantiated
            //			else
            //				return null;// could not instantiate
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public boolean instantiateAttributes( Instance inst, Object obj, Class<?> klass ) throws Exception {

        if ( slog.info() != null )
            slog.info().log( "Klass : " + klass );
        ClassStructure struct = schema.getClassStructure().get( klass );
        if ( slog.info() != null )
            slog.info().log( "\t ClassStructure : " + struct + "\n" );
        if ( struct == null )
            return false;

        if ( struct.hasLabel() ) {// it has a method or label	
            if ( slog.debug() != null )
                slog.debug().log( ":instantiateAttributes:structure has a label " );
            for ( Method m : struct.getMethods() ) {
                String mName = m.getName();
                //				if (m_name.startsWith("get")) {//TODO check
                //					m_name = m_name.substring(3).toLowerCase();
                //				}

                DataType mType = DataType.PRIMITIVE;// must be primitive
                if ( slog.debug() != null )
                    slog.debug().log( "method= " + mName + " m_type=" + mType + "\n" );
                getAttributeValue( inst, obj, mName, mType );
            }
        }
        // get the fields declared in the class
        for ( Field f : struct.getFields() ) {
            String fName = f.getName();
            DataType fType = struct.getFieldType( f );
            getAttributeValue( inst, obj, fName, fType );
        }
        if ( !struct.getParent().equals( Object.class ) )
            return instantiateAttributes( inst, obj, struct.getParent() );

        return true;
    }

    public void getAttributeValue( Instance inst, Object obj, String name, DataType type ) throws Exception {
        Class<?> objKlass = obj.getClass();
        String fieldReference = Util.getFReference( objKlass, name );
        switch ( type ) {
            case PRIMITIVE: // domain exist only for primitive types
                Domain fieldDomain = schema.getAttrDomain( fieldReference );
                ReadAccessor fieldExtractor = schema.getAttrExtractor( fieldReference );
                if ( slog.info() != null )
                    slog.info().log( "Field name : " + name + " of the object " + obj + " the extract:" + fieldExtractor + "\n" );

                //Object f_value = f_extractor.getValue( (InternalWorkingMemory) session, _obj);
                Object fieldValue = fieldExtractor.getValue( obj );
                if ( fieldValue != null ) {
                    try {
                        if ( fieldDomain.isPossible( fieldValue ) ) {
                            fieldDomain.addCategory( fieldValue );
                            inst.setAttr( fieldReference, fieldValue );
                        }
                    } catch ( Exception e ) {
                        if ( slog.debug() != null )
                            slog.debug().log( "Domain: " + fieldDomain + " could not add the value " + fieldValue + "\n" );
                        e.printStackTrace();
                    }
                } else {
                    if ( slog.debug() != null )
                        slog.debug().log( "Field name : " + name + " of the object " + obj + " the extract:" + fieldExtractor + "\n" );
                    if ( slog.debug() != null )
                        slog.debug().log( "The attribute value cannot be read, check the attribute\n" );
                    System.exit( 0 );
                }
            break;
            case STRUCTURED: // the extractor exists for both types of data.
                ReadAccessor complexFExtractor = schema.getAttrExtractor( fieldReference ); // this returns an object to me
                //Object obj_value = complex_f_extractor.getValue( (InternalWorkingMemory) session, _obj);
                Object objValue = complexFExtractor.getValue( obj );
                instantiateAttributes( inst, objValue, objValue.getClass() );

            break;
            // case Collection
            default:
                throw new Exception( "What type of data is this" );

        }

        return;
    }

}
