package pieces.hexes;

import java.lang.Math;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


/* 
 * The basic hex structure.
 * End goal: For this to hold the basic shape/dimension properties common to both terrain and placeables, but nothing else
*/

public class Hex {
    // The dimensions of a hex - everything else flows from these two
    public static int height = 18;
    public static int width = 20;
    public int[] topLeft = {0,0};

    public static int wRadius = width/2;
    public static int hRadius = height/2;
    int [] xCoords = {-wRadius,-wRadius/2,wRadius/2,wRadius,wRadius/2,-wRadius/2};
    int [] yCoords= {0,-hRadius,-hRadius,0,hRadius,hRadius};
    public int q=0, r=0, s=0;
    
    
    
    // Default tile properties
    public boolean beenUpdated = false;
    public String type = "Default"; // "type" used for name of tile for both Unit and BoardHex at this stage!


    public Hex(int startQ, int startR, int startS) { 
        // q is E, r is SW, s is NW
        // Down = +r, -s
        // NE = +q, -s
        // SE = +q, -r
        // s is redundant: can just use q and r (axial coords)
        shiftQR(startQ,startR); 

    }

    // Movement
    /*
     * TODO: Move these to Placeable. The hex stack as a whole won't move - unless Placeable extends Hex (or both co-extend background?)
     */

    public void shiftQR(int qDelt, int rDelt) {
        int newQ = q + qDelt, newR = r + rDelt;
        setQ(newQ);
        setR(newR);
        setS(s - (rDelt + qDelt));
        for (int h=0;h<6;h++) {
            xCoords[h] += qDelt*width*0.75;
            yCoords[h] += qDelt*height/2 + rDelt*height; 
        }
        topLeft[0] = xCoords[0];
        topLeft[1] = yCoords[1];
    }

    public void moveToHex(int moveQ, int moveR) {
        shiftQR(moveQ - q, moveR-r);
    }

    // Overload
    public void moveToHex(int[] moveQR) {
        moveToHex(moveQR[0], moveQR[1]);
    }

    public static int[][] hex2Pix(int aimQ, int aimR) {

        int[][] hexPixCoords = {{-wRadius,-wRadius/2,wRadius/2,wRadius,wRadius/2,-wRadius/2},{0,-hRadius,-hRadius,0,hRadius,hRadius}}; // Origin coords
        for (int h=0;h<6;h++) {
            hexPixCoords[0][h] += aimQ*width*0.75;
            hexPixCoords[1][h] += aimQ*height/2 + aimR*height; 
        }
        return hexPixCoords;
    }

    public static int[] pix2Hex(int x, int y) {
        double dq = x/(width*0.75);
        double dr = (1f*y/height - 2f*x/(3*width)); // Hacky conversion to float
        int[] qr = roundFracQR(dq, dr);
        return qr;
    }

    // Getters & Setters

    public void setQ(int newQ) {
        q = newQ;
    }

    public void setR(int newR) {
        r = newR;
    }

    public void setS(int newS) {
        s = newS;
    }

    public int[] getQR() {
        int[] qr = {q, r};
        return qr;
    }

    public int[] getX() {
        return xCoords;
    }

    public int[] getY() {
        return yCoords;
    }

    // Get QR of fractional hex (for Pix2Hex)

    public static int[] roundFracQR(double dq, double dr) {
        double ds = -dq-dr;

        int inQ = (int) Math.round(dq), inR = (int) Math.round(dr), inS = (int) Math.round(ds);
        
        double q_diff = Math.abs(dq - inQ), r_diff = Math.abs(dr - inR), s_diff = Math.abs(ds - inS);
        
        if (q_diff > r_diff && q_diff > s_diff) {
            inQ = 0 - inR - inS;
        }
        else if (r_diff > s_diff) { 
            inR = 0 - inQ - inS;
        }
        else {
            inS = 0 - inQ - inS;
        }
        
        int[] qr = {inQ,inR};
        return qr;
    }

    // Get repaint box

    public static int[] getRepaintBoxHex(int paintQ, int paintR) {
        int[][] hexBounds = hex2Pix(paintQ, paintR);
        int oldX = hexBounds[0][0], oldY = hexBounds[1][0] - height/2;
        int[] bounds = {oldX,oldY,width*2,height*2};
        return bounds;
    }

    public static int[] getRepaintBoxXY(int x, int y) {
        int[] bounds = {x-width, y-height,width*2,height*2};
        return bounds;
    }

    // Other (game functions)

    public List<Integer> getRandHex(Set<List<Integer>> hexSet) {
        int upperLimit = hexSet.size();
        Random rand = new Random();
        int index = rand.nextInt(upperLimit);
        Iterator<List<Integer>> iter = hexSet.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        List<Integer> rHex = iter.next();
        return rHex;
    }

    public void pursue(int followQ, int followR) {
        int dq = followQ - q, dr = followR - r;
        int mq = Math.abs(dq), mr = Math.abs(dr);
        if (mq > mr ) {
            if (dq > 0) {
                if ((dq - mq%2)/2 + dr >= 0) {
                    shiftQR(1, 0);
                }
                else {
                    shiftQR(1, -1);
                }
                
            }
            else {
                if ((dq - mq%2)/2 + dr < 0) {
                    shiftQR(-1, 0);
                }
                else {
                    shiftQR(-1, 1);
                }
            }
        }
        else if (mr > mq) {
            if (dr > 0) {
                shiftQR(0, 1);
            }
            else {
                shiftQR(0, -1);
            }
        }
        else if (mq == 0 && mr == 0) { // On target!

        }
        else {
            if ((dq)*(dr) > 0) {
                if (dq > 0){
                    shiftQR(1, 0);
                }
                else {
                    shiftQR(-1,0);
                }
            }
            else {
                if (dq > 0) {
                    shiftQR(1, -1);
                }
                else {
                    shiftQR(-1, 1);
                }
            }
        }
    }
}
