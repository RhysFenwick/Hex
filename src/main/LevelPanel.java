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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import inputs.KeyboardInputs;
import inputs.MouseInputs;
import main.levels.Level;
import pieces.hexes.Hex;
import pieces.hexes.Unit;

/*
 * The overarching graphics class for a level of the game. Takes in the level it's rendering and creates a visualisation of it.
 * Logic functions inherited from Level or GameCore; this is graphics only.
 */

public class LevelPanel extends JPanel implements ActionListener{

    // Initialise listeners
    private MouseInputs mouseInputs;
    
    // Graphics values
    public int hudHeight = 30;
    public int offsetX = 20;

    public int offsetY = hudHeight + 20;
    private int hexHeight, hexWidth;

    // Scrolling 
    private boolean isMouseScrolling = false;
    private int fastScrollWindow = 30;
    private int fastScrollSpeed = 10;
    private int mouseX = 0, mouseY = 0;
    private double lastScroll = System.nanoTime();
    public int frameCount = 0;

    // Initialise overarching level
    public Level lvl;

    // Initialise target & main hexes
    private Unit mainHex;
    private Hex targetHex;

    // Initialise grid overlay: called once
    public boolean updateOverlay = true;

    // Initialise grid size
    private int gridSize = 0;
    
    // Whether to update midGrid
    private boolean updateMidGrid = true;
    private int updateMGthreshold = 500; // # of hexes that need updating before the MG updates
    private Set<int[]> updateList = new HashSet<int[]>();

