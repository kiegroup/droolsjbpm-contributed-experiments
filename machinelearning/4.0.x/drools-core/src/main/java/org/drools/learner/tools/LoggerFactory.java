package org.drools.learner.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

public class LoggerFactory {

	private static BufferedWriter buffer = new BufferedWriter(new StringWriter());
	private static HashMap<Class<?>, SimpleLogger> fileLoggers = new HashMap<Class<?>, SimpleLogger>();
	
	public static SimpleLogger getUniqueFileLogger(Class<?> klass, int level) {
		SimpleLogger sl = fileLoggers.get(klass);
		if (sl == null) {
			sl = new SimpleLogger(level, new WriterLogger(klass, buffer));
			fileLoggers.put(klass, sl);
		}
		return sl;
	}

	public static SimpleLogger getSysOutLogger(Class<?> klass, int level) {
		return new SimpleLogger(level, new SysOutLogger(klass));
	}
	
	public static SimpleLogger getFileLogger(Class<?> klass, int level, String fname) {
		FileWriter file;
		try {
			file = new FileWriter("log/"+fname);
			return new SimpleLogger(level, new WriterLogger(klass, file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void dump_buffer(String file_sign, String directory) {
		// TODO Auto-generated method stub
		
		int last_slash = file_sign.lastIndexOf('/');
		String file_name = file_sign.substring(0, last_slash+1) + directory+file_sign.substring(last_slash)+"."+directory;
		System.out.println(file_name);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter (new BufferedWriter (new FileWriter (file_name)));
			writer.write(buffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}	

class SysOutLogger implements LoggerInterface {
	private Class<?> klass;
	private String msg;
	public SysOutLogger(Class<?> _klass) {
		klass = _klass;
	}
	
	public void setMsg(String _msg) {
		msg = _msg;
	}
	public void stat(String s) {
		System.out.print(s);
	}
	public void log(String s) {
		System.out.print("["+msg + "] ("+klass.getSimpleName()+ ") "+ s);
	}
	public SysOutLogger clone() {
		// TODO Auto-generated method stub
		return new SysOutLogger(klass);
	}
}

class WriterLogger implements LoggerInterface {
	private Writer writer;
	private Class<?> klass;
	private String msg;
	public WriterLogger(Class<?> _klass, Writer w) {
		writer = w;
		klass = _klass;
	}
	public void stat(String s) {
		try {
			writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String s) {
		try {
			writer.write("["+msg + "] ("+klass.getSimpleName()+ ") "+ s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setMsg(String s) {
		msg = s;
		
	}
	@Override
	public WriterLogger clone() {
		// TODO Auto-generated method stub
		return new WriterLogger(klass, writer);
	}

}
