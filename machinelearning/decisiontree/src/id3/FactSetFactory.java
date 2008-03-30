package id3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FactSetFactory {

	public static String insertNurserySet(WorkingMemory simple) {
		/*
		 * not_recom, recommend, very_recom, priority, spec_prior | attributes
		 * 
		 * parents: usual, pretentious, great_pret. has_nurs: proper,
		 * less_proper, improper, critical, very_crit. form: complete,
		 * completed, incomplete, foster. children: 1, 2, 3, more. housing:
		 * convenient, less_conv, critical. finance: convenient, inconv. social:
		 * nonprob, slightly_prob, problematic. health: recommended, priority,
		 * not_recom.
		 * 
		 */

		String filename = "../data/nursery/nursery.data.txt";
		String separator = ",";
		String klass = "Nursery";
		ArrayList<Domain<?>> domains = new ArrayList<Domain<?>>();
		domains.add(new LiteralDomain("parents", new String[] { "usual",
				"pretentious", "great_pret" }));
		domains.add(new LiteralDomain("has_nurs", new String[] { "proper",
				"less_proper", "improper", "critical", "very_crit" }));
		domains.add(new LiteralDomain("form", new String[] { "complete",
				"completed", "incomplete", "foster" }));
		domains.add(new LiteralDomain("children", new String[] { "1", "2", "3",
				"more" }));
		domains.add(new LiteralDomain("housing", new String[] { "convenient",
				"less_conv", "critical" }));
		domains.add(new LiteralDomain("finance", new String[] { "convenient",
				"inconv" }));
		domains.add(new LiteralDomain("social", new String[] { "nonprob",
				"slightly_prob", "problematic" }));
		domains.add(new LiteralDomain("health", new String[] { "recommended",
				"priority", "not_recom" }));
		domains.add(new LiteralDomain("classnursery", new String[] {
				"not_recom", "recommend", "very_recom", "priority",
				"spec_prior" }));

		for (Domain<?> d : domains) {
			d.setConstant();
		}

		try {
			FactSetFactory
					.fromFile(simple, filename, klass, domains, separator);
			// simple.insert(facts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return klass;
	}

	public static String insertCarSet(WorkingMemory simple) {
		/*
		 * | class values
		 * 
		 * unacc, acc, good, vgood | attributes
		 * 
		 * buying: vhigh, high, med, low. maint: vhigh, high, med, low. doors:
		 * 2, 3, 4, 5, more. persons: 2, 4, more. lug_boot: small, med, big.
		 * safety: low, med, high.
		 * 
		 */

		String filename = "../data/car/car.data.txt";
		String separator = ",";
		String klass = "Car";
		ArrayList<Domain<?>> domains = new ArrayList<Domain<?>>();
		domains.add(new LiteralDomain("buying", new String[] { "vhigh", "high",
				"med", "low" }));
		domains.add(new LiteralDomain("maint", new String[] { "vhigh", "high",
				"med", "low" }));
		domains.add(new LiteralDomain("doors", new String[] { "2", "3", "4",
				"5more" }));
		domains.add(new LiteralDomain("persons", new String[] { "2", "4",
				"more" }));
		domains.add(new LiteralDomain("lug_boot", new String[] { "small",
				"med", "big" }));
		domains.add(new LiteralDomain("safety", new String[] { "low", "med",
				"high" }));
		domains.add(new LiteralDomain("classCar", new String[] { "unacc",
				"acc", "good", "vgood" }));

		for (Domain<?> d : domains) {
			d.setConstant();
		}

		try {
			FactSetFactory
					.fromFile(simple, filename, klass, domains, separator);
			// simple.insert(facts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return klass;
	}

	public static String insertAdvertisementSet(WorkingMemory simple) {

		String filename = "../data/advertisement/ad.data.txt";
		String separator = ",";
		String klass = "Advertisement";

		String domainFileName = "../data/advertisement/data_domains.txt";
		String separatorDomain = ":";
		ArrayList<Domain<?>> domains;
		// FSFactSet facts;
		try {
			domains = FactSetFactory.fromFileDomain(domainFileName,
					separatorDomain);

			FactSetFactory
					.fromFile(simple, filename, klass, domains, separator);
			// simple.insert(facts);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return klass;

	}

	public static ArrayList<String> attributesOfAdvertisement = new ArrayList<String>();

	public static ArrayList<Domain<?>> fromFileDomain(String domainFileName,
			String separator) throws Exception {

		ArrayList<Domain<?>> domains = new ArrayList<Domain<?>>();
		NumericDomain height = new NumericDomain("height");
		height.setDiscrete(false);

		NumericDomain width = new NumericDomain("width");
		height.setDiscrete(false);

		NumericDomain aratio = new NumericDomain("aratio");
		height.setDiscrete(false);
		domains.add(height);
		domains.add(width);
		domains.add(aratio);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				FactSetFactory.class.getResourceAsStream(domainFileName)));// "../data/"
		// +
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("|")) {
				List<String> attributeValues = Arrays.asList(line.split(
						separator, 2));
				// BooleanDomain newDomain =
				attributesOfAdvertisement.add(attributeValues.get(0));
				domains.add(new BooleanDomain(attributeValues.get(0)));
			}
		}

		domains.add(new LiteralDomain("classAdvertisement", new String[] {
				"ad", "nonad" }));
		attributesOfAdvertisement.add("classAdvertisement");
		System.out.println("# of domains:" + domains.size());

		return domains;

	}

	public static void fromFile(WorkingMemory wm, String filename,
			String klass, List<Domain<?>> domains, String separator)
			throws Exception {
		// FSFactSet fs = new FSFactSet(klass, domains);
		//
		// for (Domain<?> d: domains) {
		// fs.addDomain(d.getName(), d);
		// }

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				FactSetFactory.class.getResourceAsStream(filename)));// "../data/"
		// +
		String line;
		while ((line = reader.readLine()) != null) {
			// Fact newFact = fromString(line,domains,separator);
			// fs.add(newFact);
			// String element, String name, String separator, List<Domain<?>>
			// domains
			line = line.trim();
			if (line.length() == 0)
				break;
			wm.insert(line, klass, separator, domains);
		}
	}

	public static boolean readObjectData(WorkingMemory simple, String filename,
			String separator, Object nullObj) {
		/*
		 * | class values
		 * 
		 * unacc, acc, good, vgood
		 *  | attributes
		 * 
		 * buying: vhigh, high, med, low. 
		 * maint: vhigh, high, med, low. 
		 * doors: 2, 3, 4, 5, more. 
		 * persons: 2, 4, more. 
		 * lug_boot: small, med, big.
		 * safety: low, med, high.
		 * 
		 */
		// String[] attr_order = {"buying", "maint", "doors", "persons", "lug_boot", "safety"
		// String filename = "../data/car/car.data.txt";
		// String separator = ",";
		// Car nullCar = new Car();
		

		try {
			FactSetFactory.fromFileAsObject(simple, nullObj.getClass(), filename, separator);

			// simple.insert(facts);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static List<Object> fromFileAsObject(WorkingMemory wm, Class<?> klass, String filename, String separator)
			throws IOException {
		List<Object> obj_read = new ArrayList<Object>();
		OOFactSet fs = wm.getFactSet(klass);
		Collection<Domain<?>> domains = fs.getDomains();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				klass.getResourceAsStream(filename)));// "../data/"
		// +
		String line;
		while ((line = reader.readLine()) != null) {
			// Fact newFact = fromString(line,domains,separator);
			// fs.add(newFact);
			// String element, String name, String separator, List<Domain<?>>
			// domains
			line = line.trim();
			if (line.length() == 0)
				break;
			Object element = ObjectReader.read(klass, domains, line, separator);
			//System.out.println("New object "+ element);
			obj_read.add(element);
			fs.insert(element);
			
		}
		return obj_read;
	}

}
