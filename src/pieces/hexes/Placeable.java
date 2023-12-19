package pieces.hexes;

/*
 * The overarching class for all tiles placed on the surface (i.e. units)
 * TODO: Should MainHex and Stone extend this? (Yes they should)
 * 
 * 
 */

public class Placeable extends Hex{

    protected boolean destructible = true; // Can it be destroyed?
    public String name = "Null"; // To be overridden!

    public Placeable(int startQ, int startR, int startS) {
        super(startQ, startR, startS);
    }

    public void doAction() {
        // Empty function - placing this here to ensure all placeables have it.
        // Boils down to "What does this do on a game tick"
    }
    
}
