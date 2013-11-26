package sheepdog.g7;

import sheepdog.sim.Point;

class Sweep {
  private static final double up_limit = 0.0;
  private static final double down_limit = 100.0;
  private static final double left_limit = 0.0;
  private static final double right_limit = 100.0;
  private static final double max_dog_speed = 1.99999;
  private static final double max_sheep_speed = 0.99999;

  private static final int RUN_TO_OTHER_SIDE = 0;
  private static final int RUN_TO_FAR_WALL   = 1;
  private static final int ALIGN_ON_FAR_WALL = 2;
  private static final int SWEEP_LEFT        = 3;
  private static final int SWEEP_CIRCLE      = 4;
  private int mode = RUN_TO_OTHER_SIDE;

  private double squeezingLimit = 2.5;

  private int countDownForUpDownSqueeze = 3;
  private boolean sheepsAwayFromDogWall = false;
  private boolean sheepsAwayFromFarWall = false;
  private boolean sheepsAwayFromUpAndDown = false;

  public Record globalRecord;

  public int radius = 50;
  public boolean changeRadius = false;
  public Point[] arcPoints;
  public Point[] dogs;
  public Point[] sheep;
  public Point current;
  public int id;
  public int counter = 0;
  public boolean linedDogs = false;

  public boolean sweeping = false;

  public Sweep(Point[] dgs, Point[] sh, int idx, Record globalRecord){
    dogs = dgs;
    sheep = sh;
    current = dogs[idx];
    id = idx;
    arcPoints = new Point[dgs.length];
    globalRecord.initialize(idx+1, dgs, sh);
  }

  private boolean sheeps_away_from_dog_wall() {
    if(!sheepsAwayFromDogWall) {
      double gapWidth = 2.0 * squeezingLimit;
      gapWidth = gapWidth * 2.0 / (double)(dogs.length-1);
      double awayThreshold = Math.sqrt(4.0 - gapWidth*gapWidth/4.0);
      sheepsAwayFromDogWall = true;
      for(Point s : sheep) {
        if( s.x >= 53.0 - awayThreshold) {
          sheepsAwayFromDogWall = false;
          break;
        }
      }
    }
    return sheepsAwayFromDogWall;
  }

  private boolean sheeps_away_from_far_wall() {
    if(!sheepsAwayFromFarWall) {
      double gapWidth = down_limit / (double)(dogs.length-1);
      double awayThreshold = Math.sqrt(4.0 - gapWidth*gapWidth/4.0);
      sheepsAwayFromFarWall = true;
      for(Point s : sheep) {
        if( s.x >= right_limit - awayThreshold) {
          sheepsAwayFromFarWall = false;
          break;
        }
      }
    }
    return sheepsAwayFromFarWall;
  }

  private boolean sheeps_away_from_up_and_down() {
    if(!sheepsAwayFromUpAndDown) {
      double gapWidth = 1.5;
      double awayThreshold = Math.sqrt(4.0 - gapWidth*gapWidth/4.0);
      sheepsAwayFromUpAndDown = true;
      for(Point s : sheep) {
        if( s.y >= down_limit - awayThreshold || s.y <= up_limit + awayThreshold) {
          sheepsAwayFromUpAndDown = false;
          break;
        }
      }
    }
    return sheepsAwayFromUpAndDown;
  }

