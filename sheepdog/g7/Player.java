package sheepdog.g7;

import sheepdog.sim.Point;

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
                return current;    
            }
        }
        // advanced scenario
        else {
            return current;
        }
    }

    private Point basic_strategy(Point[] dogs, Point[] sheeps) {
        Point next;
        switch(strategy_phase) {
            case -1:
                next = move_dogs_through_gate(dogs, sheeps);
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

        // place all sheep out of bounds of radius
        for(Point sheep: sheeps){
            boolean outside = (sheep.x - center.x)^2 + (sheep.y - center.y)^2 <= Math.pow((double) radius, 2.0);
            if(outside && sheep.x >= 50.0){
                sheepOutOfBounds.add(sheep);
            }
        } 
        return sheepOutOfBounds;
    private boolean all_dogs_ready Point[] dogs) {
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
            sumOfX += point.x;
            sumOfY += point.y;
        }
        averageX /= sheeps.length;
        averageY /= sheeps.length;
        return new Point(averageX, averageY);
    }

    private Point move_dogs_to_the_other_side( Point[] dogs, Point[] sheeps) {
        Point next = new Point(dogs[id-1].x, dogs[id-1].y);
        Point goal = new Point(50.1, 50.0);
        if(all_dogs_ready) {
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
        return return false;
    }

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog position

        Point next; // where this dog move

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
        Point[] sheepOutsideRadius = sheepOutsideRadius(sheeps, 10.0);
        // below is the strategy for just one dog
        if(dogs.length == 1){
            // find the nearest sheep to get to go to the radius
            // closestSheep
            Point closestSheep = closestSheep(sheepOutsideRadius, dogs[0]);
            // Now move the dog in the direction of the closest sheep towards the radius
            
        }

        // TODO: below is the strategy for more than just one dog
        Point next;
        return next;
    }

    // find the closest sheep depending on the sheeps passed in
    // return the coordinates of the closest sheep
    private Point closestSheep(Point[] sheeps, Point dog){
        Point closestSheep;
        double closestDistance = 1000.0;
        for(Point sheep: sheeps){
            double minusXs = dog.x - sheep.x;
            double minusYs = dog.y - sheep.y;
            double distance = Math.sqrt((Math.pow(minusXs)) + (Math.pow(minusYs)));
            if(distance < closestDistance){
                closestDistance = distance;
                closestSheep = sheep;
            }
        }
        return closestSheep;
    }

    private Point move_sheep( Point[] dogs, Point[] sheeps ) {
        Point next;
        return next;
    }
}

class Record {

    public Point[] sheepsMovement;
    public Point[] dogsMovement;

    // add more if you need it!

}

