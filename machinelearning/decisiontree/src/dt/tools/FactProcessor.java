package dt.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.NumericDomain;

public class FactProcessor {


	public static Hashtable<Object, List<Fact>> splitFacts_disc(
			List<Fact> facts, String attributeName, List<?> attributeValues) {
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
	public static Hashtable<Object, List<Fact>> splitFacts_cont(
			List<Fact> facts, Domain<?> attributeDomain) {
		String attributeName = attributeDomain.getName();
		
		System.out.println("FactProcessor.splitFacts_cont() kimi diziyoruz: "+ attributeName);
		
		List<Fact> categorization = ((NumericDomain)attributeDomain).getRepresentatives();
		System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ categorization.size());
		
		for (Fact f: categorization)
			System.out.println("FactProcessor.splitFacts_cont() haniymis benim factim: "+ f.getFieldValue(attributeName));
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(
				categorization.size());
		for (Fact v : categorization) {
			factLists.put(v, new ArrayList<Fact>());
		}
		for (Fact f : facts) {
			Comparator<Fact> cont_comp = attributeDomain.factComparator();
			ListIterator<Fact> category_it = categorization
					.listIterator(categorization.size() - 1);
			while (category_it.hasPrevious()) {
				Fact category_fact = category_it.previous();

				if (cont_comp.compare(f, category_fact) < 0) {
					factLists.get(category_fact.getFieldValue(attributeName))
							.add(f);
				}
			}

		}
		return factLists;
	}

}
