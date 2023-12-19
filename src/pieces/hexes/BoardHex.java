package pieces.hexes;


/*
 * This is a "stack": Terrain at the bottom, placeable on top. Extends basic hex function, but also contains another hex within it!
 */

public class BoardHex extends Hex{

    public String terrain = "Default"; // The bottom layer

    // Image - Should this be image, or location of image instead to save on memory?
    public String imgName; // The filename for the image (minus extension or directory, e.g. just "New Plant")


    public BoardHex(int startQ, int startR, int startS) {
        super(startQ, startR, startS);

    }

    // Setters

    // Changes the terrain (not placeable!)
    public void toTerrain(String hTerrain) {
        terrain = hTerrain;
    }
}
