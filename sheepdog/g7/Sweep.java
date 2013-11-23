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
  private static final int SWEEP_CIRCLE      = 4;
  private int mode = RUN_TO_OTHER_SIDE;

  public int radius = 50;
  public boolean changeRadius = false;
  public Point[] arcPoints;
  public Point[] dogs;
  public Point[] sheep;
  public Point current;
  public int id;
  public int counter = 0;

  public boolean sweeping = false;

  public Sweep(Point[] dgs, Point[] sh, int idx){
    dogs = dgs;
    sheep = sh;
    current = dogs[idx];
    id = idx;
    arcPoints = new Point[dgs.length];
  }

  public Point nextMove(){
      Point gate = new Point(right_limit/2, down_limit/2);
      Point next = new Point(0.0,0.0);
      setNextMode();
      if (sweeping) mode = SWEEP_CIRCLE;
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
          double y = (down_limit / (dogs.length) * id);
          System.out.println("Y VALUE: " + y);
          System.out.println("DOG ID: " + id + "Y Value: " + y);
          Point arcLocation = new Point(right_limit, y);
          next = Geometry.next_toward_goal(current, arcLocation, 1.99999);
          break;
        case SWEEP_LEFT:
          Point towardsCenter = new Point(right_limit/2, current.y);
          next = Geometry.travelTowards(current, towardsCenter, max_dog_speed/3);
          break;
        case SWEEP_CIRCLE:
          if(allDogsFar()){
            Point towardsCenter = new Point(right_limit/2, current.y);
            next = Geometry.next_toward_goal(current, towardsCenter, max_dog_speed/3);
          }
          else{
            // Top six dogs
            if(id < 3){
              Point goalPoint = new Point(50.0 + id, ++counter);
              next = Geometry.next_toward_goal(current, goalPoint, max_dog_speed/3);
            }
            // ones at bottom
            else if(id > dogs.length-4){
              Point goalPoint = new Point((50.0 + dogs.length-id), 100.0 - (++counter));
              next = Geometry.next_toward_goal(current, goalPoint, max_dog_speed/3);
            }
            else{
              double newY = (down_limit / (dogs.length-6.0)) * (id-3.0);
              System.out.println("Y VALUE: " + newY);
              System.out.println("DOG ID: " + id + "Y Value: " + newY);
              Point newPoint = new Point(dogs[id].x, newY);
              next = Geometry.next_toward_goal(current, newPoint, max_dog_speed/3);
            }
            // Bottom six dogs

            // Everyone else
            System.out.println("Stop here");
          }
          break;
          case SWEEP_LEFT:
          // Point towardsCenter = new Point(right_limit/2, current.y);
          // next = Geometry.travelTowards(current, towardsCenter, max_dog_speed/3);
          break;
        default:
          next = current;
          break;
      }
      return next;
  }

  public boolean allDogsOnArc(Point[] dogs){
    for(Point dog: dogs){
      boolean checkOnRadius = Math.abs((Math.pow((dog.x - 50.0),2) + Math.pow((dog.y - 50.0),2)) - (Math.pow(radius,2))) <= .5;
      if(!checkOnRadius){
        return false;
      }
    }
    return true;
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
      System.out.println("MODE: "  + mode);
      if ((nextMode == ALIGN_ON_FAR_WALL && allDogsLinedUp()) || mode == SWEEP_CIRCLE){
        System.out.println("NEXT MODE IS SWEEP");
        // nextMode = SWEEP_LEFT;
        nextMode = SWEEP_CIRCLE;
        sweeping = true;
      }
      if (mode == SWEEP_CIRCLE || nextMode == SWEEP_CIRCLE) {
        sweeping = true;
        nextMode = SWEEP_CIRCLE;
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
    boolean value = anyAtTop || anyAtBottom;
    System.out.println("ANY AT TOP BOTTOM: " + value);
    return anyAtTop || anyAtBottom;
  }

  private boolean allDogsFar(){
    for (int i = 0 ; i < dogs.length ; i++){
      if (dogs[i].x  <= 53.0) return false;
    }
    return true;
  }

}
