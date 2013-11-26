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

    if (towards.x > current.x){
       Point returnPoint = new Point(current.x + dx, current.y + dy);
      return fixCoordinate(returnPoint);
    }
    else{
      Point returnPoint = new Point(current.x - dx, current.y - dy);
      return fixCoordinate(returnPoint);
    }
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

  // TODO FIND PROPER FIX - but use this hack in the mean time
  public static Point fixCoordinate(Point point){
    double x = point.x;
    double y = point.y;
    if(x > 100){
      x = 100;
    }
    if(y>100){
      y = 100;
    }
    return new Point(x,y);
  }

  // the function only needs to be called once per radius setting
  // the array should be set to a global variable in the Sweep class
  // each stage - is a stage of the radius instead of like sweeping and stuff
  public static Point[] arcPoints(int numDogs,double angle, double radius, Point firstPoint){
      double fx = Math.cos(angle);
      double fy = Math.sin(angle);
      double lx = -1.0 * fy;
      double ly = fx;
      // this is the first point of the arc
      double firstX = firstPoint.x;
      double firstY = firstPoint.y;
      // this is an array of all the arcpoints
      // so the dog with id - i, will go to point arcPoints[i]
      Point[] arcPoints = new Point[numDogs];
      arcPoints[0] = new Point(firstX,firstY);
      // this will caculate all the arc Points
    for(int i=1; i< numDogs; i++){
      double sub_angle = ((i*1.0)/numDogs) * (angle * 0.01745329251);
      System.out.println("SUB ANGLE: " + sub_angle);
      double x_i = arcPoints[i-1].x + radius * (Math.sin(sub_angle) * fx) + (1.0- Math.cos(sub_angle))*(-lx);
      double y_i = arcPoints[i-1].y + radius*(Math.sin(sub_angle)*fy + (1.0-Math.cos(sub_angle))*(-ly));
      arcPoints[i] = new Point(x_i, y_i);
    }
    for(Point point: arcPoints){
      System.out.println("POINT: " + point.x + " " + point.y);
    }
    return arcPoints;
  }

  public static Point[] calcArcPoints(Point[] dogs, double r){
    double arcLength = lengthOfTheArc(dogs);
    double arcAngle = angleOfTheArc(dogs, r);
    Point firstPoint = FirstPoint(r, arcAngle);
    return arcPoints(dogs.length, arcAngle, r, firstPoint);
  }
 
 public static double lengthOfTheArc(Point[] dogs)
  {  
      double length;
     int last = dogs.length;
     Point p1,p2;
     p1 = dogs[1];
     p2 = dogs[last-1];
            
     double dx = p2.x - p1.x;
     double dy = p2.y - p1.y;
     length =  Math.sqrt( dx*dx + dy*dy);
     System.out.println("LENGTH: " + length);
     // return length;
     return 100.0;
  }
  
  public static double angleOfTheArc(Point[] dogs, double radius)
  {
   double theta;
  double oppositeSideLength, adjacentSideLength;
  double minY = Math.abs(dogs[0].y);
    for(int i = 1 ; i < dogs.length ; i++)
    {
      if(Math.abs(dogs[i].y) < minY)
        minY = Math.abs(dogs[i].y) ;      
    }
    
  oppositeSideLength = Math.abs(50 - minY );
  adjacentSideLength = radius;
  
  theta = Math.atan(oppositeSideLength/adjacentSideLength);
    return Math.abs(theta*2.0);
  }
  
  
  public static Point FirstPoint(double r, double theta)
  
  {
    Point firstp = new Point(0.0, 0.0);
    firstp.x = 50 + r*Math.cos(theta/2);
    firstp.y = 50 + r*Math.sin(theta/2);
    System.out.println("FIRST POINT: " + firstp.x + " " + firstp.y);
    return firstp;
  }

  public static Point next_toward_goal(Point current, Point goal, double speed) {
        Point direction = new Point( goal.x - current.x, goal.y - current.y );
        System.out.println("v_l:" + vector_length(direction) + ", speed_limit:" + speed);
        if(vector_length(direction) <= speed )  {
            System.out.println("jump to the goal");
            return goal;
        }
        return next_with_direction(current, direction, speed);
    }

 private static Point next_with_direction(Point current, Point direction, double speed) {
        double s = speed;
        if(s > 1.99999) {
            s = 1.99999;
        }
        double direction_length = vector_length(direction);
        if( direction_length != 0) {
            double ratio = speed / direction_length;
            return new Point(current.x + ratio * direction.x, 
                                current.y + ratio * direction.y);
        }
        return current;
    }

  private static double vector_length(Point thePoint) {
        return Math.sqrt(thePoint.x * thePoint.x + thePoint.y * thePoint.y);
    }
  private static double vector_length(Point pointA, Point pointB) {
        double dx = pointA.x - pointB.x;
        double dy = pointA.y - pointB.y;
        return Math.sqrt( dx*dx + dy*dy);
    }

  public static double angleFromSlope(double y, double x) {
    return Math.atan2(y, x);
  }

}
