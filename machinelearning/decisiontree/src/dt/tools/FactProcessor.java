package dt.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import dt.memory.Domain;
import dt.memory.Fact;

public class FactProcessor {

	public static Hashtable<Object, List<Fact>> splitFacts(
			List<Fact> facts, Domain<?> choosenDomain) {
		if (choosenDomain.isDiscrete()) {	
			return FactProcessor.splitFacts_disc(facts, choosenDomain);
		} else {
			Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
			return FactProcessor.splitFacts_cont(facts, choosenDomain);
		}
	}
	public static Hashtable<Object, List<Fact>> splitFacts_disc(
			List<Fact> facts, Domain<?> choosenDomain) {
		String attributeName = choosenDomain.getName();
		List<?> attributeValues = choosenDomain.getValues();
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(attributeValues.size());
		for (Object v : attributeValues) {
			factLists.put(v, new ArrayList<Fact>());
		}
		for (Fact f : facts) {
			factLists.get(f.getFieldValue(attributeName)).add(f);
		}
		return factLists;
	}
	
	/* it must work */
	private static Hashtable<Object, List<Fact>> splitFacts_cont(
			List<Fact> facts, Domain<?> attributeDomain) {
		
		String attributeName = attributeDomain.getName();
		
		System.out.println("FactProcessor.splitFacts_cont() attr_split "+ attributeName);
		
		List<?> categorization = attributeDomain.getValues();
		List<Integer> split_indices = attributeDomain.getIndices();
		System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ categorization.size() + " and the split points "+ split_indices.size());
		
		System.out.println("FactProcessor.splitFacts_cont() before splitting "+ facts.size());
		int split_i =0;
		for(int i=0; i<facts.size(); i++) {
			if (split_i<split_indices.size() && split_indices.get(split_i).intValue()== i) {
				System.out.println("PRINT*: FactProcessor.splitFacts_cont() will split at "+i + " the fact "+facts.get(i));
				split_i ++;
			} else {
				System.out.println("PRINT: FactProcessor.splitFacts_cont() at "+i + " the fact "+facts.get(i));
			}
		}
		
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(categorization.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		//Comparator<Fact> cont_comp = attributeDomain.factComparator();
		Iterator<Integer> splits_it = split_indices.iterator();
		int start_point = 0;
		int index = 0;
		while (splits_it.hasNext()) {
			int integer_index = splits_it.next().intValue();
			Object category = attributeDomain.getValues().get(index);
			//System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);
			Fact pseudo = new Fact();
			try {
				pseudo.add(attributeDomain, category);
				
				System.out.println("FactProcessor.splitFacts_cont() new category: "+ category );
				System.out.println(" ("+start_point+","+integer_index+")");
				factLists.put(category, facts.subList(start_point, integer_index));
				start_point = integer_index;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			index++;
			
		}
		return factLists;
	}

	/* it must work */
	private static Hashtable<Object, List<Fact>> splitFacts_cont_(
			List<Fact> facts, Domain<?> attributeDomain) {
		
		String attributeName = attributeDomain.getName();
		
		System.out.println("FactProcessor.splitFacts_cont() kimi diziyoruz: "+ attributeName);
		
		List<?> categorization = attributeDomain.getValues();
		System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ categorization.size());
		
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(categorization.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		Comparator<Fact> cont_comp = attributeDomain.factComparator();
		Iterator<?> category_it = attributeDomain.getValues().iterator();
		int start_point = 0;
		while (category_it.hasNext()) {
			Object category = category_it.next();
			System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);
			Fact pseudo = new Fact();
			try {
				pseudo.add(attributeDomain, category);
				int insertion_point_1 = Collections.binarySearch(facts, pseudo, cont_comp);
				if (insertion_point_1 < 0)
					factLists.put(category, facts.subList(start_point, -1*insertion_point_1));
				else {
					
					System.out.println("FactProcessor.splitFacts_cont() last category: "+ 
							category + " the point "+-1*insertion_point_1 + " the size "+ facts.size());
					factLists.put(category, facts.subList(start_point, insertion_point_1));
					break;
				}
				start_point = -1* insertion_point_1;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return factLists;
	}

}
