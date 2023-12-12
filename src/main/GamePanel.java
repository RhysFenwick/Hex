package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.json.JSONObject;

import inputs.KeyboardInputs;
import inputs.MouseInputs;
import main.levels.Level;
import pieces.components.Menu;
import pieces.hexes.FocusHex;
import pieces.hexes.GrowHex;
import pieces.hexes.Hex;

public class GamePanel extends JPanel implements ActionListener{


    private MouseInputs mouseInputs;

    // Initialise game board and pieces
    private Level lvl;
    private FocusHex mainHex;
    private Hex targetHex;


    // Geometry
    private int hexRows, hexCols, hexGridSize; // Get from the Level
    private List<Hex> hexGrid = new ArrayList<>(); // Should be the list of all hexes!
    private int hexHeight = Hex.height, hexWidth = Hex.width; // To avoid calling them from across the class every time

    private int hudHeight = 30;
    private int offsetX = 20, offsetY = hudHeight + 20;
    public boolean targetSet = false;
    public int menuMargin = 50; 


    // Scrolling 
    private boolean isMouseScrolling = false;
    private int fastScrollWindow = 30;
    private int fastScrollSpeed = 10;
    private int mouseX = 0, mouseY = 0;
    private double lastScroll = System.nanoTime();
    public int frameCount = 0;

    // Whether to update grids
    private boolean updateMidGrid = true;
    private int updateMGthreshold = 500; // # of hexes that need updating before the MG updates
    public boolean updateOverlay = false; // Called on level load
    private Set<int[]> updateList = new HashSet<int[]>();

    // HUD values
    private boolean paused = false;
    private int plantCount; // # of tiles that are old/new plant

    // Map of Images for the tile types
    private Map<String, Image> hexImg = new HashMap<String,Image>();

    // Initialise JSON Object for tiles
    private JSONObject tileJSON;

    // Initialise random generator
    Random rand = new Random();

    // Godmode
    private boolean godMode = false;

    // Menus
    private int menuType = 0; // 0 = "No menu"
    Menu mainMenu = Menu.mainMenu(this); // Can probably have a function in Menu.java that initialises all menus as Map<String,Menu>

    public GamePanel() {

        setLayout(null); // Removes layout, allowing for buttons to be placed 

        //setDoubleBuffered(true); // Might speed it up?
        mouseInputs = new MouseInputs(this);

        addKeyListener(new KeyboardInputs(this));
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);

        // Load in tile properties and fill hexTiles with images

        tileJSON = null;
        try {
            tileJSON = Funcs.readJSON("src/main/resources/TileTypes.JSON");
        } catch (FileNotFoundException e) {
            System.out.println("Tile types file not found");
            e.printStackTrace();
        }

        // Cycle through the JSONObject and read each entry 

