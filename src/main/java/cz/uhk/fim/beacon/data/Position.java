package cz.uhk.fim.beacon.data;

/**
 * Created by Kriz on 17. 11. 2015.
 */
public class Position {
    double x;
    double y;
    String floor;

    public Position(double x, double y, String floor) {
        this.x = x;
        this.y = y;
        this.floor = floor;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}
