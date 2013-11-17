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

    private int strategy_phase;

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;

        strategy_phase = -1; // nothing happens now
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

    private Point next_toward_goal(Point current, Point goal, double distance) {
        Point direction = new Point( goal.x - current.x, goal.y - current.y );
        return next_with_direction(current, direction, distance);
    }
    private Point next_with_direction(Point current, Point direction, double distance) {
        double d = distance;
        if(d > max_dog_speed) {
            d = max_dog_speed;
        }

    }

    private boolean all_dogs_ready Point[] dogs) {


    }

    private Point move_dogs_to_the_other_side( Point[] dogs, Point[] sheeps) {
        if(all_dogs_ready) {
            return dogs[id-1];
            strategy_phase = 0;
        }
        else {

            strategy_phase = -1;
        }
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

    private sweepDirection;

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog posision

        Point next; // where this dog move

        // if the sheeps are sweeping from the fence,
        // change the strategy_phase
        if ( sheeps_away_from_fence(sheeps, 10.0) ) {
            strategy_phase = 1;
        }
        // if not, keep dog moving along the fence
        // and let the sheeps move from them
        else {

            "finite state machine"

            specify responsible fence segment
            keep moving back and forth
            clockwise move
            counterclockwise move

        }
        return next;
    }


    private double[] where_is_the_herd( Point[] sheeps) {
        // return the center and the radius of the sheep herd
        // herdInfo[0] = radius
        // herdInfo[1] = center_x
        // herdInfo[2] = center_y
        double herdInfo = new double[3];


        return herdInfo;
    }

    private Point collect_sheep( Point[] dogs, Point[] sheeps ) {
        Point next;
        return next;
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

