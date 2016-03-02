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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (Double.compare(position.x, x) != 0) return false;
        if (Double.compare(position.y, y) != 0) return false;
        return floor != null ? floor.equals(position.floor) : position.floor == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (floor != null ? floor.hashCode() : 0);
        return result;
    }
}