        Iterator<String> tileKeys = tileJSON.keys();
        while (tileKeys.hasNext()) {
            String tString = tileKeys.next();
            try {
                hexImg.put(tString,getImage(tString+".png").getScaledInstance(hexWidth, hexHeight, Image.SCALE_DEFAULT));
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

        


        // Get level
        String lvlName = "Level001"; // Hopefully at some point will be passed in or changed!
        loadLevel(lvlName);

        // Initialise starting resources - TODO: Put this in Level
        Map<String, Integer> resourceTracker = new HashMap<>();
        resourceTracker.put("Rock", 0);
        resourceTracker.put("Gold",0);

        setIgnoreRepaint(true);
    }


    // Game functions

    // Set up the level

    public void loadLevel(String lvlName) {
        String lvlString = "src\\main\\levels\\" + lvlName + ".JSON";
         try {
            lvl = new Level(lvlString);
            startLevel(lvl);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        updateOverlay = true;
        updateMidGrid = true;
    }

    public void startLevel(Level lvl) {
        hexGrid = lvl.getHexGrid();
        targetHex = lvl.getTargetHex();
        mainHex = lvl.getMainHex();
        hexRows = lvl.getRows();
        hexCols = lvl.getCols();
        hexGridSize = hexCols * hexRows;
        plantCount = lvl.plantCount;
    }

    public Image getImage(String filename) throws IOException {
        File imgFile = new File("src/main/resources/img/hex/" + filename);
        Image image = ImageIO.read(imgFile);
        return image;
        }
        
    

    // Bomb (TODO - Move to keyboard input section?)
    public void bomb() {
        if (!paused) {
            updateHex(mainHex.getQR(),"Bomb");  
            gameLoop("Bomb"); // Going via gameLoop means a redundant pause check
        }    
    }

    public void levelWipe() {
        for (Hex hex : hexGrid) {
            updateHex(hex.getQR(), "Default");
        }
    }

    // Update hex
    private void updateHex(int[] qr, String type) { // TODO - can this take in int[] OR hex?
        Hex hex = hexGrid.get(indexFromQR(qr[0], qr[1]));
        hex.toType(type);
        hex.beenUpdated = true;   
        updateList.add(qr);     
    }

    // Controlling the game loop

    public void gameLoop(String mainAction) {
        if (!paused) {
            gameTick(mainAction);
        }
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public void togglePause() { 
        if (menuType == 0) {
            paused = paused ^ true;
        }  
    }

    public void toggleMenu() { // If pause, then menu, then unmenu, will unpause - unsure if wanted behaviour
        menuType = 1 - menuType; // Will toggle menuType between 0 and 1
        if (menuType == 0) {
            paused = false;
        }
        else {
            paused = true;
        }
    }

    // GodMode function
    public void mouseClicked(int x, int y) {
        if (!godMode) {
            targetSet = true;
            moveTargetXY(x, y);
        }
        else {
            int[] hexQR = Hex.pix2Hex(x-offsetX, y-offsetY);
            updateHex(hexQR, "Stone");
        }
    }

    // Central game tick

    public void gameTick(String mainAction) {
        
        if (targetSet && mainAction == "Move") { // Doesn't pursue if the player is manually moving it
            mainHex.pursue(targetHex.q, targetHex.r);
        }
        
        // Make a stone trail
        Hex toStone = hexFromQR(mainHex.q, mainHex.r);
        if (toStone.hexType != "Bomb") {
            updateHex(toStone.getQR(),"Stone");
        }

        List<int[]> newGrowth = new ArrayList<>(); // To hold all QRs that turn to plants this tick

        for (int i=0;i<hexGridSize;i++) { // First pass switch - bomb overlaid on top of this
            Hex h = hexGrid.get(i);
            
            // The main "If X do Y bit"
            switch (h.hexType) { 
                case "New Plant":
                    int hq = h.q, hr = h.r;
                    int [] newPlantQR = GrowHex.grow(hq, hr);
                    if (Funcs.isHexInGrid(newPlantQR[0],newPlantQR[1], hexRows, hexCols)) { // Make sure it's not accessing a QR not in hexGrid!
                        String hs = hexFromQR(newPlantQR[0],newPlantQR[1]).hexType;
                        long hGrow = tileJSON.getJSONObject(hs).getLong("growAbility");
                        if (hGrow*10 >= 1 + rand.nextInt(10) ) {
                            newGrowth.add(newPlantQR);
                        }
                    }
                    

                    // Check if old growth
                    int[][] allValidNeighbours = Funcs.neighbourRing(hq,hr,1, hexRows, hexCols); // Returns array of all in-bounds neighbours
                    boolean isSurrounded = true; // Until proven otherwise
                    for (int n=0;n<allValidNeighbours.length;n++) {
                        String ht = hexFromQR(allValidNeighbours[n][0], allValidNeighbours[n][1]).hexType;
                        if (tileJSON.getJSONObject(ht).getLong("growAbility") != 0) { // If there are tiles around it it can grow into
                            isSurrounded = false;
                        }
                    }

                    if (isSurrounded) {
                        // Should only get to here if all neighbours in grid are plants
                        updateHex(h.getQR(), "Old Plant");
                    }
                    // Transfer newGrowth tiles to being plants
                    for (int p=0;p<newGrowth.size();p++) {
                        int [] qr = newGrowth.get(p);
                        Hex np = hexFromQR(qr[0], qr[1]);
                        updateHex(np.getQR(),"New Plant");
                    }
            
                break;
            
                case "Old Plant":
                break;

                case "Stone":

                break;

                default:
                break;
            }
        }

        for (int i=0;i<hexGridSize;i++) { // Second pass - overlaid on top of tile growth. Too slow?
            Hex h = hexGrid.get(i); 

            switch(h.hexType) { 
                case "Bomb":
                    int bq = h.q, br = h.r;
                    int[][] innerRing = Funcs.neighbourRing(bq, br, 1, hexRows, hexCols);
                    int[][] outerRing = Funcs.neighbourRing(bq, br, 2, hexRows, hexCols);

                    updateHex(h.getQR(),"Default");

                    for (int inner=0;inner<innerRing.length;inner++) {
                        int nq = innerRing[inner][0], nr = innerRing[inner][1];
                        updateHex(new int[]{nq,nr},"Default");
                    }

                    for (int outer=0;outer<outerRing.length;outer++) {
                        int nq = outerRing[outer][0], nr = outerRing[outer][1];
                        if (hexFromQR(nq, nr).hexType == "Old Plant") {
                            updateHex(new int[]{nq,nr},"New Plant");
                        }
                        else {
                            updateHex(new int[]{nq,nr},"Default");
                        }
                    }

                break;

                default:
                break;
            }
        }

        // Count totals
        plantCount = 0;
        for (int i=0;i<hexGridSize;i++) {
            if (hexGrid.get(i).hexType == "New Plant" || hexGrid.get(i).hexType == "Old Plant") {
                plantCount += 1;
            }
        }
    }

    // Painting!

    // Initialise buffered images
    
    BufferedImage overlayHexGrid;
    BufferedImage midHexGrid;

    /* The painting loop
    *  Called ~65/second by GameCore
    *  TODO - Simplify g vs g2
    *  TODO - See if I can spin out parts of it to other files (like with menus)
    */

    public void paintComponent(Graphics g) {

        // Initialise BI dimensions - doesn't work outside this for some reason!
        int bufferedWidth = (int) Math.round((1+hexCols) * hexWidth * 0.75);
        int bufferedHeight = (1+hexRows) * hexHeight;

        // Setup
        super.paintComponent(g); // Calls the JPanel paintComponent to do "pre-work" (clean up image artefacts etc)
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2)); // Might be redundant?
        int fontSize = 20;
        Font f = new Font("Monospaced", Font.BOLD, fontSize);
        g2.setFont(f);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);


        // Create overlay hex grid (one at start)

        if (updateOverlay) {
            updateOverlay = false;
            overlayHexGrid = new BufferedImage(bufferedWidth, bufferedHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = overlayHexGrid.createGraphics();
            
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            

            // Create transparent background
            bg.setColor(new Color(0,0,0,0));
            bg.fillRect(0,0, bufferedWidth+1,bufferedHeight);
            
            // Draw hexes
            bg.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            bg.setColor(Color.BLACK);
            for (int i=0;i<hexGridSize;i++) {
                Hex hex = hexGrid.get(i);
                int[][] coords = Hex.hex2Pix(hex.q,hex.r);
                for (int o=0;o<6;o++) {
                    coords[0][o] += hexWidth/2;
                }
                for (int o=0;o<6;o++) {
                    coords[1][o] += hexHeight/2 + 1; // The +1 lets the top of the top row render fully
                }
                //bg.drawImage(blankHex,coords[0][0],coords[1][1],null);
                bg.drawPolygon(coords[0],coords[1],6);
            }
        }

        // Create mid grid as BI - runs every 2 seconds, with intra-run changes being rendered on hex level

        if (updateMidGrid) { // Can this be a  multithread situation? Drops ~2 frames every time and is low-priority
            updateMidGrid = false;
            midHexGrid = new BufferedImage(bufferedWidth, bufferedHeight+1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = midHexGrid.createGraphics();

            // Create transparent background
            mg.setColor(new Color(0,0,0,0));
            mg.fillRect(0,0, bufferedWidth,bufferedHeight+1);

            // Draw hexes
            for (Hex hex : hexGrid) {
                int[] topLeft = hex.topLeft;
                mg.drawImage(hexImg.get(hex.hexType), topLeft[0] + hexWidth, topLeft[1] + hexHeight,null);
                hex.beenUpdated = false;
            }
        }


        g.setClip(0,0,getWidth(),getHeight()); // Only bothers repainting what's shown on screen

        // Try scrolling
        // Note: this links scroll speed to FPS - lag will make movement relatively slower compared to update loop.
        if (isMouseScrolling && timeToScrollYet()) {
            scrollWindow(mouseX, mouseY);
        }
        
        // Draw midgrid
        g2.drawImage(midHexGrid, null, offsetX - hexWidth ,offsetY - hexHeight);


        // Renders updates to midgrid
        int midCounter = 0;
        for (Hex hex : hexGrid) { 
            if (hex.beenUpdated) {
                int[] topLeft = hex.topLeft;
                g.drawImage(hexImg.get(hex.hexType), topLeft[0] + offsetX, topLeft[1] + offsetY,null);
                midCounter++;
            }
        }

        // Check if it's time to update midgrid image and rather than manually painting new tiles
        if (midCounter > updateMGthreshold) {
            updateMidGrid = true;
            updateList.clear();
            System.out.println("Cleared!");
            midCounter = 0;
        }

        // Render main hex
        g.drawImage(hexImg.get("Main"), mainHex.topLeft[0] + offsetX, mainHex.topLeft[1] + offsetY, null);

        // Draw overlay grid
        g2.drawImage(overlayHexGrid, null, offsetX - hexWidth/2 ,offsetY - hexHeight/2 -1);

        

        // Render target outline - maybe change to a circle?
        if (targetSet && (targetHex.q != mainHex.q || targetHex.r != mainHex.r)) { // Only draws if target is set & not on main
            g2.setColor(Color.RED);
            int[][] tCoords = Hex.hex2Pix(targetHex.q,targetHex.r);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Might be redundant?
            g2.drawPolygon(offX(tCoords[0]),offY(tCoords[1]),6);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Might be redundant?

        }
        else if (targetHex.q == mainHex.q && targetHex.r == mainHex.r) {
            targetSet = false;
        }

        // HUD
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), hudHeight);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), hudHeight);

        //Coords
        g.setColor(Color.BLACK);
        g2.drawString(mainHex.q + "," + mainHex.r, 20, 20); // TODO - Make this relative to starting point?

        // Tile counter
        int rHudOffset = 0;
        if (getWidth() < 1) {
            rHudOffset = 406;
        }
        else if (plantCount > 0) { // If plantCount is more than zero
            rHudOffset = getWidth() - (int) Math.floor((Math.floor(Math.log10(plantCount)) + Math.floor(Math.log10(hexGridSize)))*fontSize*0.6) - 2*fontSize;
        }
        else {
            rHudOffset = getWidth() - (int) Math.floor((Math.floor(Math.log10(plantCount+1)) + Math.floor(Math.log10(hexGridSize)))*fontSize*0.6) - 2*fontSize;
        }
        g2.drawString(plantCount + "/" + hexGridSize, rHudOffset, 20);

        // Pause icon
        if (paused) {
            g.setColor(Color.RED);
            g.drawString("Paused", getWidth()/2 - fontSize*2, 20);
        }
        

        // Show menu
        if (menuType != 0) {
            mainMenu.setLocation(menuMargin, menuMargin);
            int menuWidth = getWidth() - menuMargin*2, menuHeight = getHeight() - menuMargin*2;
            mainMenu.setSize(menuWidth, menuHeight);
            add(mainMenu);
        }
        else {
            remove(mainMenu);
        }
        

        // Final line - frame done!
        frameCount++;
    }



    /*
     * Moving functions
     * TODO - Move these to Funcs.java
     */

    // Move the main hex

    public void shiftHexQRS(int qDelt, int rDelt, int sDelt) { // Moves main hex by q,r
        if (Funcs.isHexInGrid(mainHex.q, mainHex.r, hexRows, hexCols)) {
            qDelt += sDelt;
            rDelt -= sDelt;
            mainHex.shiftQR(qDelt,rDelt);
        }
        
    }

    public void moveToHex(int x, int y) {  // Moves main hex to x,y  
        if (Funcs.isHexInGrid(mainHex.q, mainHex.r, hexRows, hexCols)) {
            int[] qr = Hex.pix2Hex(x, y);
            mainHex.moveToHex(qr[0], qr[1]);
        }
    }

    // Move the target

    public void moveTargetQR(int qDelta, int rDelta) {  // Move the target hex by specified Q,R (likely to neighbour)
        // Adjusted to only shift if it's on the same hex as main
        int tq = targetHex.q, tr = targetHex.r;
        if (tq == mainHex.q  && tr == mainHex.r) {
            int newQ = tq + qDelta, newR = tr + rDelta;
            moveTargetToQR(newQ,newR);
        }
    }

    public void moveTargetXY(int x, int y) { // Move the target to a given x,y - called by mouse
        int [] targetQR = Hex.pix2Hex(x-offsetX, y-offsetY);
        moveTargetToQR(targetQR[0], targetQR[1]);
    }

    public void moveTargetToQR(int q, int r) {
        if (Funcs.isHexInGrid(q, r, hexRows, hexCols))
        targetHex.moveToHex(q,r);
    }


    // Move the main hex - called by WASD

    public void moveMainQR(int qDelta, int rDelta) {  // Move the main hex by specified Q,R (likely to neighbour)
        if (!paused) { // Putting this here to avoid duplication in KeyboardInputs
            targetSet = false;
        }
        int newQ = mainHex.q + qDelta, newR = mainHex.r + rDelta;
        moveMainToQR(newQ,newR);
    }

    public void moveMainXY(int x, int y) { // Move the main to a given x,y
        int [] mainHex = Hex.pix2Hex(x-offsetX, y-offsetY);
        moveMainToQR(mainHex[0], mainHex[1]);
    }

    public void moveMainToQR(int q, int r) {
        if (!paused && Funcs.isHexInGrid(q, r, hexRows, hexCols)) {
            mainHex.moveToHex(q,r); 
            gameLoop("Move"); // Going via gameLoop means a redundant pause check
        }
        
    }

    // Timing functions

    public boolean timeToScrollYet() {
        if (System.nanoTime() - lastScroll > 1_000_000_000.0 / GameCore.FPS) {
            lastScroll = System.nanoTime();
            return true;
        }
        else {
            return false;
        }
    }

    // Dragging functions - not currently used
    /* 
    public void dragOver(int x, int y) {
        int[] qr = Hex.pix2Hex(x, y);
        Hex h = hexFromQR(qr[0], qr[1]);
        if (h.hexType != "Stone") { // Only need to change/repaint if it's a new hex!
            h.hexType = "Stone";
            h.color = Color.DARK_GRAY;
        }
    }

    public void clearDrag() { // This function no longer works.
        dragHex.clear();
    }
    */


    // Offset functions

    public int[] offX(int[] x) {
        for (int i=0;i<x.length;i++) {
            x[i] += offsetX;
        }
        return x;
    }

    public int[] offY(int[] y) {
        for (int i=0;i<y.length;i++) {
            y[i] += offsetY;
        }
        return y;
    }

    public void incOX(int x) {
        if (x + offsetX < 30 && x + offsetX - getWidth() + 20 > -(hexCols*hexWidth*0.75)) {
            offsetX += x;
        }
        else if (x + offsetX - getWidth() + 20 < - (hexCols*hexWidth*0.75) && x > 0) { // If it's out of bounds and scrolling inwards
            offsetX += x;
        }
    }

    public void incOY(int y) {
        if (y + offsetY < 30 + hudHeight && y + offsetY - getHeight() + 20 > - (hexRows*hexHeight) ) {
            offsetY += y;
        }
        else if (y + offsetY - getHeight() + 20 < - (hexRows*hexHeight) && y > 0) { // If it's out of bounds and scrolling upwards
            offsetY += y;
        }
    }

    public void updateMouse(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    public void shouldScroll(int x, int y) { // Checks if it *should* scroll and makes this a flag
        if (x < fastScrollWindow || x > getWidth() - fastScrollWindow || y < hudHeight + fastScrollWindow || y > getHeight() - fastScrollWindow) {
            // If it should be scrolling...
            isMouseScrolling = true;
        }
        else {
            isMouseScrolling = false;
        }
        updateMouse(x, y);
    }

    public void scrollWindow(int x, int y) {
        if (x < fastScrollWindow) {
            incOX(fastScrollSpeed);
        }
        else if (x > getWidth() - fastScrollWindow) {
            incOX(-fastScrollSpeed);
        }

        if (y < hudHeight + fastScrollWindow) {
            incOY(fastScrollSpeed);
        }
        else if (y > getHeight() - fastScrollWindow) {
            incOY(-fastScrollSpeed);
        }
    }

    // Maths functions

    public Hex hexFromQR(int q, int r) {
        int i = indexFromQR(q, r);
        return hexGrid.get(i);
    }

    public int indexFromQR(int q, int r) {
        int i = q * hexRows + r + q/2;
        return i;
    }

    public static int[][] list2nestInt(List<int[]> qrList) {
        int[][] outInt = new int[qrList.size()][2];
        for (int i=0;i<qrList.size();i++) {
            outInt[i][0] = qrList.get(i)[0];
            outInt[i][1] = qrList.get(i)[1];
        }
        return outInt;
    }

    public int[] topCorner(int q, int r) { // Returns x,y coords for top left corner of a hex - good for overlaying image on it!
        // Might improve performance by having this pre-calced for each hex as Hex attr?
        Hex hex = hexFromQR(q, r);
        int x = hex.getX()[0];
        int y = hex.getY()[1];
        int[] xy = {x,y};
        return xy;
    }

    // Get button name from getSource()
    public String getName(ActionEvent e) {
        Object object = e.getSource();
        String fullName = object.toString();
        String[] stringEnd = fullName.split("\\[");
        String[] stringMid = stringEnd[1].split(",",2);
        String stringName = stringMid[0];
        return stringName;
    }

    // What to do on button press
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(getName(e));
        switch (getName(e)) {
            case "Next Level Button":
                System.out.println("Next level!");
                paused = false;
                menuType = 0;
                loadLevel("Level002");
            break;

            case "God Mode":
                System.out.println("Editing!");
                levelWipe();
                godMode = true;
                paused = false;
                menuType = 0;
            break;
        
            default:
            break;
        }
    }
}
