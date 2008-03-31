package dt.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
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
		
		System.out.println("FactProcessor.splitFacts_cont() kimi diziyoruz: "+ attributeName);
		
		List<?> categorization = attributeDomain.getValues();
		System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ categorization.size());
		
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(categorization.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		for (Fact f : facts) {
			Comparator<Fact> cont_comp = attributeDomain.factComparator();
			ListIterator<?> category_it = attributeDomain.getValues().listIterator(attributeDomain.getValues().size() - 1);
			while (category_it.hasPrevious()) {
				Object category = category_it.previous();
				Fact pseudo = new Fact();
				try {
					pseudo.add(attributeDomain, category);
					if (cont_comp.compare(f, pseudo) < 0) {
						factLists.get(category).add(f);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}
		return factLists;
	}

}
