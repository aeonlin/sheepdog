package sheepdog.g7;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;

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
            case 0:
                strategy_phase = 0;
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

    private double[] where_is_the_herd( Point[] sheeps) {
        // return the center and the radius of the sheep herd
        // herdInfo[0] = radius
        // herdInfo[1] = center_x
        // herdInfo[2] = center_y
        double herdInfo = new double[3];

        return herdInfo;
    }

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog posision

        Point next; // where this dog move

        // if the sheeps are sweeping from the fence,
        // change the strategy_phase
        if (allSheepsAreAwayFromTheFence) {
            strategy_phase = 1;
        }

        return next;
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
