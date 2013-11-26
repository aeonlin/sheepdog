package sheepdog.g7;

import sheepdog.sim.Point;

class Record {

    public Point[] sheepsMovement;
    public Point[] dogsMovement;

    // add more if you need it!

    public int sweepPhase;
    // the index of the sheep being targeted by this dog
    public int targetSheepIndex;

    /*
    // whether a sheep is targeted by a dog or not
    public boolean[] sheepIsTargeted;
	public Point[] dogsLastTic;
	*/

    public Point gatePoint;
    public Point idlePoint;

    public Record() {
        sweepPhase = -1;
        targetSheepIndex = -1;
       	//Arrays.fill(sheepIsTargeted, false);
       	gatePoint = new Point(50.0, 50.0);
    }

    public void initialize(int id, Point[] dogs, Point[] sheeps) {
    	set_turning_point(id, dogs.length);
		/*
    	if( dogsLastTic == null ) {
    		dogsLastTic = new Point[dogs.length];
    	}
    	for(int i = 0; i < dogs.length; i++) {
    		dogsLastTic[i] = new Point(dogs[i].x, dogs[i].y);
    	}
		*/
    }

    private void set_turning_point (int id, int numberOfDogs) {
		double yy = (double)(id) * 4.0 / (double)(numberOfDogs+1) + 45.0;
		if ( yy > 47.0 ) {
			yy += 6.0;
		}
        if(idlePoint == null ) {
            idlePoint = new Point( 50.0 , yy); 
        }
        else {
            idlePoint.x = 50.0;
            idlePoint.y = yy;
        }
    }

    /*
    public void updateTargetedSheep(Point[] dogs, Point[], sheeps) {
    	for(int i = 0; i < dogs.length; i++) {
    		if ( dogsLastTic[i] < 50.0 && dogs[i] >= 50.0 ) {
    			for ( int j = 0; j < sheeps.length; j++) {

    			}
    		}
    	}
    }

    public void updateLastTic(Point[] dogs, Point[] sheeps) {
    	for(int i = 0; i < dogsLastTic.length; i++ ) {
    		dogsLastTic[i] = dogs[i];
    	}
    }
    */
}