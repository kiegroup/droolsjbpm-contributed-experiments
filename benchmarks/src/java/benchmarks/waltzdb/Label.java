package benchmarks.waltzdb;

public class Label {
    public final static String VOID = "*****";
    public final static String FORK = "F";
    public final static String ARROW = "A";
    public final static String TEE = "T";   
    public final static String BEE = "B";
    public final static String ELL = "L";
    public final static String PLUS = "+";
    public final static String MINUS = "-"; 

    private String type;
    private String name;    
    private String n1;
    private String n2;
    private String n3;
    private int id;
            
    public Label(String type,
                 String name,
                 String n1,
                 String n2,
                 String n3,
                 int id) {
        super();
        this.type = type;
        this.name = name;
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getN1() {
        return n1;
    }
    public void setN1(String n1) {
        this.n1 = n1;
    }
    public String getN2() {
        return n2;
    }
    public void setN2(String n2) {
        this.n2 = n2;
    }
    public String getN3() {
        return n3;
    }
    public void setN3(String n3) {
        this.n3 = n3;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    
}
