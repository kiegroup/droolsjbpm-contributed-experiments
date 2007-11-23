package benchmarks.dispatch.fact.independent;

public enum VehicleSize {
    SMALL(1), MEDIUM(2), LARGE(3), EXTRA_LARGE(4);
    
    private int sizeNumber;
    
    VehicleSize(int sizeNumber){
        this.sizeNumber = sizeNumber;
    }
    
    public int getSizeNumber(){
        return sizeNumber;
    }
}
