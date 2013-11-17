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

    private ArrayList<Point> sheepOutsideRadius( Point[] sheeps, double radius) {
        // find the sheep that are outside of the specified radius
        // return a list of sheep that are outside of the radius
        Point center = centerPoint(sheeps);
        // radius is a constant for now
        ArrayList<Point> sheepOutOfBounds = new ArrayList<Point>();

        // place all sheep out of bounds of radius
        for(Point sheep: sheeps){
            boolean outside = (sheep.x - center.x)^2 + (sheep.y - center.y)^2 <= Math.pow((double) radius, 2.0);
            if(outside && sheep.x >= 50.0){
                sheepOutOfBounds.add(sheep);
            }
        } 
        return sheepOutOfBounds;
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

    private Point sweep_sheep( Point[] dogs, Point[] sheeps ) {
        // goal: to sweep all the sheeps from the fence
        // input: the positions of all the sheeps
        // return: the dog position

        Point next; // where this dog move

        // if the sheeps are sweeping from the fence,
        // change the strategy_phase
        if (allSheepsAreAwayFromTheFence) {
            strategy_phase = 1;
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
