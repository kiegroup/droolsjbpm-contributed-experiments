package dt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dt.DecisionTree;
import dt.builder.C45TreeIterator;
import dt.builder.DecisionTreeBuilder;

public class DecisionTreeSerializer {
	
	public static void write(Object dt, String file_name) {
		
		File file =new File(file_name);//"temp.tree"
		
		if(file.exists()&& (file.length()>0))
			file.delete();	// should i delete the tree if it already exists??
		
		
//		if(!file.exists())
//			System.out.println("File doesnot exit, creating...");
		
		try {
			// Write to disk with FileOutputStream
			FileOutputStream f_out = new FileOutputStream(file);
			
			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject ( dt );// fix the serialization of working memory
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Object read(String file_name) throws Exception {	
		File file =new File(file_name);//"temp.tree"
		if(!file.exists() || (file.length()<=0)) {
			throw new Exception("File is not found or empty");
		}
		
		// Read from disk using FileInputStream
		FileInputStream f_in = new FileInputStream(file);
		
		// Read object using ObjectInputStream
		ObjectInputStream obj_in = new ObjectInputStream (f_in);

		// Read an object
		Object obj = obj_in.readObject();

		if (obj instanceof DecisionTree || obj instanceof DecisionTreeBuilder || obj instanceof C45TreeIterator ) {
			System.out.println("The object class found");
			return obj;
		} else {
			throw new Exception("There is something else in the decision tree");
		}
		//return null;
		
		
		
	}
}
