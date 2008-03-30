package dt.memory;

import java.util.Collection;

public interface FactSet {

	String getClassName();
	
	void assignTo(Collection<Fact> c);

	Domain<?> getDomain(String attr);

	/* TODO iterator */ 
	public Collection<Domain<?>> getDomains();

	public int getSize();
	
	public String toString();
}