  public Point nextMove(){
      Point gate = new Point(right_limit/2, down_limit/2);
      Point next = new Point(0.0,0.0);

      double gapWidth = down_limit / (double)(dogs.length-1);
      double sweepSpeed = globalRecord.max_speed_for_gap_width(gapWidth);

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
          double y = (down_limit / (dogs.length-1) * id);
          System.out.println("Y VALUE: " + y);
          System.out.println("DOG ID: " + id + "Y Value: " + y);
          Point arcLocation = new Point(right_limit, y);
          next = Geometry.next_toward_goal(current, arcLocation, max_dog_speed);
          break;
        // case SWEEP_LEFT:
        //   Point towardsCenter = new Point(right_limit/2, current.y);
        //   next = Geometry.travelTowards(current, towardsCenter, max_dog_speed/3);
        //   break;
        case SWEEP_CIRCLE:
          if(allDogsFar()){
            Point towardsCenter = new Point(right_limit/2, current.y);
            next = Geometry.next_toward_goal(current, towardsCenter, sweepSpeed);
          }
          else if(doneSqueezing()){
            Point next1 = new Point(0.0,0.0);
            System.out.println("DONE SQUEEZING NOW");
            globalRecord.sweepPhase =2;
            // id < n/4 : up
            // id > 3n/4 : down
            // n/4 < id < 3n/4 : right
            double len = dogs.length;
            double d_id = id;
            if( d_id < len/4 ) {
              next1.y = 50.0 - squeezingLimit;
              next1.x = 50.0 + d_id * 3.0 * 4.0 / len;
            }
            else if (d_id > 3.0*len/4.0) {
              next1.y = 50.0 + squeezingLimit;
              next1.x = 50.0 + (len - 1.0 - d_id)* 3.0 * 4.0 / len;
            }
            else {
              next1.y = 50.0 + squeezingLimit * (d_id - len/2.0) * 4.0 / len;
              next1.x = current.x;
            }
            if (!current.equals(next1)) {
              next = Geometry.next_toward_goal(current, next1, max_dog_speed);
            }
            else {
              if(sheeps_away_from_dog_wall()) {
                next1.x = 50.0;
              }
              next = Geometry.next_toward_goal(current, next1, max_sheep_speed * 0.9);
            }
          }
          else{
            // Top six dogs
            double squeezingGapWidth = 1.5;
            double squeezingSpeed = globalRecord.max_speed_for_gap_width(squeezingGapWidth);
            if(id < 3){
            	Point goalPoint = new Point(0.0,0.0);
              if(linedOnWall()){
                goalPoint = new Point(50.0 + (double)(id)*squeezingGapWidth, 50.0 - squeezingLimit);
              }
              else if(current.y == 0.0 ){
                goalPoint = new Point(50 + (double)(id)*squeezingGapWidth, 0);
                squeezingSpeed = 0.1;
                //squeezingSpeed = max_dog_speed;
              }
              else {
            	  goalPoint = new Point(current.x, 0);
              }
              next = Geometry.next_toward_goal(current, goalPoint, squeezingSpeed);
            }
            // ones at bottom
            else if(id > dogs.length-4){
            	Point goalPoint = new Point(0.0,0.0);
            	if(linedOnWall()){
            		goalPoint = new Point(50.0 + (double)(dogs.length-id-1)*squeezingGapWidth,
                                      50.0 + squeezingLimit);
            	}
            	else if(current.y == 100.0){
                goalPoint = new Point(50.0 + (double)(dogs.length-id-1)*squeezingGapWidth, 100.0);
                squeezingSpeed = 0.1;
            	}
              else {
                goalPoint = new Point(current.x, 100.0);
              }
              next = Geometry.next_toward_goal(current, goalPoint, squeezingSpeed);
            }
            else{
            	
              double newY = (down_limit / (dogs.length-7.0)) * (id-3.0);
              System.out.println("Y VALUE: " + newY);
              System.out.println("DOG ID: " + id + "Y Value: " + newY);
              Point newPoint = new Point(dogs[id].x, newY);
              if(id >= dogs.length / 2 && current.y >= dogs[dogs.length-1].y) {
                newPoint.y = dogs[dogs.length-1].y;
              }
              if(id < dogs.length / 2 && current.y <= dogs[0].y) {
                newPoint.y = dogs[0].y;
              }
              next = Geometry.next_toward_goal(current, newPoint, squeezingSpeed);
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
  
  public boolean linedOnWall(){
	//  if(dogs[0].y == 0.0 && dogs[1].y == 0.0 && dogs[2].y == 0.0 && dogs[dogs.length-2].y == 100.0 && dogs[dogs.length-3].y == 100.0 && dogs[dogs.length-4].y == 100.0){
    if(!sheeps_away_from_up_and_down()) {
      return false;
    }

	  if(linedDogs){
		  return true;
	  }
	  for(int i=0;i<3;i++)
		  
	  {
		  if(dogs[i].y> 0.0)
			  return false;
	  }
	  for(int i = dogs.length-1; i< dogs.length -4; i--) 
	  {
		  if(Math.abs(dogs[i].y-100)> 0.0)
		  return false;
			  
	  }
	  	  linedDogs = true;
		  return true;
	  }
//	  return false;
//  }
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
      if ((nextMode == ALIGN_ON_FAR_WALL && allDogsLinedUp() && sheeps_away_from_far_wall()) 
          || mode == SWEEP_CIRCLE){
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

  private boolean doneSqueezing(){
    System.out.println("THIS IS THE DOGS Y: " + dogs[0].y);
   // if(Math.abs(dogs[0].y - 50.0) <= 0.3){
    if(dogs[1].y >= 50.0 - squeezingLimit) {
    	 return true;
    }
    return false;
  }

  private boolean allDogsFar(){
    for (int i = 0 ; i < dogs.length ; i++){
      if (dogs[i].x  <= 53.0) return false;
    }
    return true;
  }

}
