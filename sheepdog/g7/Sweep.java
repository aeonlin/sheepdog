package sheepdog.g7;

import sheepdog.sim.Point;

class Sweep {
  private static final double up_limit = 0.0;
  private static final double down_limit = 100.0;
  private static final double left_limit = 0.0;
  private static final double right_limit = 100.0;
  private static final double max_dog_speed = 2.0;

  private static final int RUN_TO_OTHER_SIDE = 0;
  private static final int RUN_TO_FAR_WALL   = 1;
  private static final int ALIGN_ON_FAR_WALL = 2;
  private static final int SWEEP_LEFT        = 3;
  private int mode = RUN_TO_OTHER_SIDE;

  public Point[] dogs;
  public Point[] sheep;
  public Point current;
  public int id;

  public boolean sweeping = false;

  public Sweep(Point[] dgs, Point[] sh, int idx){
    dogs = dgs;
    sheep = sh;
    current = dogs[idx];
    id = idx;
  }

  public Point nextMove(){
      Point gate = new Point(right_limit/2, down_limit/2);
      Point next;
      setNextMode();
      if (sweeping) mode = SWEEP_LEFT;
      System.out.println(mode);
      System.out.println("SWEEPING: " + sweeping);
      switch (mode) {
        case RUN_TO_OTHER_SIDE:
          next = Geometry.travelTowards(current, gate, max_dog_speed);
          break;
        case RUN_TO_FAR_WALL:
          Point farRight = new Point(right_limit, down_limit/2);
          next = Geometry.travelTowards(current, farRight, max_dog_speed);
          break;
        case ALIGN_ON_FAR_WALL:
          double y = (down_limit / (dogs.length-1) * id);
          Point arcLocation = new Point(right_limit, y);
          next = Geometry.travelTowards(current, arcLocation, max_dog_speed);
          break;
        case SWEEP_LEFT:
          Point towardsCenter = new Point(right_limit/2, current.y);
          next = Geometry.travelTowards(current, towardsCenter, max_dog_speed/3);
          break;
        default:
          next = current;
          break;
      }
      return next;
  }

  private void setNextMode(){
      int nextMode = 0;
      if (current.x < right_limit / 2) {
        nextMode = RUN_TO_OTHER_SIDE;
      } else if (current.x < right_limit * 0.95 && (mode == RUN_TO_OTHER_SIDE || mode == RUN_TO_FAR_WALL)) {
        nextMode = RUN_TO_FAR_WALL;
      } else {
        nextMode = ALIGN_ON_FAR_WALL;
      }
      System.out.println(allDogsLinedUp());
      System.out.println("ALIGN_ON_FAR_WALL: "  + (mode == ALIGN_ON_FAR_WALL));
      if ((nextMode == ALIGN_ON_FAR_WALL && allDogsLinedUp()) || mode == SWEEP_LEFT){
        System.out.println("NEXT MODE IS SWEEP");
        nextMode = SWEEP_LEFT;
        sweeping = true;
      }
      if (mode == SWEEP_LEFT || nextMode == SWEEP_LEFT) {
        sweeping = true;
        nextMode = SWEEP_LEFT;
      }
      else mode = nextMode;
  }

  private boolean allDogsLinedUp() {
    boolean anyAtTop    = false;
    boolean anyAtBottom = false;
    for (int i = 0 ; i < dogs.length ; i++){
      if (Math.abs(dogs[i].x - (right_limit)) > 1.0) return false;
      if (Math.abs(dogs[i].y - down_limit) < 0.5) anyAtTop = true;
      if (Math.abs(dogs[i].y - up_limit) < 0.5) anyAtBottom = true;
    }
    return anyAtTop && anyAtBottom;
  }
}
