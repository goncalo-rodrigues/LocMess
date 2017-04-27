package pt.ulisboa.tecnico.locmess.data;

import java.util.Date;

/**
 * Created by goncalo on 26-04-2017.
 */

public class Point {
    public double x;
    public double y;
    public Point nextPoint;

    private static final double STANDARD_PARALLELS_COSINE = 0.77995433338;
    private static final double STANDARD_PARALLELS = 38.7436056;
    private static final double UNIT_DISTANCE = 86.81831160375718;



    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

//    public boolean distanceToPath(Point originPoint) {
//        Point currentPoint = originPoint;
//        while (currentPoint != null)  {
//            double distance = distanceToPointSquared(currentPoint);
//
//        }
//    }

    public double distanceToPathSquared(Point originPoint) {
        Point origin = new Point(0,0);
        Point currentPoint = originPoint;
        double bestDistance = 5000;
        while (currentPoint != null)  {
            Point ax = this.minus(currentPoint); // x - a
            double distance = ax.sizeSquared(); // |x-a|^2
            bestDistance = Math.min(distance, bestDistance);
            if (currentPoint.nextPoint != null) {
                Point ab = currentPoint.nextPoint.minus(currentPoint); // b - a
                double vectorSize = ab.sizeSquared(); // |b-a|^2
                double dotProduct = ab.dot(ax); // |ax.ab|
                double proj = dotProduct*dotProduct/vectorSize; // |ax.ab|^2/|ab|^2
                if (dotProduct >=0 && dotProduct <= vectorSize) {
                    double distanceToVector = distance - proj; // |ax|^2 - |ax.ab|^2/|ab|^2
                    bestDistance = Math.min(bestDistance, distanceToVector);
                }
            }
            currentPoint = currentPoint.nextPoint;
        }

        return bestDistance*UNIT_DISTANCE*UNIT_DISTANCE;
    }

    public void aggregatePoints() {
        double maxD = 1;
        maxD = Math.pow(maxD / UNIT_DISTANCE, 2);
        Point a = this;
        Point b = nextPoint;
        if (b == null) {
            return;
        }
        Point c = b;
        Point ab = b.minus(a);
        Point prev = b;
        double vectorSize = ab.sizeSquared(); // |b-a|^2
        double currentD = 0;
        while (currentD < maxD) {
            prev = c;
            c = c.nextPoint;
            if (c == null) {
                break;
            }
            Point ac = c.minus(a);
            double distance = ac.sizeSquared(); // |x-a|^2
            double dotProduct = ab.dot(ac); // |ax.ab|
            double proj = dotProduct*dotProduct/vectorSize; // |ax.ab|^2/|ab|^2
            double distanceToVector = distance - proj; // |ax|^2 - |ax.ab|^2/|ab|^2
            currentD = distanceToVector;
        }

        this.nextPoint = prev;
        prev.aggregatePoints();

    }

    public double sizeSquared() {
        return x*x + y*y;
    }

    public double dot(Point point) {
        return x*point.x + y*point.y;
    }

    public Point minus(Point point) {
        return new Point(x-point.x, y-point.y);
    }

    public static Point fromLatLon(double lat, double lon) {
        double x = lon * STANDARD_PARALLELS_COSINE;
        double y = lat - STANDARD_PARALLELS;
        return new Point(x, y);
    }

    public double getLon() {
        return x / STANDARD_PARALLELS_COSINE;
    }

    public double getLat() {
        return y + STANDARD_PARALLELS;
    }
    @Override
    public String toString() {
        return "("+x +","+y+")" + (nextPoint != null? "->"+nextPoint.toString() : "");
    }
}
