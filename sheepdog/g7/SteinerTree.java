package sheepdog.g7;

import sheepdog.sim.Point;

public class SteinerTree {
  Point ROOT = new Point(50,50);

  public SteinerTree() {
  }

  // Returns an array where element at i has value j
  // where j corresponds to the index of the sheep that
  // is its parent. -1 as a parent means the root is its parent.
  int[] fill(Point[] sheep){
    int n = sheep.length;
    // Initialization: Store distance from root (goal) to each sheep
    double[] distance = new double[n];
    for (int i = 0; i < distance.length; i++) {
      distance[i] = Geometry.distance(ROOT, sheep[i]);
    }

    int[] parent = new int[n];
    for (int i = 0; i < sheep.length; i++) {
      double distToNearestNode = distance[i];

      // initialize the parent sheep as -1 (meaning root node / goal)
      parent[i] = -1;

      // Look at all sheep that are closer than sheep i to the root
      for (int j = 0; j < n; j++) {
        if (i == j || distance[j] >= distToNearestNode) continue;

        // Mark the closest sheep that is nearer to the root as the parent
        if (distToNearestNode > Geometry.distance(sheep[i], sheep[j])) {
          parent[i] = j;
          distToNearestNode = Geometry.distance(sheep[i], sheep[j]);
        }
      }
    }
    return parent;
  }
}
