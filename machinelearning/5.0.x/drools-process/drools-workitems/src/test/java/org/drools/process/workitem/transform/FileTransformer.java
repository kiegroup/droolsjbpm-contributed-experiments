package org.drools.process.workitem.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileTransformer {

	// All transform methods must be static and must contain the @Transformer annotation
	@Transformer
	public static String fileToString(File file) {
		try {
			String fileString = "";
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = reader.readLine()) != null) {
				fileString += line;
			}
			return fileString;
		} catch (Exception e) {
			System.err.println("Failed to read file " + file.getName());
		}
		return null;
	}

	@Transformer
	public static BufferedReader fileToBufferedReader(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			return reader;
		} catch (Exception e) {
			System.err.println("Failed to read file " + file.getName());
		}
		return null;
	}
}
