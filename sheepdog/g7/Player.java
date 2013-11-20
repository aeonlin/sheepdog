package sheepdog.g7;

import sheepdog.sim.Point;
import java.util.*;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;

    private static final double up_limit = 0.0;
    private static final double down_limit = 100.0;
    private static final double left_limit = 0.0;
    private static final double right_limit = 100.0;
    private static final double max_dog_speed = 1.9999;
    private static final double max_sheep_speed = 0.9999;

    private static final double border_distance = 49.5;

    private Record globalRecord;

    private int strategy_phase;
    public Sweep[] sweeps;

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        strategy_phase = -1; // nothing happens now
        globalRecord = new Record();
    }

    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps

        Point current = dogs[id-1];
        // basic scenario
        if( mode == false) {

            return basic_strategy(dogs, sheeps);
            /*
            if  (dogs.length == 1) {
                return basic_strategy(dogs, sheeps);
            }
            else {
                return manyDogStrategy(dogs, sheeps);
            }
            */
        }
        // advanced scenario
        else {
            return current;
        }
    }

    private Point manyDogStrategy(Point[] dogs, Point[] sheep) {
      if (sweeps == null) sweeps = new Sweep[dogs.length];
      int idx = id - 1;

      if (sweeps[idx] == null) {
        sweeps[idx] = new Sweep(dogs, sheep, id - 1);
      } else {
        sweeps[idx].current = dogs[idx];
        sweeps[idx].dogs = dogs;
        sweeps[idx].sheep = sheep;
      }
      return sweeps[idx].nextMove();
    }

    private void log(String message){
      System.out.println(message);
    }

    private Point basic_strategy(Point[] dogs, Point[] sheeps) {

        switch(strategy_phase) {
            case -1:
                return move_dogs_to_the_other_side(dogs, sheeps);
            case 0:
                return sweep_sheep(dogs, sheeps);
            /*
            case 1:
                return collect_sheep(dogs, sheeps);
            case 2:
                return move_sheep(dogs, sheeps);
            case 3:
            */
            default:
                break;
        }
        return new Point(0.0, 0.0);
    }

    private ArrayList<Point> sheepOutsideRadius( Point[] sheeps, double radius) {
        // find the sheep that are outside of the specified radius
        // return a list of sheep that are outside of the radius
        Point center = centerPoint(sheeps);
        // radius is a constant for now
        ArrayList<Point> sheepOutOfBounds = new ArrayList<Point>();
    

        // place all sheep out of bounds of radius
        for(Point sheep: sheeps){
            boolean outside = !(Math.pow((double) sheep.x - center.x, 2) + Math.pow((double) sheep.y - center.y, 2) <= Math.pow((double) radius, 2.0));
            if(outside && sheep.x >= 50.0){
                sheepOutOfBounds.add(sheep);
            }
        } 
        return sheepOutOfBounds;
    }

    private void print_point(Point thePoint, String s) {
        System.out.println( s + " : (" + thePoint.x + "," + thePoint.y + ")");
    }
    private double vector_length(Point thePoint) {
        return Math.sqrt(thePoint.x * thePoint.x + thePoint.y * thePoint.y);
    }
    private double vector_length(Point pointA, Point pointB) {
        double dx = pointA.x - pointB.x;
        double dy = pointA.y - pointB.y;
        return Math.sqrt( dx*dx + dy*dy);
    }
    private Point next_toward_goal(Point current, Point goal, double speed) {
        Point direction = new Point( goal.x - current.x, goal.y - current.y );
        System.out.println("v_l:" + vector_length(direction) + ", speed_limit:" + speed);
        if(vector_length(direction) <= speed )  {
            System.out.println("jump to the goal");
            return goal;
        }
        return next_with_direction(current, direction, speed);
    }
    private Point next_with_direction(Point current, Point direction, double speed) {
        double s = speed;
        if(s > max_dog_speed) {
            s = max_dog_speed;
        }
        double direction_length = vector_length(direction);
        if( direction_length != 0) {
            double ratio = speed / direction_length;
            return new Point(current.x + ratio * direction.x, 
                                current.y + ratio * direction.y);
        }
        return current;
    }

    /*
    private boolean all_dogs_ready (Point[] dogs) {
        for (Point d : dogs) {
            if(d.x <= 50) {
                return false;
            }
        }
        return true;
    }
    */

    private Point centerPoint(Point[] sheeps){
        double averageX = 0.0;
        double averageY = 0.0;
        for(Point point: sheeps){
            averageX += point.x;
            averageY += point.y;
        }
        averageX /= sheeps.length;
        averageY /= sheeps.length;
        return new Point(averageX, averageY);
    }

    private Point move_dogs_to_the_other_side( Point[] dogs, Point[] sheeps) {
        if(dogs[id-1].x == 0 ) {
            globalRecord.initialize(id, dogs, sheeps); 
        }

        Point next = new Point(dogs[id-1].x, dogs[id-1].y);
        Point goal = globalRecord.gatePoint;

        if(next.equals(goal)) {
            strategy_phase = 0;
            next = globalRecord.gatePoint;
        }
        else {
            strategy_phase = -1;
            System.out.println("phase == -1");
            next = next_toward_goal(next, goal, max_dog_speed);
        }
        //globalRecord.updateTargetedSheep(dogs,sheeps);
        //globalRecord.updateLastTic(dogs,sheeps);
        return next;
    }

    private double[] sheeps_rectangle( Point[] sheeps) {
        double[] ends = new double[4]; // up, down, left, right
        ends[0] = right_limit; // then update for the left_lmit of the sheep herd
        ends[1] = left_limit; // then update for the right_lmit of the sheep herd
        ends[2] = down_limit; // then update for the up_lmit of the sheep herd
        ends[3] = up_limit; // then update for the down_lmit of the sheep herd
        for(Point s: sheeps) {
            if( s.x < ends[0]) {
                ends[0] = s.x;
            }
            if( s.x > ends[1]) {
                ends[1] = s.x;
            }
            if( s.y < ends[2]) {
                ends[2] = s.y;
            }
            if( s.y > ends[3]) {
                ends[3] = s.y;
            }
        }
        return ends;
    }

    private boolean sheeps_away_from_fence (Point[] sheeps, double distance) {
        double[] ends = new double[4];
        ends = sheeps_rectangle(sheeps);
        if (    (ends[0] > left_limit) &&
                (ends[1] < right_limit - distance) &&
                (ends[2] > up_limit + distance) &&
                (ends[3] < down_limit - distance) ) {
            return true;
        }
        return false;
    }

    private boolean sheeps_away_from_fence (Point theSheep, double distance) {
        if (    (theSheep.x > left_limit) &&
                (theSheep.x < right_limit - distance) &&
                (theSheep.y > up_limit + distance) &&
                (theSheep.y < down_limit - distance) ) {
            return true;
        }
        return false;
    }

    // divide the fence of the right half into segments
    // every dog is responsible for one segment
    /*
    private Point line_to_plane_transform(double x) {
        if(x<0.0) {
            return new Point(50.0,0.0);
        }
        if(x<50.0) {
            return new Point(50.0 + x, 0.0);
        }
        if(x<150.0) {
            return new Point(100.0, x - 50.0);
        }
        if(x<200.0) {
            return new Point(250.0 - x, 100.0);
        }
        return new Point(50.0, 100.0);
    }
    private double plane_to_line_transform(Point thePoint) {
        if(thePoint.y == 0.0) {
            return thePoint.x - 50.0;
        }
        if(thePoint.y == 100.0) {
            return 250.0 - thePoint.x;
        }
        if(thePoint.x == 100.0) {
            return thePoint.y + 50.0;
        }
        return plane_to_line_transform(projection_to_fence(thePoint));
    }

    private Point[] sweep_segment(int numberOfDogs) {
        Point[] theSegment = new Point[2];
        // up side: 0.0 - 50.0
        // right side: 50.0 - 150.0
        // down side: 150.0 - 200.0
        double start = (double)(id-1) * 200.0 / (double)(numberOfDogs);
        double end = (double)(id) * 200.0 / (double)(numberOfDogs);
        theSegment[0] = line_to_plane_transform(start);
        theSegment[1] = line_to_plane_transform(end);
        return theSegment;
    }

    private Point next_along_fence(Point current, Point goal) {
        double start = plane_to_line_transform(current);
        double end = plane_to_line_transform(goal);
        double setPoint = start;
        if( start < end ) {
            setPoint = start + max_dog_speed;
            if( 50.0 - max_dog_speed < start && start < 50.0) {
                setPoint = 50.0;
            }
            if( 150.0 - max_dog_speed < start && start < 150.0) {
                setPoint = 150.0;
            }
            if(setPoint > end) {
                setPoint = end;
            }
            return line_to_plane_transform( setPoint );
        }
        if( start > end ) {
            setPoint = start - max_dog_speed;
            if( 150.0 + max_dog_speed > start && start > 150.0) {
                setPoint = 150.0;
            }
            if( 50.0 + max_dog_speed > start && start > 50.0) {
                setPoint = 50.0;
            }
            if( setPoint < end ) {
                setPoint = end;
            }
            return line_to_plane_transform( setPoint );
        }
        return current;
    }
    */
    private boolean on_the_fence(Point thePoint) {
        if (    (thePoint.x == 100.0) ||
                (thePoint.x == 50.0) ||
                (thePoint.y == 0.0) ||
                (thePoint.y == 100.0) ) {
            return true;
        }
        return false;
    }
    private boolean outside_the_fence(Point thePoint) {
        if (    (thePoint.x > 100.0) ||
                (thePoint.y > 100.0) ||
                (thePoint.y < 0.0) ||
                (thePoint.x < 50.0) ){
            return true;
        }
        return false;
    }

    private Point projection_to_fence(Point thePoint) {
        
        double x_shift = thePoint.x - globalRecord.gatePoint.x;
        double y_shift = thePoint.y - globalRecord.gatePoint.y;

        double downRight = (100.0 - globalRecord.gatePoint.y) / (100.0 - globalRecord.gatePoint.x);
        double upRight = (0.0 - globalRecord.gatePoint.y) / (100.0 - globalRecord.gatePoint.x);
        double downLeft = (100.0 - globalRecord.gatePoint.y) / (0.0 - globalRecord.gatePoint.x);
        double upLeft = (0.0 - globalRecord.gatePoint.y) / (0.0 - globalRecord.gatePoint.x);

        if ( x_shift == 0.0) {
            if( y_shift > 0.0 ) {
                return new Point (globalRecord.gatePoint.x, 100.0);
            }
            if( y_shift < 0.0) {
                return new Point (globalRecord.gatePoint.x, 0.0);
            }
            return new Point (100.0 , globalRecord.gatePoint.y);
        }
        double ratio = y_shift / x_shift;
        if ( x_shift > 0.0 ) {
            // on the bottom
            if ( ratio > downRight) {
                ratio = (100.0 - globalRecord.gatePoint.y) / y_shift;
            }
            // on the top
            else if ( ratio < upRight) {
                ratio = (0.0 - globalRecord.gatePoint.y) / y_shift;
            }
            // on the right
            else {
                ratio = (100.0 - globalRecord.gatePoint.x) / x_shift;
            }
        }
        else {
            // on the bottom
            if ( ratio < downLeft) {
                ratio = (100.0 - globalRecord.gatePoint.y) / y_shift;
            }
            // on the top
            else if ( ratio > upLeft) {
                ratio = (0.0 - globalRecord.gatePoint.y) / y_shift;
            }
            // on the left
            else {
                ratio = (50.0 - globalRecord.gatePoint.x) / x_shift;
            }
        }
        return new Point(x_shift * ratio + globalRecord.gatePoint.x,
                         y_shift * ratio + globalRecord.gatePoint.y);
    }

    private Point projection_from_gate(Point thePoint, double distance) {
        double x_shift = thePoint.x - globalRecord.gatePoint.x;
        double y_shift = thePoint.y - globalRecord.gatePoint.y;
        System.out.println("x_s:" + x_shift + ", y_s:" +y_shift);

        Point direction = new Point(x_shift,y_shift);
        if(on_the_fence(thePoint)) {
            // go along with the reflection extension line 1m away
            if( thePoint.x == 100.0 || thePoint.x == 50.0) {
                direction.y = 0.0 - direction.y;
            }
            else {
                direction.x = 0.0 - direction.x;
            }
            return next_with_direction(thePoint, direction, distance);
        }
        else {
            Point targetPoint = next_with_direction(thePoint, direction, distance);
            if (outside_the_fence(targetPoint)) {
                return projection_to_fence(thePoint);
            }
            return targetPoint;
        }
    }

    private ArrayList<Integer> list_free_sheeps(Point[] sheeps) {
        ArrayList<Integer> theList = new ArrayList<Integer>();
        for(int i = 0; i < sheeps.length; i++ ) {
            if (sheeps[i].x >= 50.0) {
                theList.add(i);
            }
        }
        /*
        System.out.print("sheepList:");
        int[] ret = new int[theList.size()];
        for(int i = 0; i < theList.size(); i++) {
            ret[i] = theList.get(i).intValue();
            System.out.print(ret[i]+",");
        }
        System.out.println();
        return ret;
        */
        return theList;
    }

    private boolean sheep_in_gate(Point theSheep) {
        if(theSheep.x <= globalRecord.gatePoint.x) {
            if(theSheep.y > 49.0 && theSheep.y < 51.0) {
                return true;
            }
            if( vector_length(theSheep, new Point(50.0, 49.0)) < 1.0 ||
                vector_length(theSheep, new Point(50.0, 51.0)) < 1.0 ) {
                return true;
            }
        }
        if(theSheep.x < 50.0) {
            return true;
        }
        return false;
    }
    private boolean deep_in_left(Point theSheep) {
        if(theSheep.y > 49.0 && theSheep.y < 51.0) {
            if(theSheep.x < 49.0) {
                return true;        
            }
            return false;
        }
        else if(theSheep.x < 50.0) {
            if( vector_length(theSheep, new Point(50.0, 49.0)) > 1.0 &&
                vector_length(theSheep, new Point(50.0, 51.0)) > 1.0 ) {
                return true;
            }
            return false;
        }
        return false;
    }

    private int nearest_sheep(ArrayList<Integer> sheepList, Point[] sheeps, Point theDog) {
        double min_distance = -1;
        double distance = -1;
        int index = -1;
        for (Integer s: sheepList) {
            int i = s.intValue();
            Point d = new Point(sheeps[i].x - theDog.x, sheeps[i].y - theDog.y);
            distance = vector_length(d);
            if(min_distance == -1 || min_distance > distance) {
                index = i;
                min_distance = distance;
            }
        }
        return index;
    }
    private int nearest_sheep_not_chased(ArrayList<Integer> sheepList, Point[] sheeps, Point[] dogs) {
        //boolean[] theSheepIsChased = new boolean[sheeps.length];
        //Arrays.fill(theSheepIsChased, false);
        for (int i = 0; i < id - 1; i++) {
            if (dogs[i].x >= 50) {
                // mark dog by dog
                int j = nearest_sheep(sheepList, sheeps, dogs[i]);
                //theSheepIsChased[j] = true;
                for (int k = 0; k < sheepList.size(); k++) {
                    if (j == sheepList.get(k) ) {
                        sheepList.remove(k);
                        break;
                    }
                }
            }
        }
        return nearest_sheep(sheepList, sheeps, dogs[id-1]);
    }

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog position

        // where this dog move
        Point next = new Point(dogs[id-1].x, dogs[id-1].y); 

        /*
        "finite state machine"
        // specify the sheeps it need to take care
            1. list all the sheeps which are "too close" (10m) to the fence
            2. divide tasks to all dogs
            3. each dog keep track with the same sheeps
                3.i the dog run along the fence to the opposite direction of the sheeps
                3.ii the dog approach sheep with the run speed of sheeps
                3.iii until the sheeps are away from the fence
                3.iv then it move back to the fence and move to the next sheep
        */

        // normal sheeping strategy

            System.out.println("sweepPhase = " + globalRecord.sweepPhase);
            System.out.println("targetSheep: " + globalRecord.targetSheepIndex);
            ArrayList<Integer> sheepList = list_free_sheeps(sheeps);
            Point theSheep = new Point();
            // case of no target sheep now, find the nearest sheep as the target
            if(globalRecord.targetSheepIndex == -1) {
                globalRecord.sweepPhase = 3;
            }

            switch(globalRecord.sweepPhase) {
                // phase -1: choose a sheep. if choosed, then keep moving to its extending line a little bit further
                // phase 0: approach to the sheep with the run speed of sheeps, then chase it back to the gatePoint
                // phase 1: move the sheep into the left side with 1m
                // phase 2: go back to the gatePoint
                case -1:
                    // set the goal be 1.0m more further
                    theSheep = sheeps[globalRecord.targetSheepIndex];
                    Point goal = projection_from_gate(theSheep, 3.0);
                    if(next.equals(goal) || vector_length(next,goal) <= 0.5 ) {
                        globalRecord.sweepPhase = 0;
                        break;
                    }
                    if( sheep_in_gate(theSheep)) {
                        globalRecord.sweepPhase = 2;
                    }
                    // then move to the goal
                    next = next_toward_goal(next, goal, max_dog_speed);
                    break;
                case 0:
                    // herding sheep to the gatePoint
                    theSheep = sheeps[globalRecord.targetSheepIndex];
                    double moveSpeed = max_sheep_speed;
                    if (sheep_in_gate(theSheep)) {
                        globalRecord.sweepPhase = 1;
                        break;
                    }
                    if (vector_length(next,globalRecord.gatePoint) <= 
                        vector_length(theSheep,globalRecord.gatePoint)) {
                        globalRecord.sweepPhase = -1;
                        break;
                    }
                    if (vector_length(next,theSheep) > 1.1) {
                        moveSpeed = max_dog_speed;
                    }
                    next = next_toward_goal(next, projection_from_gate(theSheep, 0.1), moveSpeed);
                    break;
                case 1:
                    // move the sheep 1m into the left side
                    theSheep = sheeps[globalRecord.targetSheepIndex];
                    if ( deep_in_left(theSheep)) {
                        globalRecord.sweepPhase = 2;
                    }
                    else {
                        Point direction = new Point (50.0 - theSheep.x, 50.0 - theSheep.y);
                        if( theSheep.x >= 50.0 ) {
                            direction.x = 1;
                            direction.y = 0;
                        }
                        if( sheep_in_gate(theSheep) ) {
                            next = next_toward_goal(next, next_with_direction(theSheep, direction, 0.2), max_dog_speed);
                        }
                        // sometimes the sheep escape
                        else {
                            globalRecord.sweepPhase = -1;
                        }
                        break;
                    }
                case 2:
                    // heading back to the idlePoint
                    if (next.equals(globalRecord.gatePoint)) {
                        // ready for next sheep
                        globalRecord.sweepPhase = 3;
                        globalRecord.targetSheepIndex = -1;
                    }
                    next = next_toward_goal(next, globalRecord.gatePoint, max_dog_speed);
                    break;
                case 3:
                    if (next.equals(globalRecord.idlePoint)) {
                        globalRecord.targetSheepIndex = nearest_sheep_not_chased(sheepList, sheeps, dogs);
                        if(globalRecord.targetSheepIndex != -1) {
                            globalRecord.sweepPhase = -1;
                        }
                    }
                    print_point(globalRecord.idlePoint, "idlePoint");
                    next = next_toward_goal(next, globalRecord.idlePoint, max_dog_speed);
                    break;
                //escape from the left side
                case 999:
                    Point theEscape = new Point(49.9, 50.0);
                    if (next.equals(theEscape)) {
                        globalRecord.sweepPhase = 2;
                    }
                    next = next_toward_goal(next, theEscape, max_dog_speed);
                    break;
                default:
                    break;
            }

        return next;
    }

    /*

    private Point collect_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: get sheep inside of desired radius
        // input: the positions of the dogs and the sheeps
        // return: the position of where the dog should move
        // Get sheep outside of desired radius
        // for now, radius is hard coded as 10m
        ArrayList<Point> sheepOutsideRadius = sheepOutsideRadius(sheeps, 5.0);
        if(sheepOutsideRadius  != null){
            System.out.println("THERE ARE THIS MANY SHEEP OUTSIDE RADIUS" + sheepOutsideRadius);
        }
        Point targetPoint = new Point(dogs[id-1].x, dogs[id-1].y);
        // below is the strategy for just one dog
        if(dogs.length == 1){
            // find the nearest sheep to get to go to the radius
            // closestSheep
            Point closestSheep = closestSheep(sheepOutsideRadius, dogs[id-1]);
            // Determine the target direction for the sheep to move in
            // target direction is on three points side of circle
            // target direction is determined by slope of line between sheep and center point
            Point centerPoint = centerPoint(sheeps);
            double slope = (closestSheep.x - centerPoint.x) / (closestSheep.y - centerPoint.y);
            //System.out.println(globalRecord.dogsMovement.length);
            //Point lastPoint = globalRecord.dogsMovement[globalRecord.dogsMovement.length-1];
            // move along arc!
            // left point of sheep
            Point leftMostPoint = new Point(closestSheep.x - .1, closestSheep.y);
            Point rightMostPoint = new Point(closestSheep.x + .1, closestSheep.y);
            Point bottomMostPoint = new Point(closestSheep.x, closestSheep.y - .1);
            Point topMostPoint = new Point(closestSheep.x, closestSheep.y + .1);

            ArrayList<Point> desiredPoints = new ArrayList<Point>(4);
            desiredPoints.add(leftMostPoint);
            desiredPoints.add(rightMostPoint);
            desiredPoints.add(bottomMostPoint);
            desiredPoints.add(topMostPoint);

            // I'm just going to move from this point to that
            System.out.println(slope);
            if(slope >= 0){
                Point newTarget = closestSheep(desiredPoints, dogs[id-1]);
                targetPoint = next_toward_goal(targetPoint, newTarget, max_dog_speed);
                //next_with_direction
            }
            else{
                // DO SOMETHING
            }
            return targetPoint;
        }

        // TODO: below is the strategy for more than just one dog
        return new Point(0.0,0.0);
        //return next_toward_goal(next, projection_to_fence(next), max_dog_speed);
    }

    // find the closest sheep depending on the sheeps passed in
    // return the coordinates of the closest sheep
    private Point closestSheep(ArrayList<Point> sheeps, Point dog){
        Point closestSheep = new Point(0.0,0.0);
        double closestDistance = 1000.0;
        for(Point sheep: sheeps){
            double minusXs = dog.x - sheep.x;
            double minusYs = dog.y - sheep.y;
            double distance = Math.sqrt((Math.pow(minusXs,2.0)) + (Math.pow(minusYs, 2.0)));
            if(distance < closestDistance){
                closestDistance = distance;
                closestSheep = sheep;
            }
        }
        return closestSheep;
    }

    private Point move_sheep( Point[] dogs, Point[] sheeps ) {
        // Point next;
        return new Point(0.0, 0.0);
    }

    */
}


