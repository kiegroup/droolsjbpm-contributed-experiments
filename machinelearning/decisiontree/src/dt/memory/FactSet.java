package dt.memory;

import java.io.Serializable;
import java.util.Collection;

public interface FactSet extends Serializable {

	String getClassName();
	
	void assignTo(Collection<Fact> c);

	Domain<?> getDomain(String attr);

	/* TODO iterator */ 
	public Collection<Domain<?>> getDomains();

	public int getSize();
	
	public String toString();
}
