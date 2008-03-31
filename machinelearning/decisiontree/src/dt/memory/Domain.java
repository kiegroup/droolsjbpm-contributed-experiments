package dt.memory;

import java.util.Comparator;
import java.util.List;

public interface Domain<T> {
	
	boolean isConstant();
	void setConstant();
	
	boolean isDiscrete();
	void setDiscrete(boolean disc);
	
	boolean contains(T value);

	String getName();

	void addValue(T value);
	void addPseudoValue(T fieldValue);
	
	List<T> getValues();
	
	Object readString(String data);
	
	String toString();
	boolean isPossible(Object value) throws Exception;
	
	void setReadingSeq(int readingSeq);
	int getReadingSeq();
	
	Comparator<Fact> factComparator();
	
	public Domain<T> clone();
	
}



/*
workingmemory.insert(object)

	factset f = factsets_hashtable[object.class]
	if f == null
		f = createnew_factset(object.class);
	f.insert(object)
	
	
factset workingmemory.createnew_factset(class)

	factset newfs = new newfactset(class) 
	foreach field in class
		domain d = domainset_hashtable[field]
		if d == null
			d = createnew_domain(field)
		newfs.adddomain(d)


factset.insert(object)

	fact f;
	foreach field in object
		domain d = domainset_hashtable[field];
		attribute attr = d.createattribute(field.value)
		f.add(attr)
	addfact(f)


treebuilder.execute(workingmemory, classtoexecute, attributestoprocess)

	foreach factset in workingmemory
		if classtoexecute.isAssignableFrom( factset.class )
			internaladd(factset)

	internalprocess(attributestoprocess)


*/

