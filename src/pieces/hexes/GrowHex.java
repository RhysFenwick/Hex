package pieces.hexes;

import java.awt.Color;
import java.util.Random;

/*
 * TODO: Rename this "New Plant", extends Placeable
 */

public class GrowHex extends Unit{

    private static int growChance = 100_000; // Chance out of a million that a tile will grow this tick

    Color color = Color.GREEN;
    boolean isPlant = true;
    boolean active = true; // Whether or not it has free neighbours

    public GrowHex(int q, int r, int s) {
        super(q, r, s);
    }

    // 

    public static int[] grow(int q, int r) {
        int[][] perms = {{0,1},{0,-1},{1,0},{-1,0},{-1,1},{1,-1}}; // QR permutations for neighbours
        Random rand = new Random();

        int randDir = rand.nextInt(6);
        int growRand = rand.nextInt(1_000_000);
        if (growRand < growChance) {
            q += perms[randDir][0];
            r += perms[randDir][1];
        }
        int[] qr = {q,r};
        return qr;
    }
}
