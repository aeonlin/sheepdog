package sheepdog.g7;

import sheepdog.sim.Point;

class Record {

    public Point[] sheepsMovement;
    public Point[] dogsMovement;

    // add more if you need it!

    public boolean initialized;
    public int sweepPhase;
    // the index of the sheep being targeted by this dog
    public int targetSheepIndex;

    public double[] gapToSpeed;
    /*
    // whether a sheep is targeted by a dog or not
    public boolean[] sheepIsTargeted;
	public Point[] dogsLastTic;
	*/

    public Point gatePoint;
    public Point idlePoint;

    // for advanced mode: 
    // direction == -1 : sweep black sheep to the left
    // direction == 1: sweep white sheep to the right
    public int gameDirection;

    public Record() {
        sweepPhase = -1;
        targetSheepIndex = -1;
        initialized = false;
       	//Arrays.fill(sheepIsTargeted, false);
       	gatePoint = new Point(50.0, 50.0);
    }

    public void initialize(int id, Point[] dogs, Point[] sheeps) {
    	set_turning_point(id, dogs.length);
    	calculate_gap_to_speed();
        initialized = true;
		/*
    	if( dogsLastTic == null ) {
    		dogsLastTic = new Point[dogs.length];
    	}
    	for(int i = 0; i < dogs.length; i++) {
    		dogsLastTic[i] = new Point(dogs[i].x, dogs[i].y);
    	}
		*/
    }

    public double max_speed_for_gap_width(double gapWidth) {
    	if( gapWidth < 0.0 || gapWidth > 3.98) {
    		return 0.0;
    	}
    	int d = (int)((gapWidth + 0.01)*100.0);
    	return gapToSpeed[d];
    }

    private void calculate_gap_to_speed() {
    	int precision = 10000;
    	gapToSpeed = new double [400];
    	for (int d_i = 0; d_i < 399; d_i++) {
			for (int i = precision ; i >= 0; i--) { 
				double d = (double)(d_i) / 100.0;
				double s = (double)(i) / (double)(precision);
				double x = Math.sqrt(4.00 - d*d/4.00) - s;
				double y = x / Math.sqrt(x*x + d*d/4);
				if ( s <= y ) { // found the maximum possible s
					gapToSpeed[d_i] = s - 0.05;
					break;
				}
			}
		}
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