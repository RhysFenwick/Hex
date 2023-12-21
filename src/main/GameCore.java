package main;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import main.levels.Level;
import pieces.hexes.Hex;

/*
 * Currently mostly just a holder for FPS/TPS counters
 * TODO: Move core game functions here! 
 * Final model goal: World logic happens here, GamePanel just for display
 */

public class GameCore{


    public static int FPS = 65; // Would like to make this not static! Used for GamePanel scrolling.
    public static int TPS = 10; // Game ticks per second

    // Initialise hex size
    int hexHeight = Hex.height;

    int hexWidth = Hex.width;

    // Initialise Image Map
    public Map<String, Image> hexImg = new HashMap<String,Image>();

    // Initialise first level (logic and graphics)
    public Level lvl;
    public LevelPanel lPanel;

    // ArrayList for ordered list of terrain types
    public ArrayList<String> tileList = new ArrayList<>();

    public GameCore() {

        // Set up hexImg
        // TODO: Combine these into one!
        fillImageMap(hexImg, "TerrainTypes");
        fillImageMap(hexImg, "PlaceableTypes");
        
        // Set up first level
        lvl = new Level("Level001", this);
        lPanel = lvl.lPanel;

    
        System.setProperty("sun.java2d.opengl", "true"); // Should speed up rendering!
        lPanel.setDoubleBuffered(true); // Maybe speed up?
        GameWindow gameWindow = new GameWindow(lvl);
        gameWindow.getAlignmentX(); // Exists only to avoid the yellow squiggle
        lPanel.requestFocus(); // Gets focus to pick up keypresses
        run();
    }

    // The main "tick" loop - constantly running!

    public void run() {

        // Set FPS loop variables
        double timePerFrame = 1_000_000_000.0 / FPS;
        long lastFrame = System.nanoTime();
        
        // Set FPS counter variables
        long lastCheck = System.currentTimeMillis();

        // Set TPS counter
        double timePerTick = 1_000_000_000.0 / TPS;
        long lastTick = System.nanoTime();

        // The core FPS/TPS loop
        while (true) {
            long now = System.nanoTime();

            //TPS - TODO: Add catch-up capability if it misses a beat
            if (now - lastTick > timePerTick) {
                //gamePanel.gameLoop("Move");
                lastTick = now;
            }

            // FPS
            if (now - lastFrame > timePerFrame) {
                lPanel.repaint();
                lastFrame = now; // Calls the Sys anew?
            }

            // FPS Counter
            if (System.currentTimeMillis() - lastCheck > 1000) {
                lastCheck = System.currentTimeMillis();
                //System.out.println("FPS: " + gamePanel.frameCount);
                //gamePanel.frameCount = 0;
            }
        }
    }


    // Function to initialise image map(s).
    public void fillImageMap(Map<String,Image> map, String jsonName) {

        JSONObject tileJSON = Funcs.readJSON("src/main/resources/" +  jsonName + ".JSON");

        // Cycle through the JSONObject and read each entry 

        Iterator<String> tileKeys = tileJSON.keys();
        while (tileKeys.hasNext()) {
            String tString = tileKeys.next();
            if (!tString.equals("Null")) { // The "Null" Placeable doesn't have an image
                tileList.add(tString); // Filling the ArrayList
                map.put(tString,getImageFromFile(tString));
            }   
        }
    }

    public Image getImageFromFile(String filename){
        // Returns a tile-sized image
        Image image = null;
        try {
            File imgFile = new File("src/main/resources/img/terrain/" + filename + ".png");
            image = ImageIO.read(imgFile);
        }
        catch (IOException e) {
            System.out.println(filename);
            System.out.println(e);
        }  
        return image.getScaledInstance(hexWidth, hexHeight, Image.SCALE_DEFAULT);
    }

    public void loadLevel(String string) {
    }
}