    public LevelPanel(Level level) {

    // Relabel references to lvl properties (not necessary but improves readability)
    lvl = level;
    hexHeight = lvl.gc.hexHeight;
    hexWidth = lvl.gc.hexWidth;
    mainHex = lvl.mainHex;
    targetHex = lvl.targetHex;
    gridSize = lvl.terrainGrid.size();

    // Set up listeners - unsure if these should be here or LevelPanel
    mouseInputs = new MouseInputs(this);

    addKeyListener(new KeyboardInputs(this));
    addMouseListener(mouseInputs);
    addMouseMotionListener(mouseInputs);

    }

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
        if (x + offsetX < 30 && x + offsetX - getWidth() + 20 > -(lvl.cols*hexWidth*0.75)) {
            offsetX += x;
        }
        else if (x + offsetX - getWidth() + 20 < - (lvl.cols*hexWidth*0.75) && x > 0) { // If it's out of bounds and scrolling inwards
            offsetX += x;
        }
    }

    public void incOY(int y) {
        if (y + offsetY < 30 + hudHeight && y + offsetY - getHeight() + hudHeight + 20 > - (lvl.rows*hexHeight) ) { // If it's in bounds
            offsetY += y;
        }
        else if (y + offsetY - getHeight() + 20 < - (lvl.rows*hexHeight) && y > 0) { // If it's out of bounds and scrolling up
            offsetY += y;
        }
    }

    // Scrolling

    public void updateMouse(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    public void shouldScroll(int x, int y) { // Checks if it *should* scroll and makes this a flag
        if (x < fastScrollWindow || x > getWidth() - fastScrollWindow || y < hudHeight + fastScrollWindow || y > getHeight() - fastScrollWindow - hudHeight) {
            // If it should be scrolling...
            isMouseScrolling = true;
        }
        else {
            isMouseScrolling = false;
        }
        updateMouse(x, y);
    }

    public void scrollWindow(int x, int y) {

        if (y > getHeight() - hudHeight) { // If the mouse is in the lower HUD
            // Don't scroll!
            return;
        }

        if (x < fastScrollWindow) {
            incOX(fastScrollSpeed);
        }
        else if (x > getWidth() - fastScrollWindow) {
            incOX(-fastScrollSpeed);
        }

        if (y < hudHeight + fastScrollWindow) {
            incOY(fastScrollSpeed);
        }
        else if (y > getHeight() - fastScrollWindow - hudHeight) { 
            incOY(-fastScrollSpeed);
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

    /*
     * Painting!
     * Logic of what/where should be held in Level or GameCore
     * This is just "paint these pixels"
     * TODO: g vs g2
     */

    // Takes a tile index and returns the image for that tile
    // TODO: Overload to take Hex or int[]/int,int qr values
    public Image getImage(int i) {
        Unit hex = lvl.unitGrid.get(i); // Initialise hex
        String hType = hex.type;
        if (hType.equals("Null")) { // If there's no unit on this hex
            hType = lvl.terrainGrid.get(i).type;
        }
        Image img = lvl.gc.hexImg.get(hType);
        return img;
    }

    // Initialise buffered images
    
    BufferedImage overlayHexGrid;
    BufferedImage midHexGrid;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Initialise BI dimensions - doesn't work outside this for some reason!
        int bufferedWidth = (int) Math.round((1+lvl.cols) * hexWidth * 0.75);
        int bufferedHeight = (1+lvl.rows) * hexHeight;

        // Setup
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
            bg.setColor(Color.DARK_GRAY);
            for (int i=0;i<gridSize;i++) {
                Hex hex = lvl.terrainGrid.get(i);
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



        // Create mid grid as BI -  runs when a threshold of changes is reached, with intra-run changes being rendered on hex level

        if (updateMidGrid) { // Can this be a  multithread situation? Drops ~2 frames every time and is low-priority
            updateMidGrid = false;
            midHexGrid = new BufferedImage(bufferedWidth, bufferedHeight+1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = midHexGrid.createGraphics();

            // Create transparent background
            mg.setColor(new Color(0,0,0,0));
            mg.fillRect(0,0, bufferedWidth,bufferedHeight+1);

            // Draw hexes
            for (int i=0;i<gridSize;i++) {
                Unit hex = lvl.unitGrid.get(i);
                mg.drawImage(getImage(i), hex.topLeft[0] + hexWidth, hex.topLeft[1] + hexHeight,null);
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
        // TODO: Integrate UpdateList
        int midCounter = 0;
        for (int i=0;i<gridSize;i++) {
            Unit hex = lvl.unitGrid.get(i);
            if (hex.beenUpdated) {
                g.drawImage(getImage(i), hex.topLeft[0] + offsetX, hex.topLeft[1] + offsetY,null);
                midCounter++;
            }
        }
            

        // Check if it's time to update midgrid image and rather than manually painting new tiles
        if (midCounter > updateMGthreshold) {
            updateMidGrid = true;
            updateList.clear();
            midCounter = 0;
        }

        // Render main hex
        g.drawImage(lvl.gc.hexImg.get("Main"), mainHex.topLeft[0] + offsetX, mainHex.topLeft[1] + offsetY, null);

        // Draw overlay grid
        g2.drawImage(overlayHexGrid, null, offsetX - hexWidth/2 ,offsetY - hexHeight/2 -1);

        // Render target outline - maybe change to a circle?
        if (lvl.targetSet && (targetHex.q != mainHex.q || targetHex.r != mainHex.r)) { // Only draws if target is set & not on main
            g2.setColor(Color.RED);
            int[][] tCoords = Hex.hex2Pix(targetHex.q,targetHex.r);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Might be redundant?
            g2.drawPolygon(offX(tCoords[0]),offY(tCoords[1]),6);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Might be redundant?

        }
        else if (targetHex.q == mainHex.q && targetHex.r == mainHex.r) {
            lvl.targetSet = false;
        }

                /*
         * HUDs
         * Lower HUD separate component
         * TODO: Make the upper HUD the same
         * TODO: Scrolling should stop if hovering over HUD
         */

        // Upper HUD
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
        else if (lvl.plantCount > 0) { // If plantCount is more than zero
            rHudOffset = getWidth() - (int) Math.floor((Math.floor(Math.log10(lvl.plantCount)) + Math.floor(Math.log10(gridSize)))*fontSize*0.6) - 2*fontSize;
        }
        else {
            rHudOffset = getWidth() - (int) Math.floor((Math.floor(Math.log10(lvl.plantCount+1)) + Math.floor(Math.log10(gridSize)))*fontSize*0.6) - 2*fontSize;
        }
        g2.drawString(lvl.plantCount + "/" + gridSize, rHudOffset, 20);

        // Pause icon
        if (lvl.paused) {
            g.setColor(Color.RED);
            g.drawString("Paused", getWidth()/2 - fontSize*2, 20);
        }

        // Lower HUD - goes over the edges by 10px
        lvl.lowerHUD.setLocation(-10, getHeight() - hudHeight); // Same height as top HUD (Not necessarily the same as the menuMargin)
        lvl.lowerHUD.setSize(getWidth()+20,hudHeight+10);
        add(lvl.lowerHUD);


        
        /* TODO: Fix this
        // Show main menu
        if (lvl.menuType != 0) {
            lvl.mainMenu.setLocation(menuMargin, menuMargin);
            int menuWidth = getWidth() - menuMargin*2, menuHeight = getHeight() - menuMargin*2;
            mainMenu.setSize(menuWidth, menuHeight);
            add(mainMenu);
        }
        else {
            remove(mainMenu);
        }
        */
        

        // Final line - frame done!
        frameCount++;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
}
