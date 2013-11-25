package sheepdog.g4;

import sheepdog.sim.Point;

public class Tree {
	Player player;
	public Tree(Player player) {
		this.player=player;
	}
	int[] buildTree(Point[] sheeps){
		int n=sheeps.length;
		Point root=new Point(50,50);
		double[] disToRoot=new double[n];
		for (int i = 0; i < disToRoot.length; i++) {
			disToRoot[i]=player.distance(root, sheeps[i]);
		}
		int[] father=new int[n];
		for (int i = 0; i < sheeps.length; i++) {
			double nearest=disToRoot[i];
			father[i]=-1;
			for (int j = 0; j < n; j++) {
				if (i==j || ( disToRoot[j]>=disToRoot[i] ))
					continue;
				if (nearest>player.distance(sheeps[i], sheeps[j])) {
					father[i]=j;
					nearest=player.distance(sheeps[i], sheeps[j]);
				}
			}
		}
		return father;
	}
}
