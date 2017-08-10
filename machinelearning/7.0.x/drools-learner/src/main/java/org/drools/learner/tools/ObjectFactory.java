package org.drools.learner.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ObjectFactory {

    private Class<?>                 objClazz;
    private ArrayList<Field>         clazzFields;
    private HashMap<String, Integer> fieldSequences;

    private int MAX_NUM = 30012;

    private ObjectFactory(Class<?> clazz) {
        this.objClazz = clazz;
        this.clazzFields = new ArrayList<Field>();
        Util.getSuperFields(clazz, clazzFields);

        this.fieldSequences = getFieldReadingSeqs(clazz, clazzFields);
    }

    //	public static List<Object> getObjectsFromFile(Class<?> clazz, String filename, String separator) {
    //
    //		ObjectFactory class_factory = new ObjectFactory(clazz);
    //		List<Object> new_objects = fromFileAsObject(filename, separator);
    //
    //	}
    public static List<Object> getObjects(Class<?> objClass, String filename) {
        ObjectFactory classFactory = new ObjectFactory(objClass);
        try {
            //return class_factory.fromFileAsObject("src/main/java/org/drools"+filename, ",");
            return classFactory.fromFileAsObject(filename, ",");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static List<Object> getStructuredObjects(Class<?> objClass, String filename) {
        ObjectFactory classFactory = new ObjectFactory(objClass);
        try {
            //return class_factory.fromFileAsObject("src/main/java/org/drools"+filename, ",");
            return classFactory.fromFileAsStructuredObject(filename, ",");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, Integer> getFieldReadingSeqs(Class<? extends Object> classObj,
                                                                ArrayList<Field> elementFields) {

        HashMap<String, Integer> readingSeqs = new HashMap<String, Integer>(elementFields.size());
        for (Field f : elementFields) {
            String fName = f.getName();

            Annotation[]    annotations = f.getAnnotations();
            FieldAnnotation spec        = null;
            for (Annotation a : annotations) {
                if (a instanceof FieldAnnotation) {
                    spec = (FieldAnnotation) a; // here it is !!!
                    readingSeqs.put(fName, spec.readingSeq());
                    break;
                }
            }
        }
        return readingSeqs;
    }

    private static Object readString(Field f, String text) {

        Class<?> typeName = f.getType();
        if (typeName.isPrimitive()) { //boolean, byte, char, short, int, long, float, and double.
            if (typeName == Boolean.TYPE) {
                return readBooleanFromString(text);
            } else if (typeName == Integer.TYPE) {
                return readIntegerFromString(text);
            } else if (typeName == Short.TYPE) {
                return readShortFromString(text);
            } else if (typeName == Long.TYPE) {
                return readLongFromString(text);
            } else if (typeName == Double.TYPE) {
                return readDoubleFromString(text);
            } else if (typeName == Float.TYPE) {
                return readFloatFromString(text);
            } else {
                System.out.print("ObjectFactory.readString(field, text) primitive type " + typeName);
                return null;
            }
        } else if (typeName.isAssignableFrom(String.class)) {
            return text.trim();
        } else if (typeName.isAssignableFrom(Boolean.class)) {
            return readBooleanFromString(text);
        } else if (typeName.isAssignableFrom(Integer.class)) {
            return readIntegerFromString(text);
        } else if (typeName.isAssignableFrom(Short.class)) {
            return readShortFromString(text);
        } else if (typeName.isAssignableFrom(Long.class)) {
            return readLongFromString(text);
        } else if (typeName.isAssignableFrom(Double.class)) {
            return readDoubleFromString(text);
        } else if (typeName.isAssignableFrom(Float.class)) {
            return readFloatFromString(text);
        } else {
            System.out.print("ObjectFactory.readString(field, text) object type " + typeName);
            return null;
        }
    }

    private static Object readBooleanFromString(String data) {
        //System.out.print("What is the data : "+ data);
        if (data.trim().equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if ((data.trim().equalsIgnoreCase("false"))) {
            return Boolean.FALSE;
        } else if (data.trim().equalsIgnoreCase("1")) {
            return Boolean.TRUE;
        } else if (data.trim().equalsIgnoreCase("0")) {
            return Boolean.FALSE;
        } else {
            return Boolean.parseBoolean(data);
        }
    }

    private static Object readIntegerFromString(String data) {
        return Integer.parseInt(data);
    }

    private static Object readShortFromString(String data) {
        return Short.parseShort(data);
    }

    private static Object readLongFromString(String data) {
        return Long.parseLong(data);
    }

    private static Object readFloatFromString(String data) {
        return Float.parseFloat(data);
    }

    private static Object readDoubleFromString(String data) {
        return Double.parseDouble(data);
    }

    public List<Object> fromFileAsObject(String filename, String separator) throws Exception {

        List<Object> objRead = new ArrayList<Object>();

//        /**/
//        File file = new File( filename );
//        if ( !file.exists() ) {
//            System.out.println( "where is the file ? " + filename );
//            //System.exit(0);
//
//            file = new File( "src/main/java/org/drools/examples/learner/" + filename );
//            if ( !file.exists() ) {
//                file = new File( "drools-examples/drools-examples-drl/src/main/java/org/drools/examples/learner/" + filename );
//
//                System.out.println( "where is still the file ? " + file );
//                if ( !file.exists() ) {
//                    System.out.println( "where is still still the file ? " + file );
//                    System.exit( 0 );
//                }
//            }
//        }
//
//        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        InputStream is = this.getClass().getResourceAsStream("/" + filename);
        if (is == null) {
            System.out.println("where is the file ? " + filename);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        /**/

        //		BufferedReader reader = new BufferedReader(new InputStreamReader(
        //				this.obj_clazz.getResourceAsStream(filename)));// "../data/"
        String line;
        int    num = 0;
        while ((line = reader.readLine()) != null && num < MAX_NUM) {
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            Object element = this.read(line, separator);
            //System.out.println("New object "+ element);
            objRead.add(element);
            num++;
        }
        return objRead;
    }

    public List<Object> fromFileAsStructuredObject(String filename, String separator) throws Exception {
        List<Object> objRead = new ArrayList<Object>();

        /**/
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("where is the file ? " + filename);
            //System.exit(0);

            file = new File("src/main/java/org/drools/examples/learner/" + filename);
            if (!file.exists()) {
                file = new File("drools-examples/drools-examples-drl/src/main/java/org/drools/examples/learner/" + filename);

                System.out.println("where is still the file ? " + file);
                if (!file.exists()) {
                    System.out.println("where is still still the file ? " + file);
                    System.exit(0);
                }
            }
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        /**/

        //		BufferedReader reader = new BufferedReader(new InputStreamReader(
        //				this.obj_clazz.getResourceAsStream(filename)));// "../data/"
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            if (line.endsWith(".")) {
                line = line.substring(0,
                                      line.length() - 1);
            }
            List<String> fieldValues = Arrays.asList(line.split(separator));
            Object       element     = this.readStructured(this.objClazz, fieldValues);
            //System.out.println("New object "+ element);
            objRead.add(element);
        }
        return objRead;
    }

    private Object readStructured(Class<?> objClazz, List<String> fieldValues) throws Exception {
        Object           element     = null;
        ArrayList<Field> clazzFields = new ArrayList<Field>();
        Util.getSuperFields(objClazz, clazzFields);
        HashMap<String, Integer> fieldSequences = getFieldReadingSeqs(objClazz, clazzFields);
        try {
            element = objClazz.newInstance();

            Method[] elementMethods = objClazz.getDeclaredMethods();
            for (Method m : elementMethods) {
                String     mName         = m.getName();
                Class<?>[] paramTypeName = m.getParameterTypes();
                if (Util.isSetter(mName)) {
                    Object fieldValue = null;
                    ;
                    if (Util.isSimpleMethod(paramTypeName)) {
                        String basicFieldName = Util.getFieldName(mName);

                        /*
                         * TODO dont use the basic field name as the real field
                         * name, there can be a capital/miniscul letter problem
                         */
                        Field field = null;
                        for (Field f : clazzFields) {
                            if (f.getName().equalsIgnoreCase(basicFieldName)) {
                                field = f;
                                break;
                            }
                        }
                        if (field == null) {
                            throw new Exception("Field(" + basicFieldName + ") could not be found for the class(" + objClazz + ")");
                        } else {
                            //System.out.print("Field("+f.getName()+") for the class("+_obj_clazz+") " );
                            //System.out.print("seq="+_field_sequences.get(f.getName()) + " ");
                            String fieldString = fieldValues.get(fieldSequences.get(field.getName()));
                            //System.out.println("val="+fieldString);
                            fieldValue = readString(field, fieldString.trim());
                        }
                    } else {
                        Class<?>[] paramClasses = m.getParameterTypes();
                        fieldValue = readStructured(paramClasses[0], fieldValues);
                    }

                    try {
                        m.invoke(element, fieldValue);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        System.out.println("invoking the method of the element e:" + element + " v:" + fieldValue);

                        e.printStackTrace();
                    }
                }
            }
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return element;
    }

    private Object read(String data, String separator) throws Exception {
        Object element = null;

        try {
            element = this.objClazz.newInstance();

            if (data.endsWith(".")) {
                data = data.substring(0, data.length() - 1);
            }
            List<String> fieldValues = Arrays.asList(data.split(separator));

            Method[] elementMethods = this.objClazz.getDeclaredMethods();

            for (Method m : elementMethods) {
                String     mName         = m.getName();
                Class<?>[] paramTypeName = m.getParameterTypes();
                if (Util.isSetter(mName) && Util.isSimpleMethod(paramTypeName)) {
                    String basicFieldName = Util.getFieldName(mName);

                    /*
                     * TODO dont use the basic field name as the real field
                     * name, there can be a capital/miniscul letter problem
                     */
                    Field field = null;
                    for (Field f : this.clazzFields) {
                        if (f.getName().equalsIgnoreCase(basicFieldName)) {
                            field = f;
                            break;
                        }
                    }
                    if (field == null) {
                        throw new Exception("Field(" + basicFieldName + ") could not be found for the class(" + this.objClazz + ")");
                    } else {
                        String fieldString = fieldValues.get(this.fieldSequences.get(field.getName()));
                        Object fieldValue  = readString(field, fieldString.trim());
                        try {
                            m.invoke(element, fieldValue);
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            System.out.println("invoking the method of the element e:" + element + " v:" + fieldValue);

                            e.printStackTrace();
                        }
                    }
                }
            }

            /*
             * for (Field f: element_fields) { String f_name = f.getName();
             *
             * String fieldString = fieldValues.get(fieldSequences.get(f_name));
             * Object fieldValue = readString(f, fieldString);
             *
             * ClassFieldExtractor f_extractor = cache.getExtractor( clazz,
             * f_name, clazz.getClassLoader() ); // f_extractor.
             *
             * }
             */
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return element;
    }
}