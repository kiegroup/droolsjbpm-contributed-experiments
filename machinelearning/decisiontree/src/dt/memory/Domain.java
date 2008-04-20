package dt.memory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public interface Domain<T> extends Serializable {
	
	boolean isConstant();
	void setConstant();
	
	boolean isDiscrete();
	void setDiscrete(boolean disc);
	
	String getName();

	void addValue(T value);
	void addPseudoValue(T fieldValue);
	
	T getClass(Object value);
	
	List<T> getValues();
	
	Object readString(String data);
	
	String toString();
	
	boolean contains(T value) throws Exception;
	boolean isPossible(Object value) throws Exception;
	
	void setReadingSeq(int readingSeq);
	int getReadingSeq();
	
	Comparator<Fact> factComparator();
	
	public Domain<T> clone();
	void setIndices(List<Integer> split_indices);
	
	List<Integer> getIndices();
	void addIndex(int index);
	
	
	int compare(Object o1, Object o2);
	
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

