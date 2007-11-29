package benchmarks.dispatch.test;

import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.drools.FactHandle;
import org.drools.ObjectFilter;
import org.drools.WorkingMemory;

import benchmarks.dispatch.DispatchWrapper;
import benchmarks.dispatch.fact.independent.DispatchState;

public class AbstractDispatchTest extends TestCase {

	protected WorkingMemory wm;
	protected FactHandle dispatchStateFactHandle;
	protected DispatchState ds;

	public void setUp() throws Exception {
		DispatchWrapper db = new DispatchWrapper();
		wm = db.getWM();

		ds = new DispatchState();
		ds.setCurrentTime(new Date());
		dispatchStateFactHandle = wm.insert(ds);
		
		wm.fireAllRules();
	}

	protected void incrementDSTime(int minutes) {
		DispatchState ds = (DispatchState) wm
				.getObject(dispatchStateFactHandle);
		Date newTime = new Date(ds.getCurrentTime().getTime() + 1000 * 60
				* minutes);
		ds.setCurrentTime(newTime);
		wm.update(dispatchStateFactHandle, ds);
	}

	protected void assertFactExists(Class factClass, String message) {
		assertTrue(message, hasFact(factClass));
	}

	protected void assertFactAbsent(Class factClass, String message) {
		assertFalse(message, hasFact(factClass));

	}
	
	private boolean hasFact(Class factClass){
		Iterator it = wm.iterateObjects(new TypeFilter(factClass));
		return it.hasNext();
	}

	private static class TypeFilter implements ObjectFilter {

		Class c;

		public TypeFilter(Class c) {
			this.c = c;
		}

		public boolean accept(Object object) {
			return object.getClass().equals(c);
		}

	}
}
