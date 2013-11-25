package sheepdog.g7;

// general utilities
import java.io.*;
import java.util.List;
import java.util.*;

import javax.tools.*;

import java.util.concurrent.*;
import java.net.URL;

import sheepdog.sim.Point;

// gui utility
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

enum PType {
    PTYPE_DOG,
    PTYPE_BLACKSHEEP,
    PTYPE_WHITESHEEP
}

public class Sheepdog
{
    static String ROOT_DIR = "sheepdog";

    // recompile .class file?
    static boolean recompile = true;
    
    // print more details?
    static boolean verbose = true;

    // Step by step trace
    static boolean trace = true;

    // enable gui
    static boolean gui = true;

    // default parameters
    static int DEFAULT_DOGS = 1;
    static int DEFAULT_SHEEPS = 10;
    static int DEFAULT_BLACKS = 5;
    static boolean DEFAULT_MODE = false;

    static double WALK_DIST = 10.0; // <10m, >=2m, walk
    static double RUN_DIST = 2.0; // <2m, run
    static double CLUSTER_DIST = 1.0; // <1m, move
    static double WALK_SPEED = 0.1; // 1m/s
    static double RUN_SPEED = 1.0; // 10m/s
    static double CLUSTER_SPEED = 0.05; // 0.5m/s
    static double DOG_SPEED = 2.0; // 20m/s

    static double OPEN_LEFT = 49.0; // left side of center openning
    static double OPEN_RIGHT = 51.0; // right side of center opening

    static int MAX_TICKS = 10000;
    
	// list files below a certain directory
	// can filter those having a specific extension constraint
    //
	static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

  	// compile and load players dynamically
    //


  
    // generate a random position on the given side
    static Point randomPosition(int side) {
        Point point = new Point();
        // generate [0-50)
        point.x = random.nextDouble() * dimension * 0.5;
        // generate [50-100)
        if (side == 1)
            point.x  = point.x + (dimension * 0.5);
        // generate [0-100)
        point.y = random.nextDouble() * dimension;
        return point;
    }

