package Model;

public class RoboPos{

    private final double x;
    private final double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private final double r;


    public RoboPos(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public double getRadious() {
        return r;
    }
}