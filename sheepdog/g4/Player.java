package sheepdog.g4;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;
    private int move_num;
    private double distanceInTree[];
    public  Point [] sheeps,dogs;
    Tree myTree=new Tree(this);
    static double DOG_SPEED = 2.0;
    static Point gate= new Point(50,50);
    double epsilon=1e-6;
    
    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        move_num=0;
    }
    
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
    	
    	sheeps=updateToNewSheep(sheeps,dogs);
    	
        move_num++;
        this.sheeps=sheeps;
        this.dogs=dogs;
        int [] father = null;
        distanceInTree=new double[sheeps.length];

        if(move_num<26)
        {
            
            dogs[id-1]=initial_dog_move(dogs[id-1]);

        }
        if(dogs[id-1].x>=50){
        	father=myTree.buildTree(sheeps);
            print_array(father);
            findDistanceInTree(father, -1 , 0.0);
            print_array(distanceInTree);
            int[] farthest=findFarthest(1);

            int sheepid=farthest[0];
            
            
			
            if(sheepid!=-1 && father[sheepid]!=-1 && distance(sheeps[sheepid], sheeps[father[sheepid]])<1){
            	sheepid=father[sheepid];
            	System.out.println("worked!!!");
            }
            
            
            
            Point fromPoint= sheeps[sheepid];
            Point toPoint=null;
            if(father[sheepid]==-1){
            	toPoint=gate;
            }else toPoint= sheeps[father[sheepid]];
            
            Point dogPoint = positionDogNearSheep(fromPoint, toPoint, 1.99);
            //Point dogPoint = positionDogNearSheep(toPoint, fromPoint, 0.5);
            
            System.out.println("from "+fromPoint.x+" "+fromPoint.y);
            System.out.println("to "+toPoint.x+" "+toPoint.y);
            System.out.println("dog point "+dogPoint.x+" "+dogPoint.y);
            
            Point motion=new Point(dogPoint.x-dogs[id-1].x, dogPoint.y-dogs[id-1].y);
            double motionDist=vectorLengthPoint(motion);
            if (motionDist>DOG_SPEED) {
            	motion.x=motion.x/motionDist*(DOG_SPEED-epsilon);
                motion.y=motion.y/motionDist*(DOG_SPEED-epsilon);
			}
            //System.out.println("dist of motion is "+vectorLengthPoint(motion));
            Point dogPos=new Point(dogs[id-1].x+motion.x, dogs[id-1].y+motion.y);
            //System.out.println("dog to sheep dist "+distance(dogPos, sheeps[0]));
            return dogPos;
        }
        /*Point current = dogs[id-1];
        System.out.println(dogs.length);
        return current;*/

        
        
        return dogs[id-1];
    }

    private Point[] updateToNewSheep(Point[] sheeps,Point[] dogs) {
		Sheepdog sheepdog=new Sheepdog(dogs.length, sheeps.length, nblacks, false);
		sheepdog.sheeps=sheeps;
		sheepdog.dogs=dogs;
		
		Point[] newSheeps = new Point[sheeps.length];
        for (int i = 0; i < sheeps.length; ++i) {
            // compute its velocity vector
            newSheeps[i] = sheepdog.moveSheep(i);
        }
		return newSheeps;
	}

	private int[] findFarthest(int k) {
    	double[] d=new double[distanceInTree.length];
    	for (int j = 0; j < d.length; j++) {
			d[j]=distanceInTree[j]-distance(dogs[id-1], sheeps[j])*1.5;
		}
    	for (int j = 0; j < d.length; j++) {
			if (sheeps[j].x<50)
				d[j]=-100;
		}
    	int[] index=new int[distanceInTree.length];
    	for (int i = 0; i < index.length; i++) {
			index[i]=i;
		}
		for (int i = 0; i < distanceInTree.length; i++) {
			for (int j = i+1; j < distanceInTree.length; j++) {
				if (d[i]<d[j]) {
					double t=d[i];
					d[i]=d[j];
					d[j]=t;
					int tt=index[i];
					index[i]=index[j];
					index[j]=tt;
				}
			}
		}
		int ret[]=new int[k];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=index[i];
		}
		return ret;
	}

	public void print_array(double[] array){

        for (int i=0; i<array.length;i++ ) {

            System.out.print(array[i] + ",  ");
            
        }

        System.out.println();


    }


    public void print_array(int[] array){

        for (int i=0; i<array.length;i++ ) {

            System.out.print(array[i] + ",  ");
            
        }

        System.out.println();


    }
    
    static Point initial_dog_move(Point dog){

            double dist_from_gate=distance(gate, dog);

            double move_distance=dist_from_gate;

            if(dist_from_gate<DOG_SPEED)

                move_distance=dist_from_gate;
            else

                move_distance=DOG_SPEED;

            dog.x+=move_distance;

            return dog;
            
    }
    
    // compute Euclidean distance between two points
    static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }
    // return the length of a vector
    static double vectorLength(double ox, double oy) {

        return Math.sqrt(ox * ox + oy * oy);
    }

    static double vectorLengthPoint(Point p) {

        return Math.sqrt(p.x*p.x + p.y*p.y);
    }
    
    //vipul
    static Point positionDogNearSheep(Point from, Point to, double BUFFER){
        // Calculate Slope, Intercept and Initial Distance
        double slope = (from.y - to.y)/(from.x - to.x);
        //System.out.println("Slope: " + slope);
        double initialDistance = distance(from, to);
        //System.out.println("Initial Dist: " + initialDistance);

        // Final Coordinates
        Point finalDestination = new Point();
        finalDestination.x = ((from.x - to.x)*(initialDistance + BUFFER)/(initialDistance)) + to.x;
        finalDestination.y = ((from.y - to.y)*(initialDistance + BUFFER)/(initialDistance)) + to.y;

        // Error Checking
        if (finalDestination.x > 100)
        {
		finalDestination.x = 100;
		finalDestination.y = slope*(100 - to.x) + to.y;
        }

        if (finalDestination.x < 50)
        {
	       finalDestination.x = 50;
	       finalDestination.y = slope*(50 - to.x) + to.y;
        }

        if (finalDestination.y > 100){
            finalDestination.y = 100;
            finalDestination.x = (100 - to.y)/slope + to.x;
        }

        if (finalDestination.y < 0){
            finalDestination.y = 0;
            finalDestination.x = (0 - to.y)/slope + to.x;
        }

        //System.out.println("X: " + finalDestination.x);
        //System.out.println("Y: " + finalDestination.y);

        return finalDestination;
    }
      

    private void findDistanceInTree(int[] father, int index, double distance){

        double temp_distance = 0.0;

        //System.out.println("Print index: " + index + "  " + "Print distance:" + distance);

        for (int i=0;i<sheeps.length;i++) {

            if (father[i] == index) {

                if(index == -1)

                    temp_distance = distance(gate, sheeps[i]);

                else

                    temp_distance = distance(sheeps[index], sheeps[i]);

                distanceInTree[i] = distance+temp_distance;

                findDistanceInTree(father, i, distance+temp_distance);
                
            }  
            
        }

         return;

    }

}
