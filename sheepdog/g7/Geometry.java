package sheepdog.g7;

import sheepdog.sim.Point;

class Geometry {
  public static Point travelAlong(Point p1, Point p2, Point current, Point towards, double dist){
    double m = (p1.y - p2.y) / (p1.x - p2.x);
    // b = y - mx
    double b = p1.y - (m * p1.x);
    double slopeAngle = Math.atan(m);

    double dx = Math.cos(slopeAngle) * dist;
    double dy = Math.sin(slopeAngle) * dist;

    if (towards.x > current.x)
      return new Point(current.x + dx, current.y + dy);
    else
      return new Point(current.x - dx, current.y - dy);
  }

  public static Point travelTowards(Point current, Point goal, double dist) {
    dist -= 0.001;
    return travelAlong(current, goal, current, goal, dist);
  }
  public static double distance(Point a, Point b) {
      return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                       (a.y-b.y) * (a.y-b.y));
  }
  static double vectorLength(double ox, double oy) {
      return Math.sqrt(ox * ox + oy * oy);
  }
  static double vectorLengthPoint(Point p) {
      return Math.sqrt(p.x*p.x + p.y*p.y);
  }

}
