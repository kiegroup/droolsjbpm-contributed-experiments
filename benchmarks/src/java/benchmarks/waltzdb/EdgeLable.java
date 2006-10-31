package benchmarks.waltzdb;

public class EdgeLable {
    private int p1;
    private int p2;
    private String l_name;
    private int l_id;
    
    public EdgeLable(int p1,
                     int p2,
                     String l_name,
                     int l_id) {
        super();
        this.p1 = p1;
        this.p2 = p2;
        this.l_name = l_name;
        this.l_id = l_id;
    }

    public int getL_id() {
        return l_id;
    }

    public void setL_id(int l_id) {
        this.l_id = l_id;
    }

    public String getL_name() {
        return l_name;
    }

    public void setL_name(String l_name) {
        this.l_name = l_name;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    
    
}
