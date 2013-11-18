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
    private static final double max_dog_speed = 2.0;

    private Record globalRecord;

    private int strategy_phase;
    public Sweep[] sweeps;

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        strategy_phase = -1; // nothing happens now
        Record globalRecord = new Record();
    }

    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps

        Point current = dogs[id-1];
        // basic scenario
        if( mode == false) {
            if  (dogs.length == 1) {
                return basic_strategy(dogs, sheeps);
            }
            else {
                return manyDogStrategy(dogs, sheeps);
            }
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
        Point next = new Point(0.0,0.0);
        switch(strategy_phase) {
            case -1:
                next = move_dogs_to_the_other_side(dogs, sheeps);
            case 0:
                next = sweep_sheep(dogs, sheeps);
                break;
            case 1:
                next = collect_sheep(dogs, sheeps);
                break;
            case 2:
                next = move_sheep(dogs, sheeps);
                break;
            case 3:
            default:
                break;
        }
        return next;
    }

    private ArrayList<Point> sheepOutsideRadius( Point[] sheeps, double radius) {
        // find the sheep that are outside of the specified radius
        // return a list of sheep that are outside of the radius
        Point center = centerPoint(sheeps);
        // radius is a constant for now
        ArrayList<Point> sheepOutOfBounds = new ArrayList<Point>();
    

        // place all sheep out of bounds of radius
        for(Point sheep: sheeps){
            boolean outside = (Math.pow((double) sheep.x - center.x, 2) + Math.pow((double) sheep.y - center.y, 2) <= Math.pow((double) radius, 2.0));
            if(outside && sheep.x >= 50.0){
                sheepOutOfBounds.add(sheep);
            }
        } 
        return sheepOutOfBounds;
    }

    private double vector_length(Point thePoint) {
        return Math.sqrt(thePoint.x * thePoint.x + thePoint.y * thePoint.y);
    }
    private Point next_toward_goal(Point current, Point goal, double speed) {
        Point direction = new Point( goal.x - current.x, goal.y - current.y );
        return next_with_direction(current, direction, speed);
    }
    private Point next_with_direction(Point current, Point direction, double speed) {
        double s = speed;
        if(s > max_dog_speed) {
            s = max_dog_speed;
        }
        double ratio = speed / vector_length(direction);
        Point next = new Point(current.x + ratio * direction.x, 
                                current.y + ratio * direction.y);
        return next;
    }

    private boolean all_dogs_ready (Point[] dogs) {
        for (Point d : dogs) {
            if(d.x <= 50) {
                return false;
            }
        }
        return true;
    }

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
        Point next = new Point(dogs[id-1].x, dogs[id-1].y);
        Point goal = new Point(50.1, 50.0);
        if(all_dogs_ready(dogs)) {
            strategy_phase = 0;
        }
        else {
            next = next_toward_goal(next, goal, 2.0);
            strategy_phase = -1;
        }
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
        if (    (ends[0] > left_limit + distance) &&
                (ends[1] < right_limit - distance) &&
                (ends[2] > up_limit + distance) &&
                (ends[3] < down_limit + distance) ) {
            return true;
        }
        return false;
    }

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog position

        Point next = new Point(0.0, 0.0); // where this dog move

        // if the sheeps are sweeping from the fence,
        // change the strategy_phase
        if ( sheeps_away_from_fence(sheeps, 10.0) ) {
            strategy_phase = 1;
        }
        // if not, keep dog moving along the fence
        // and let the sheeps move from them
        else {

            /*
            "finite state machine"

            specify responsible fence segment
            keep moving back and forth
            clockwise move
            counterclockwise move
            */
        }
        return next;
    }

    private Point collect_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: get sheep inside of desired radius
        // input: the positions of the dogs and the sheeps
        // return: the position of where the dog should move
        // Get sheep outside of desired radius
        // for now, radius is hard coded as 10m
        ArrayList<Point> sheepOutsideRadius = sheepOutsideRadius(sheeps, 10.0);
        Point targetPoint = new Point(0.0,0.0);
        // below is the strategy for just one dog
        if(dogs.length == 1){
            // find the nearest sheep to get to go to the radius
            // closestSheep
            Point closestSheep = closestSheep(sheepOutsideRadius, dogs[0]);
            // Determine the target direction for the sheep to move in
            // target direction is on three points side of circle
            // target direction is determined by slope of line between sheep and center point
            Point centerPoint = centerPoint(sheeps);
            double slope = (closestSheep.x - centerPoint.x) / (closestSheep.y - centerPoint.y);
            Point lastPoint = globalRecord.dogsMovement[globalRecord.dogsMovement.length-1];
            // move along arc!
            // left point of sheep
            Point leftMostPoint = new Point(closestSheep.x - 3, closestSheep.y);
            Point rightMostPoint = new Point(closestSheep.x + 3, closestSheep.y);
            Point bottomMostPoint = new Point(closestSheep.x, closestSheep.y - 3);
            Point topMostPoint = new Point(closestSheep.x, closestSheep.y + 3);

            ArrayList<Point> desiredPoints = new ArrayList<Point>(4);
            desiredPoints.add(leftMostPoint);
            desiredPoints.add(rightMostPoint);
            desiredPoints.add(bottomMostPoint);
            desiredPoints.add(topMostPoint);

            // I'm just going to move from this point to that
            if(slope >= 0){
                targetPoint = closestSheep(desiredPoints, dogs[0]);
            }
            else{
                // DO SOMETHING
            }
            return targetPoint;
        }

        // TODO: below is the strategy for more than just one dog
        return new Point(0.0,0.0);
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
}

class Record {

    public Point[] sheepsMovement;
    public Point[] dogsMovement;

    // add more if you need it!

}

