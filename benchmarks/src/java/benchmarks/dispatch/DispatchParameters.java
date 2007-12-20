package benchmarks.dispatch;

public class DispatchParameters {
	public final static double INITIAL_SEARCH_RADIUS = 100D;
	public final static double INCREMENTAL_SEARCH_RADIUS = 50D;
	
	//Increase surface area by 25%
	public final static double INCREMENTAL_SEARCH_RADIUS_MULTIPLIER = Math.sqrt(1.25);
	
	public final static int MIN_SCORED_WORKERS = 50;
	
}
