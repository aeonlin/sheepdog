package sheepdog.g7;

import sheepdog.sim.Point;
import java.util.*;

class TreeStrategy {
  private Point current;
  private Point[] sheep;
  private Point[] dogs;

  private double distToRoot[];
  private double epsilon = 1e-6;
  static Point gate = new Point(50,50);
  static double DOG_SPEED = 2.0;
  private int nblacks;
  private int id;
  private int targetSheep = -1;
  SteinerTree stree = new SteinerTree();

  public TreeStrategy(Point current, Point[] sheep, Point[] dogs, int id, int nblacks){
    this.current = current;
    this.sheep = sheep;
    this.dogs = dogs;
    this.nblacks = nblacks;
    this.id = id;
  }

  public void update(Point current, Point[] sheep, Point[] dogs){
    this.current = current;
    this.sheep = updateToNewSheep(sheep,dogs);
    this.dogs = dogs;
  }

  private boolean sheepInDogsRegion(int sheep_i){
    Point pos = sheep[sheep_i];
    double x = pos.x - 50;
    double y = pos.y - 50;
    double angle = Geometry.angleFromSlope(y,x);

    double incr =  Math.PI / dogs.length;
    double bottom = (-Math.PI/2) + ((id - 1) * incr);
    double top = (-Math.PI/2) + ((id) * incr);
    return angle > bottom && angle < top;
  }

  public Point nextMove(){
    if (current.x < 50) {
      return Geometry.travelTowards(current, gate, DOG_SPEED);
    }

    distToRoot = new double[sheep.length];
    // indecies of parents for each sheep
    int [] parents = stree.fill(sheep);
    fillDistToRoot(parents, -1 , 0.0);

    int[] farthest = findFarthest(dogs.length);
    int sheepid = farthest[0];


    if (sheepid != -1 && parents[sheepid] != -1 && Geometry.distance(sheep[sheepid], sheep[parents[sheepid]]) < 1){
      sheepid = parents[sheepid];
    }

    Point fromPoint = sheep[sheepid];
    Point toPoint;

    if (parents[sheepid] == -1){
      toPoint = gate;
    } else {
      toPoint = sheep[parents[sheepid]];
    }

    Point dogPoint = positionDogNearSheep(fromPoint, toPoint, 2.0 - epsilon);

    Point motion = new Point(dogPoint.x-dogs[id-1].x, dogPoint.y-dogs[id-1].y);
    double motionDist = Geometry.vectorLengthPoint(motion);

    if (motionDist > DOG_SPEED) {
      motion.x = motion.x/motionDist*(DOG_SPEED-epsilon);
      motion.y = motion.y/motionDist*(DOG_SPEED-epsilon);
    }

    Point dogPos = new Point(dogs[id-1].x+motion.x, dogs[id-1].y+motion.y);
    return dogPos;
  }

  private Point[] updateToNewSheep(Point[] sheep,Point[] dogs) {
    Sheepdog sheepdog = new Sheepdog(dogs.length, sheep.length, nblacks, false);
    sheepdog.sheeps = sheep;
    sheepdog.dogs = dogs;

    Point[] newSheeps = new Point[sheep.length];
      for (int i = 0; i < sheep.length; ++i) {
          // compute its velocity vector
          newSheeps[i] = sheepdog.moveSheep(i);
      }
      return newSheeps;
    }

    static Point positionDogNearSheep(Point from, Point to, double BUFFER){
        double slope = (from.y - to.y)/(from.x - to.x);
        double initialDistance = Geometry.distance(from, to);

        Point finalDestination = new Point();
        finalDestination.x = ((from.x - to.x)*(initialDistance + BUFFER)/(initialDistance)) + to.x;
        finalDestination.y = ((from.y - to.y)*(initialDistance + BUFFER)/(initialDistance)) + to.y;

        if (finalDestination.x > 100)
        {
          finalDestination.x = 100;
          finalDestination.y = slope*(100 - to.x) + to.y;
        }

        if (finalDestination.x < 50)
        {
          finalDestination.x = 50;
          finalDestination.y = slope*(50 - to.x) + to.y;
        }

        if (finalDestination.y > 100){
          finalDestination.y = 100;
          finalDestination.x = (100 - to.y)/slope + to.x;
        }

        if (finalDestination.y < 0){
          finalDestination.y = 0;
          finalDestination.x = (0 - to.y)/slope + to.x;
        }
        return finalDestination;
    }

    // Find distance from each node to the root
    // parents is array where indecies correspond to parent sheep ids
    // nodeIndex is the index of the node/sheep to look at
    private void fillDistToRoot(int[] parents, int nodeIndex, double distance){
        for (int i = 0; i < sheep.length; i++) {
            // find the node(s) that have nodeIndex as a parent
            if (parents[i] == nodeIndex) {
              double distFromParent;

              if (nodeIndex == -1) distFromParent = Geometry.distance(gate, sheep[i]);
              else                 distFromParent = Geometry.distance(sheep[nodeIndex], sheep[i]);

              distToRoot[i] = distance + distFromParent;
              fillDistToRoot(parents, i, distance + distFromParent);
            }
        }
    }

    private Point nodePointFromIndex(int sheepIndex){
      if (sheepIndex == -1) return gate;
      return sheep[sheepIndex];
    }

    private int[] findFarthest(int k) {
        double[] d = new double[distToRoot.length];
        for (int j = 0; j < d.length; j++) {
          // each has negative 1.5 * distnce from current dog to that sheep
          d[j] = distToRoot[j] - (Geometry.distance(dogs[id-1], sheep[j]) * 1.5);
          if (!sheepInDogsRegion(j)){
            d[j] -= 40;
          }
        }

        // if on left, say distance is even more negative
        for (int j = 0; j < d.length; j++) {
          if (sheep[j].x < 50) d[j] =- 100;
        }

        // numbers 0 - distToRoot.length
        int[] index = new int[distToRoot.length];
        for (int i = 0; i < index.length; i++) {
          index[i] = i;
        }

        // dynamic programming ftw
        for (int i = 0; i < distToRoot.length; i++) {
          for (int j = i + 1; j < distToRoot.length; j++) {
            if (d[i] < d[j]) {
              // swap
              double temp = d[i];
              d[i] = d[j];
              d[j] = temp;

              int temp2 = index[i];
              index[i] = index[j];
              index[j] = temp2;
            }
          }
        }
        int ret[] = new int[k];
        for (int i = 0; i < ret.length; i++) {
          ret[i] = index[i];
        }
        return ret;
    }

}
