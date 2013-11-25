package sheepdog.g4;

import sheepdog.sim.Point;

public class Test {

	public static void main(String args[]){
		Player player=new Player();
		Point from=new Point(54,54);
		Point to=new Point(50,50);
		Point pos=player.positionDogNearSheep(from, to, 1);
		System.out.println(pos.x+" "+pos.y);
	}
}
