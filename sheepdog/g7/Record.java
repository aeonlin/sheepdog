package sheepdog.g7;

import sheepdog.sim.Point;

class Record {

    public Point[] sheepsMovement;
    public Point[] dogsMovement;

    // add more if you need it!
    public int sweepPhase;
    public int targetSheepIndex;
    
    public Record() {
        sweepPhase = -1;
    }
}