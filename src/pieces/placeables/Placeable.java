package pieces.placeables;


/*
 * The overarching class for all tiles placed on the surface (i.e. units)
 * TODO: Should MainHex and Stone extend this?
 * To be extended by MovingUnit (draft name) as well as ProducerUnit (draft name, i.e. mines)
 * Take in a 
 */

public class Placeable {

    protected int q,r; // The location - in case of multi-hex placeable, the centre hex?
    protected int[] qr; // The location but as an array
    protected boolean destructable = true; // Can it be destroyed?

    public Placeable() {

    }

    public void effect() {
        // Empty function - placing this here to ensure all placeables have it.
    }
    
}