    // compute Euclidean distance between two points
    static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }

    static double vectorLength(double ox, double oy) {
        return Math.sqrt(ox * ox + oy * oy);
    }

    void playgui() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SheepdogUI ui  = new SheepdogUI();
                    ui.createAndShowGUI();
                }
            });
    }


    class SheepdogUI extends JPanel implements ActionListener {
        int FRAME_SIZE = 800;
        int FIELD_SIZE = 600;
        JFrame f;
        FieldPanel field;
        JButton next;
        JLabel label;

        public SheepdogUI() {
            setPreferredSize(new Dimension(FRAME_SIZE, FRAME_SIZE));
            setOpaque(false);
        }

        public void init() {}

        public void actionPerformed(ActionEvent e) {

            if (tick > MAX_TICKS) {
                label.setText("Time out!!!");
                label.setVisible(true);
                // print error message
                System.err.println("[ERROR] The player is time out!");
                next.setEnabled(false);
            }
            else if (endOfGame()) {
                label.setText("Finishes in " + tick + " ticks!");
                label.setVisible(true);
                // print success message
                System.err.println("[SUCCESS] The player achieves the goal in " + tick + " ticks.");
                next.setEnabled(false);
            }
            else {
                playStep();
            }
            repaint();
        }


        public void createAndShowGUI()
        {
            this.setLayout(null);

            f = new JFrame("Sheepdog");
            field = new FieldPanel(1.0 * FIELD_SIZE / dimension);
            next = new JButton("Next"); 
            next.addActionListener(this);
            next.setBounds(0, 0, 100, 50);

            label = new JLabel();
            label.setVisible(false);
            label.setBounds(150, 0, 100, 50);
            label.setFont(new Font("Arial", Font.PLAIN, 15));

            field.setBounds(100, 100, FIELD_SIZE + 50, FIELD_SIZE + 50);

            this.add(next);
            this.add(label);
            this.add(field);

            f.add(this);

            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setVisible(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
        }

    }

    class FieldPanel extends JPanel {
        double PSIZE = 10;
        double s;
        BasicStroke stroke = new BasicStroke(2.0f);
        double ox = 10.0;
        double oy = 10.0;

        public FieldPanel(double scale) {
            setOpaque(false);
            s = scale;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(stroke);

            // draw 2D rectangle
            g2.draw(new Rectangle2D.Double(ox,oy,dimension*s,dimension*s));

            // draw 2D line
            g2.draw(new Line2D.Double(0.5 * dimension * s + ox, 0 + oy,
                                      0.5 * dimension * s + ox, OPEN_LEFT * s + oy));

            g2.draw(new Line2D.Double(0.5 * dimension * s + ox, OPEN_RIGHT * s + oy,
                                      0.5 * dimension * s + ox, dimension * s + oy));

            // draw dogs
            drawDogs(g2);

            // sheeps
            drawSheeps(g2);
        }
        
        public void drawPoint(Graphics2D g2, Point p, PType type) {
            if (type == PType.PTYPE_DOG)
                g2.setPaint(Color.BLUE);
            else if (type == PType.PTYPE_BLACKSHEEP)
                g2.setPaint(Color.BLACK);
            else
                g2.setPaint(Color.ORANGE);

            Ellipse2D e = new Ellipse2D.Double(p.x*s-PSIZE/2+ox, p.y*s-PSIZE/2+oy, PSIZE, PSIZE);
            g2.setStroke(stroke);
            g2.draw(e);
            g2.fill(e);
        }

        public void drawDogs(Graphics2D g2) {
            for (int i = 0; i < ndogs; ++i) {
                drawPoint(g2, dogs[i], PType.PTYPE_DOG);
            }
        }

        public void drawSheeps(Graphics2D g2) {
            for (int i = 0; i < nsheeps; ++i) {
                if (i < nblacks)
                    drawPoint(g2, sheeps[i], PType.PTYPE_BLACKSHEEP);
                else
                    drawPoint(g2, sheeps[i], PType.PTYPE_WHITESHEEP);
            }
        }
    }



    Point getClosestDog(int sheepId) {
        int mindog = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < ndogs; ++i) {
            double d = distance(sheeps[sheepId], dogs[i]);
            if (d < mindist && d != 0) { // ignore overlapping dog
                mindist = d;
                mindog = i;
            }
        }
        return dogs[mindog];
    }
    
    Point moveSheep(int sheepId) {
        Point thisSheep = sheeps[sheepId];
        double dspeed = 0;
        Point closestDog = getClosestDog(sheepId);
        double dist = distance(thisSheep, closestDog);
        assert dist > 0;

        if (dist < RUN_DIST)
            dspeed = RUN_SPEED;
        else if (dist < WALK_DIST)
            dspeed = WALK_SPEED;
        
        // offset from dogs
        double ox_dog = (thisSheep.x - closestDog.x) / dist * dspeed;
        double oy_dog = (thisSheep.y - closestDog.y) / dist * dspeed;

        // offset from clustering
        double ox_cluster = 0, oy_cluster = 0;

        // aggregate offsets then normalize
        for (int i = 0; i < nsheeps; ++i) {
            // skip this sheep itself
            if (i == sheepId) continue;

            double d = distance(thisSheep, sheeps[i]);

            // ignore overlapping sheep
            if (d < CLUSTER_DIST && d > 0) {
                // add an unit vector to x-axis, y-axis
                ox_cluster += ((thisSheep.x - sheeps[i].x) / d);
                oy_cluster += ((thisSheep.y - sheeps[i].y) / d);
            }
        }
        // normalize by length
        double l = vectorLength(ox_cluster, oy_cluster);
        if (l != 0) {
            ox_cluster = ox_cluster / l * CLUSTER_SPEED;
            oy_cluster = oy_cluster / l * CLUSTER_SPEED;
        }

        double ox = ox_dog + ox_cluster, oy = oy_dog + oy_cluster;
        
        Point npos = updatePosition(thisSheep, ox, oy);

        if (verbose) {
            if (!npos.equals(thisSheep)) {
                System.err.format("Sheep %d moves from (%.2f,%.2f) to (%.2f,%.2f): %.2f\n", 
                                  sheepId, thisSheep.x, thisSheep.y, npos.x, npos.y, distance(thisSheep, npos));
            }
            else {
                System.err.format("Sheep %d stays at (%.2f,%.2f)\n", sheepId, thisSheep.x, thisSheep.y);
            }
        }
        return npos;

    }

    // update the current point according to the offsets
    Point updatePosition(Point now, double ox, double oy) {
        double nx = now.x + ox, ny = now.y + oy;

        // hit the left fence        
        if (nx < 0) {
            System.err.println("SHEEP HITS THE LEFT FENCE!!!");

            // move the point to the left fence
            Point temp = new Point(0, now.y);
            // how much we have already moved in x-axis?
            double moved = 0 - now.x;
            // how much we still need to move
            // BUT in opposite direction
            double ox2 = -(ox - moved); 
            return updatePosition(temp, ox2, oy);
        }
        // hit the right fence
        if (nx > dimension) {
            System.err.println("SHEEP HITS THE RIGHT FENCE!!!");

            // move the point to the right fence
            Point temp = new Point(dimension, now.y);
            double moved = (dimension - now.x);
            double ox2 = -(ox - moved);
            return updatePosition(temp, ox2, oy);
        }
        // hit the up fence
        if (ny < 0) {
            System.err.println("SHEEP HITS THE UP FENCE!!!");

            // move the point to the up fence
            Point temp = new Point(now.x, 0);
            double moved = 0 - now.y;
            double oy2 = -(oy - moved);
            return updatePosition(temp, ox, oy2);
        }
        // hit the bottom fence
        if (ny > dimension) {
            System.err.println("SHEEP HITS THE BOTTOM FENCE!!!");

            Point temp = new Point(now.x, dimension);
            double moved = (dimension - now.y);
            double oy2 = -(oy - moved);
            return updatePosition(temp, ox, oy2);
        }

        assert nx >= 0 && nx <= dimension;
        assert ny >= 0 && ny <= dimension;
        
        // hit the middle fence
        if (hitTheFence(now.x, now.y, nx, ny)) {
            System.err.println("SHEEP HITS THE CENTER FENCE!!!");
            //            System.err.println(nx + " " + ny);
            //            System.err.println(ox + " " + oy);

            // move the point to the fence
            Point temp = new Point(50, now.y);
            double moved = (50 - now.x);
            double ox2 = -(ox-moved);
            return updatePosition(temp, ox2, oy);
        }

        // otherwise, we are good
        return new Point(nx, ny);
    }

    // up side is 0
    // bottom side is 1
    // at the fence 2
    int getSide(double x, double y) {
        if (x < dimension * 0.5)
            return 0;
        else if (x > dimension * 0.5)
            return 1;
        else
            return 2;
    }
    int getSide(Point p) {
        return getSide(p.x, p.y);
    }


    boolean hitTheFence(double x1, double y1,
                        double x2, double y2) {
        // on the same side
        if (getSide(x1,y1) == getSide(x2, y2))
            return false;

        // one point is on the fence
        if (getSide(x1,y1) == 2 || getSide(x2,y2) == 2)
            return false;
        
        // compute the intersection with (50, y3)
        // (y3-y1)/(50-x1) = (y2-y1)/(x2-x1)

        double y3 = (y2-y1)/(x2-x1)*(50-x1)+y1;

        assert y3 >= 0 && y3 <= dimension;

        // pass the openning?
        if (y3 >= OPEN_LEFT && y3 <= OPEN_RIGHT)
            return false;
        else
            return true;
    }

    void moveSheeps() {
        // move every sheep
        Point[] newSheeps = new Point[nsheeps];
        for (int i = 0; i < nsheeps; ++i) {
            // compute its velocity vector
            newSheeps[i] = moveSheep(i);
        }
        sheeps = newSheeps;
    }


    boolean validateMove(Point src, Point dst) {
        if (dst.x < 0 || dst.x > dimension)
            return false;
        if (dst.y < 0 || dst.y > dimension)
            return false;
        if (distance(src, dst) > DOG_SPEED)
            return false;
        if (hitTheFence(src.x,src.y,dst.x,dst.y))
            return false;
        return true;
    }

    // detect whether the player has achieved the requirement
    boolean endOfGame() {
        if (!mode) {
            // simple mode
            for (int i = 0; i < nsheeps; ++i) {
                if (getSide(sheeps[i]) == 1)
                    return false;
            }
            return true;
        }
        else {
            // advanced mode
            // all black are in upper side
            for (int i = 0; i < nblacks; ++i) {
                if (getSide(sheeps[i]) == 1)
                    return false;
            }
            // all white are in lower side 
            for (int i = nblacks; i < nsheeps; ++i) {
                if (getSide(sheeps[i]) == 0)
                    return false;
            }
            return true;
        }

    }


    void playStep() {
        tick++;

        // move the player dogs
        Point[] next = new Point[ndogs];
        for (int d = 0; d < ndogs; ++d) {
            next[d] = players[d].move(dogs, sheeps);

            if (verbose) {
                System.err.format("Dog %d moves from (%.2f,%.2f) to (%.2f,%.2f)\n", 
                                  d+1, dogs[d].x, dogs[d].y, next[d].x, next[d].y);
            }

            // validate player move
            if (!validateMove(dogs[d], next[d])) {
                System.err.println("[ERROR] Invalid move, let the dog stay.");
                // for testing purpose
                // let's make the dog stay
                next[d] = dogs[d];
            }
        }
            
        // move sheeps
        moveSheeps();

        // move dogs
        dogs = next;
    }

    void play() {
        while (tick <= MAX_TICKS) {
            if (endOfGame()) break;
            playStep();
        }
        
        if (tick > MAX_TICKS) {
            // Time out
            System.err.println("[ERROR] The player is time out!");
        }
        else {
            // Achieve the goal
            System.err.println("[SUCCESS] The player achieves the goal in " + tick + " ticks.");
        }
    }

    void init() {
        // initialize sheeps
        sheeps = new Point[nsheeps];
        for (int s = 0; s < nsheeps; ++s)
            sheeps[s] = randomPosition(1);
        
        // initialize dogs
        dogs = new Point[ndogs];
        for (int d = 1; d <= ndogs; ++d) {
            double x = 0;
            double y = 1.0 * d / (ndogs+1) * dimension;
            dogs[d-1] = new Point(x, y);
        }

        for (int d = 0; d < ndogs; ++d) {
            players[d].init(nblacks, mode);
        }
    }


    Sheepdog(int players, int nsheeps, int nblacks, boolean mode)
    {
        //this.players = players;
        this.ndogs = players;
        this.nsheeps = nsheeps;
        this.nblacks = nblacks;
        this.mode = mode;

        /*// print config
        System.err.println("##### Game config #####");
        System.err.println("Dogs: " + players.length);
        System.err.println("Sheeps: " + nsheeps);
        System.err.println("Blacks: " + nblacks);
        System.err.println("Mode: " + mode);
        System.err.println("##### end of config #####");
        */
    }

  
    // players
    Player[] players;
    // dog positions
    Point[] dogs;
    // sheep positions
    Point[] sheeps;
    
    // game config
    int ndogs;
    int nsheeps;
    int nblacks;
    boolean mode;

    int tick = 0;

    static double dimension = 100.0; // dimension of the map
    static Random random = new Random();
}
