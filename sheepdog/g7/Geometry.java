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
  public static Point arcPoints(int numDogs,double angle, double radius, Point firstPoint){
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
      double sub_angle = (i/numDogs * 1.0) * (angle * 0.01745329251);
      double x_i = allPoints[i-1] + radius * (Math.sin(sub_angle) * fx) + (1.0- Math.cos(sub_angle))*(-lx));
      double y_i = allPoints[i-1]+radius*(Math.sin(sub_angle)*fy + (1.0-Math.cos(sub_angle))*(-ly));
      arcPoints[i] = new Point(x_i, y_i);
    }
    return allPoints;
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
     return length;
  }
  
  public static double angleOfTheArc(double lengthOfArc, double r)
  
  {
    double theta;
    theta = (lengthOfArc*180)/(3.14*r);
    return theta;
    
  }
  
  
  public static Point FirstPoint(double r, double theta)
  
  {
    Point firstp = null;
    firstp.x = 50 + r*Math.cos(theta/2);
    firstp.y = 50 + r*Math.sin(theta/2);
    
    return firstp;
  }

}
