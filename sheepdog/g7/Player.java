package sheepdog.g7;

import sheepdog.sim.Point;
import java.util.*;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private int sim_nblacks;
    private boolean mode;

    private static final double max_dog_speed = 1.9999;
    private static final double max_sheep_speed = 0.9999;

    private Record globalRecord;
    private TreeStrategy[] treeStrategies;

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
        sheeps = copy_point_array(sim_sheeps, globalRecord.gameDirection, true);
        dogs = copy_point_array(sim_dogs, globalRecord.gameDirection, false);

        assign_game_direction_and_initialization(dogs, sim_dogs, sheeps, sim_sheeps);

        Point current = dogs[id-1];
        Point next = new Point();

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
                next = basic_strategy(dogs, sheeps);
            } else if (dogs.length < 35){
                next = treeStrategies[id-1].nextMove();
            } else {
                next = manyDogStrategy(dogs, sheeps);
            }
        }
        // advanced scenario
        else {
            //return basic_strategy(dogs, sheeps);
            // return basic_strategy(dogs, sheeps);
            if  (dogs.length >= nblacks || globalRecord.gameDirection == 1) {
                next =  basic_strategy(dogs, sheeps);
            } else 
                next =  treeStrategies[id-1].nextMove();
        }
        
        if (globalRecord.gameDirection > 0) {
            next.x = 100.0 - next.x;
        }
        
        return next;
    }

    private void assign_game_direction_and_initialization(
        Point[] dogs, Point[] sim_dogs, Point[] sheeps, Point[] sim_sheeps) {
        Point current = new Point(sim_dogs[id-1].x, sim_dogs[id-1].y);
        //sheeps = copyPointArray(sim_sheeps, -1, true);
        //dogs = copyPointArray(sim_dogs, -1, false);     
        if (all_target_sheep_moved(globalRecord.gameDirection, sim_sheeps) && mode) {
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
            sheeps = copy_point_array(sim_sheeps, globalRecord.gameDirection, true);
            dogs = copy_point_array(sim_dogs, globalRecord.gameDirection, false);
            current = dogs[id-1];
            treeStrategies = new TreeStrategy[sim_dogs.length];
            treeStrategies[id-1] = new TreeStrategy(current, sheeps, dogs, id, nblacks);
            System.out.println("Sys.Reset.done: new dir = " + globalRecord.gameDirection);
        }
        if(!mode) {
            globalRecord.gameDirection = -1;
        }
    }

    private Point[] copy_point_array(Point[] sim_a, int gameDirection, boolean isSheep) {
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

    private boolean all_target_sheep_moved(int gameDirection, Point[] sim_sheeps) {
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
            default:
                break;
        }
        return new Point(0.0, 0.0);
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
            next = Geometry.next_toward_goal(next, goal, max_dog_speed, max_dog_speed);
        }
        return next;
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
            if( Geometry.vector_length(theSheep, new Point(50.0, 49.0)) < 1.0 ||
                Geometry.vector_length(theSheep, new Point(50.0, 51.0)) < 1.0 ) {
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
            if( Geometry.vector_length(theSheep, new Point(50.0, 49.0)) > 1.0 &&
                Geometry.vector_length(theSheep, new Point(50.0, 51.0)) > 1.0 ) {
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
            distance = Geometry.vector_length(d);
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
            double d = Geometry.vector_length(theDog, sheeps[sheepList.get(i)]);
            if(index == -1 || min_distance > d) {
                index = i;
                min_distance = d;
            }
        }
        System.out.print("SheepChased:" + sheepList.get(index) + ", by the dog at: ");
        Geometry.print_point(theDog, "");
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
                Point theSheepPositionNextTick = 
                    Geometry.vector_add(dogs[id-1], 
                        Geometry.vector_add( Geometry.vector_diff(dogs[id-1],theSheep), 
                                                max_sheep_speed));
                if (theSheepPositionNextTick.x < 50.0) {
                    next = Geometry.next_toward_goal(next, globalRecord.gatePoint, max_sheep_speed, max_dog_speed);
                }
                else if (theSheepPositionNextTick.x == 50.0 &&
                         theSheepPositionNextTick.y < 51.0 &&
                         theSheepPositionNextTick.y > 49.0) {
                    Point thePushPoint = Geometry.vector_add(theSheepPositionNextTick, new Point(keepDistance, 0.0));
                    next = Geometry.next_toward_goal(next, theSheepPositionNextTick, max_dog_speed, max_dog_speed);
                }
                else {
                    Point thePushPoint = Geometry.projection_from_gate(theSheepPositionNextTick, keepDistance);
                    next = Geometry.next_toward_goal(next, thePushPoint, max_dog_speed, max_dog_speed);
                }
                break;
            case -1:
                // idle for other dogs
                next = Geometry.next_toward_goal(next, globalRecord.idlePoint, max_dog_speed, max_dog_speed);
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
