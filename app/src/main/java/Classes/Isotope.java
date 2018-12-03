package Classes;

public class Isotope {

    // Attributes
    private int id_;
    private String name_;
    private double energy_;
    private float x_pos_;

    public Isotope(String name, double energy){
        this.id_ = 0;
        this.name_ = name;
        this.energy_ = energy;
        this.x_pos_ = 0.0f;
    }

    public Isotope(String name, double energy, float x_pos){
        this.id_ = 0;
        this.name_ = name;
        this.energy_ = energy;
        this.x_pos_ = x_pos;
    }

    public int getId() {
        return id_;
    }

    public String getName() {
        return name_;
    }

    public double getEnergy() {
        return energy_;
    }

    public float getX_pos() {
        return x_pos_;
    }

    public void setX_pos(float x_pos) {
        this.x_pos_ = x_pos;
    }
}
