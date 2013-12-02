package sheepdog.g7;

import sheepdog.sim.Point;
import java.util.*;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private int sim_nblacks;
    private boolean mode;

    private static final double up_limit = 0.0;
    private static final double down_limit = 100.0;
    private static final double left_limit = 0.0;
    private static final double right_limit = 100.0;
    private static final double max_dog_speed = 1.9999;
    private static final double max_sheep_speed = 0.9999;

    private Record globalRecord;
    private TreeStrategy[] treeStrategies;

    private static final double dog_territory_range = 1.5;
    private static final double border_distance = 49.5;

    private int strategy_phase;
    public Sweep[] sweeps;

    public void init(int nblacks, boolean mode) {
        this.sim_nblacks = nblacks;
        this.mode = mode;
        strategy_phase = -1; // nothing happens now
        globalRecord = new Record();
        globalRecord.gameDirection = 1;
    }

    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] sim_dogs, // positions of dogs
                      Point[] sim_sheeps) { // positions of the sheeps
        
        //Point[] dogs = sim_dogs;
        //Point[] sheeps = sim_sheeps;
        //nblacks = sim_nblacks;
        
        Point[] dogs = new Point[sim_dogs.length];
        Point[] sheeps = new Point[sim_sheeps.length];
        sheeps = copyPointArray(sim_sheeps, globalRecord.gameDirection, true);
        dogs = copyPointArray(sim_dogs, globalRecord.gameDirection, false);
        Point current = dogs[id-1];
        Point next = new Point();
        //sheeps = copyPointArray(sim_sheeps, -1, true);
        //dogs = copyPointArray(sim_dogs, -1, false);
        
        if (allTargetSheepMoved(globalRecord.gameDirection, sim_sheeps) && mode) {
            System.out.println("Sys.Reset: old dir = " + globalRecord.gameDirection);
            // change to the other direction
            int dir = globalRecord.gameDirection;
            strategy_phase = -1; // forget everything
            globalRecord = new Record();
            globalRecord.gameDirection = dir * (-1);
            // and reset all the necessary data structure
            // reset the sheeps: exchange the roles of black sheeps and white sheeps
            if (globalRecord.gameDirection < 0) {
                nblacks = sim_nblacks;
            }
            else {
                nblacks = sheeps.length - sim_nblacks;
            }
            sheeps = copyPointArray(sim_sheeps, globalRecord.gameDirection, true);
            dogs = copyPointArray(sim_dogs, globalRecord.gameDirection, false);
            current = dogs[id-1];
            treeStrategies = new TreeStrategy[sim_dogs.length];
            treeStrategies[id-1] = new TreeStrategy(current, sheeps, dogs, id, nblacks);
            System.out.println("Sys.Reset.done: new dir = " + globalRecord.gameDirection);
        }
        if(!mode) {
            globalRecord.gameDirection = -1;
        }
        
        Point[] blackSheep = new Point[nblacks];
        for (int i = 0 ; i < nblacks; i++){
          blackSheep[i] = sheeps[i];
        }
        if (mode){
          sheeps = blackSheep;
        }
        if (treeStrategies == null){
          treeStrategies = new TreeStrategy[dogs.length];
        }
        if (treeStrategies[id-1] == null){
          treeStrategies[id-1] = new TreeStrategy(current, sheeps, dogs, id, nblacks);
        } else {
          treeStrategies[id-1].update(current, sheeps, dogs);
        }

        // basic scenario
        if( mode == false) {
            // return basic_strategy(dogs, sheeps);
            if  (dogs.length >= sheeps.length) {
                next =  basic_strategy(dogs, sheeps);
            } else if (dogs.length < 35){
                next =  treeStrategies[id-1].nextMove();
            } else {
                next = manyDogStrategy(dogs, sheeps);
            }
        }
        // advanced scenario
        else {
            //return basic_strategy(dogs, sheeps);
            // return basic_strategy(dogs, sheeps);
            if  (dogs.length >= nblacks) {
                next =  basic_strategy(dogs, sheeps);
            } else 
                next =  treeStrategies[id-1].nextMove();
        }
        
        if (globalRecord.gameDirection > 0) {
            next.x = 100.0 - next.x;
        }
        
        return next;
    }

    private Point[] copyPointArray(Point[] sim_a, int gameDirection, boolean isSheep) {
        Point[] a = new Point[sim_a.length];
        if (gameDirection < 0) {
            for (int i = 0; i < sim_a.length; i++) {
                a[i] = new Point(sim_a[i].x, sim_a[i].y);
            }
        }
        else {
            for (int i = 0; i < sim_a.length; i++) {
                int j = i;
                if (isSheep) {
                    j = sim_a.length - i - 1;
                }
                a[i] = new Point(100.0 - sim_a[j].x, sim_a[j].y);
            }
        }
        return a;
    }

    private boolean allTargetSheepMoved(int gameDirection, Point[] sim_sheeps) {
        if (gameDirection < 0) {
            // check if all black sheeps are on the left
            for (int i = 0; i < sim_nblacks; i++) {
                System.out.println();
                System.out.print(i + ".x="+sim_sheeps[i].x+",");
                if (sim_sheeps[i].x > 50.0) {
                    return false;
                }
            }
            return true;
        }
        else {
            System.out.println("g.d>0");
            // check if all the white sheeps are on the right
            for (int i = sim_nblacks; i < sim_sheeps.length; i++) {
                if (sim_sheeps[i].x < 50.0) {
                    return false;
                }
            }
            return true;
        }
    }

    private Point manyDogStrategy(Point[] dogs, Point[] sheep) {
      if (sweeps == null) sweeps = new Sweep[dogs.length];
      int idx = id - 1;

      if (sweeps[idx] == null) {
        sweeps[idx] = new Sweep(dogs, sheep, id - 1, globalRecord);
      } else {
        sweeps[idx].current = dogs[idx];
        sweeps[idx].dogs = dogs;
        sweeps[idx].sheep = sheep;
        sweeps[idx].globalRecord = globalRecord;
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
            
            // case 1:
            //     return collect_sheep(dogs, sheeps);
            // case 2:
            //     return move_sheep(dogs, sheeps);
            case 3:
                break;
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

    private Point vector_diff(Point pointA, Point pointB) {
        return new Point(pointB.x - pointA.x, pointB.y - pointA.y);
    }
    private Point vector_add(Point pointA, Point pointB) {
        return new Point(pointA.x + pointB.x, pointA.y + pointB.y);
    }
    private Point vector_add(Point thePoint, double extraLength) {
        double oldLength = vector_length(thePoint);
        double newLength = oldLength + extraLength;
        if (oldLength == 0) {
            return new Point(extraLength, 0.0);
        }
        return new Point(thePoint.x * newLength/oldLength, thePoint.y * newLength/oldLength);
    }
    private double vector_length(Point thePoint) {
        return Math.sqrt(thePoint.x * thePoint.x + thePoint.y * thePoint.y);
    }
    private double vector_length(Point pointA, Point pointB) {
        /*
        double dx = pointA.x - pointB.x;
        double dy = pointA.y - pointB.y;
        return Math.sqrt( dx*dx + dy*dy);
        */
        return vector_length(vector_diff(pointA,pointB));
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
                if(!mode || i < nblacks)
                theList.add(i);
            }
        }
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

    private void remove_other_dogs_targets(ArrayList<Integer> sheepList, Point[] sheeps, Point theDog) {
        if(sheepList.size() == 0) {
            return;
        }
        double min_distance = -1;
        int index = -1;
        for(int i = 0; i < sheepList.size(); i++) {
            double d = vector_length(theDog, sheeps[sheepList.get(i)]);
            if(index == -1 || min_distance > d) {
                index = i;
                min_distance = d;
            }
        }
        System.out.print("SheepChased:" + sheepList.get(index) + ", by the dog at: ");
        print_point(theDog, "");
        sheepList.remove(index);
    }

    private int nearest_sheep_not_chased(ArrayList<Integer> sheepList, Point[] sheeps, Point[] dogs) {
        for (int i = 0; i < id-1; i++) {
            remove_other_dogs_targets(sheepList, sheeps, dogs[i]);
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
            (stateless machine)
            one-dog-per-sheep strategy
        */
        ArrayList<Integer> sheepList = list_free_sheeps(sheeps);
        int newTargetIndex = nearest_sheep_not_chased(sheepList, sheeps, dogs);
        int sweepPhase = 0;
        if( newTargetIndex == -1) {
            sweepPhase = -1;
        }
        else {
            sweepPhase = 0;
        }
        switch(sweepPhase) {
            case 0:
                double keepDistance = 1.0;
                Point theSheep = sheeps[newTargetIndex];
                // move to the position that can push the sheep toward the gate next tick
                Point theSheepPositionNextTick = vector_add(dogs[id-1], vector_add(
                    vector_diff(dogs[id-1],theSheep), max_sheep_speed));
                if (theSheepPositionNextTick.x < 50.0) {
                    next = next_toward_goal(next, globalRecord.gatePoint, max_sheep_speed);
                }
                else if (theSheepPositionNextTick.x == 50.0 &&
                         theSheepPositionNextTick.y < 51.0 &&
                         theSheepPositionNextTick.y > 49.0) {
                    Point thePushPoint = vector_add(theSheepPositionNextTick, new Point(keepDistance, 0.0));
                    next = next_toward_goal(next, theSheepPositionNextTick, max_dog_speed);
                }
                else {
                    Point thePushPoint = projection_from_gate(theSheepPositionNextTick, keepDistance);
                    next = next_toward_goal(next, thePushPoint, max_dog_speed);
                }
                break;
            case -1:
                // idle for other dogs
                next = next_toward_goal(next, globalRecord.idlePoint, max_dog_speed);
                break;
            default:
                break;
        }
        if(next.x < 50.0) {
            next.x = 50.0;
        }
        return next;
    }

}


