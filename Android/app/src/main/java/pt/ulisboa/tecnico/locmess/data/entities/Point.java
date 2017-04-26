package pt.ulisboa.tecnico.locmess.data.entities;

/**
 * Created by goncalo on 26-04-2017.
 */

public class Point {
    public double x;
    public double y;
    public Point nextPoint;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean distanceToPath(Point originPoint) {
        Point currentPoint = originPoint;
        while (currentPoint != null)  {
            double distance = distanceToPointSquared(currentPoint);

        }
    }

    public double distanceToPointSquared(Point point) {
        return Math.pow(point.x - x,2) + Math.pow(point.y - y, 2);
    }

    public double dot(Point point) {
        return x*point.x + y*point.y;
    }

//    public Point minus(Point point) {
//        return new
//    }
}
