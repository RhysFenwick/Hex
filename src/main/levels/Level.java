package main.levels;

import org.json.JSONArray;
import org.json.JSONObject;


import main.Funcs;
import main.GameCore;
import main.LevelPanel;
import pieces.hexes.Hex;
import pieces.hexes.Unit;
import pieces.components.HUD;
import pieces.components.Menu;
import pieces.hexes.BoardHex;
import pieces.hexes.GrowHex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


/*
 * The overarching class for each level
 * 
 */

public class Level {

    // Game control values
    public boolean paused = false;
    private boolean godMode = false;
    public int menuType = 0; // 0 = "No menu"
    public int editTerrain = 0;
    public String dropTile;

    // Gameplay values
    public int plantCount; // # of tiles that are old/new plant
    
    // Grid values
    public List<BoardHex> terrainGrid = new ArrayList<>(); // List of all terrain hexes
    public List<Unit> unitGrid = new ArrayList<>(); // List of all unit hexes
    public int cols, rows;
    public Unit mainHex;
    public Hex targetHex;
    public Map<String,Integer> tileCounters = new HashMap<>();
    private String[] plantNames = {"New Plant", "Old Plant"};
    public List<String> unitList, terrainList; // Lists of all types

    // Initialise GameCore
    public GameCore gc;

    // Initialise LevelPanel
    public LevelPanel lPanel;

    // Initialise menus
    public Menu mainMenu; // Can probably have a function in Menu.java that initialises all menus as Map<String,Menu>
    public HUD lowerHUD; // TODO - If all initialised at 0, can remove those parameters

    // Initialise target hex
    public boolean targetSet = false;

    // Initialise random generator
    Random rand = new Random();

    // Initialise JSONs of terrain/unit properties
    JSONObject terrainJSON;
    JSONObject unitJSON;


    public Level(String levelJSONName, GameCore gameCore){

        // Inherit GameCore
        gc = gameCore;

        // Read level
        JSONObject lvl = Funcs.readJSON("src/main/levels/" + levelJSONName + ".JSON");

        // Read in JSONs of terrain/placeables
        // TODO: Should there be one JSON for terrain and placeables?
        terrainJSON = Funcs.readJSON("src/main/resources/TerrainTypes.JSON");
        unitJSON = Funcs.readJSON("src/main/resources/PlaceableTypes.JSON");

        // Fill ArrayLists of tile type names
        terrainList = Funcs.keyListFromJSON(terrainJSON);
        unitList = Funcs.keyListFromJSON(unitJSON);
        unitList.remove("Null");


        // Read important variables from JSON
        cols = lvl.getInt("cols");
        rows = lvl.getInt("rows");
        int[] mainHexQR = convertArray(lvl.getJSONArray("mainHexQR"));


        // NEW: Get terrain tile map
        Map<String, int[][]>  terrainMap = extractTiles(lvl.getJSONObject("mapTerrain"));

        // NEW: Get placeables tile map
        Map<String, int[][]>  unitMap = extractTiles(lvl.getJSONObject("mapPlaceables"));

        // Set up the map
        initialiseGrids(cols,rows); // Fill the grids with blanks
        fillhexGrid(terrainMap, "terrainGrid"); // This line will also fill tileCounters
        fillhexGrid(unitMap, "unitGrid"); // Should fill unitMap once that's sorted
        plantCount = sumValues(tileCounters, plantNames);
        mainHex = new Unit(mainHexQR[0], mainHexQR[1],-mainHexQR[0]-mainHexQR[1]); 
        targetHex = new Hex(mainHexQR[0], mainHexQR[1],-mainHexQR[0]-mainHexQR[1]); // Initialise as on target
        unitFromQR(mainHex.q, mainHex.r).type = "Wall"; // Only for stone trail!        

        // Set up lPanel
        lPanel = new LevelPanel(this);

        // Set up menus
        mainMenu = Menu.mainMenu(this);
        lowerHUD = new HUD(0,0,0,0,this);
    }


    private void initialiseGrids(int cols, int rows) {
        for (int col=0;col<cols;col++) {   // Fill hexGridQR with QR coords - might not render all of them!
            for (int row=0;row<rows;row++) {
                // hexGridQR[col*rows + row] = new int[] {col,row-col/2};
                terrainGrid.add(new BoardHex(col,row - col/2,-col-(row-col/2)));
                unitGrid.add(new Unit(col,row - col/2,-col-(row-col/2)));
            }
        }
    }


    private int[] convertArray(JSONArray j) { // Only works one-dimensionally
        int jLen = j.length();
        int[] finalArray = new int[jLen];
        for (int i = 0; i < jLen; i++) {
            finalArray[i] = j.getInt(i);
        }
        return finalArray;
    }


    private Map<String,int[][]> extractTiles(JSONObject tiles) {
        /*
         * Takes the object with a list of tileType:[[q,r],[q,r]] arrays
         * Returns a map of type "TypeName":[[q,r],[q,r]]
         */
        Map<String,int[][]> tilesMap = new HashMap<>();
        Iterator<String> keys = tiles.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray listOfType = tiles.getJSONArray(key); // listOfType is a nested array of form [[q,r],[q,r]]
            int[][] arrayOfType = new int[listOfType.length()][2]; // Will contain array of [q,r] pairs
            for (int i=0;i<listOfType.length();i++) {
                JSONArray tileArray = listOfType.getJSONArray(i);
                int[] qrPair = convertArray(tileArray);
                arrayOfType[i] = qrPair;
            }
            tilesMap.put(key,arrayOfType);
        }
        return tilesMap;
    }


    private void fillhexGrid(Map<String,int[][]> tileMap, String gridName) { 
        /*
         * Takes Map of <"Type":[[q,r],[q,r]]> and updates hexGrid with these
         * Whether it's updating terrain or placeable depends on the field string ("Terrain","Placeable")
         * TODO: Change this! Need to fill two grids, rather than one grid with two bits of info
         */

        for (Map.Entry<String,int[][]> tileType : tileMap.entrySet()) {
            String typeName =  tileType.getKey();
            int[][] tileList = tileType.getValue();
            for (int[] qr : tileList) {
                if (gridName.equals("terrainGrid")) {
                    BoardHex hex = terrainFromQR(qr);
                    hex.type = typeName;
                    hex.grow = terrainJSON.getJSONObject(typeName).getLong("growAbility");
                }
                else if (gridName.equals("unitGrid")) {
                    Unit hex = unitFromQR(qr);
                    hex.type = typeName;
                }
                if (tileCounters.containsKey(typeName)) {
                    int c = tileCounters.get(typeName);
                    tileCounters.put(typeName, c+1);
                }
                else { // If tileType isn't counted yet, initialise it with a value of 1
                    tileCounters.put(typeName,1);
                }
            }
        } 
    }

    private int sumValues(Map<String,Integer> tileCounter, String[] checkStrings) {
        int tileSum = 0;
        for (String s : checkStrings) {
            try {
                tileSum += tileCounter.get(s);
            }
            catch(NullPointerException n) { // There are 0 of a tile type

            }
        }
        return tileSum;
    }
    // Getters (and maybe setters)
    
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public List<BoardHex> getHexGrid() { // TODO: Update
        return terrainGrid;
    }

    public Unit getMainHex() {
        return mainHex;
    }

    public Hex getTargetHex() {
        return targetHex;
    }


    /*
     * Get Unit/BoardHex from q,r pair and list name string
     * TODO: Can I combine these?
     * TODO: Change away from stringName method
     */


    private Unit unitFromQR(int q, int r) { 
        int i = q * rows + r + q/2;
        return unitGrid.get(i);
    }

    // Overload
    public Unit unitFromQR(int[] qr) { 
        int q = qr[0], r = qr[1];
        Unit hex = unitFromQR(q, r);
        return hex;
    }

    private BoardHex terrainFromQR(int q, int r) { 
        int i = q * rows + r + q/2;
        return terrainGrid.get(i);
    }

    // Overload
    public BoardHex terrainFromQR(int[] qr) { 
        int q = qr[0], r = qr[1];
        BoardHex hex = terrainFromQR(q, r);
        return hex;
    }

    // Get index
    public int indexFromQR(int q, int r) {
        int i = q * rows + r + q/2;
        return i;
    }

    // Overload
    public int indexFromQR(int[] qr) {
        int i = indexFromQR(qr[0], qr[1]);
        return i;
    }

    /*
     * Update unit/terrain type - overloaded!
     * TODO: Validate type name to stop terrain/unit switch
     * TODO: Overload
     */
    private void updateHex(Unit hex, String newType) {
        hex.type = newType;
        hex.beenUpdated = true;
        lPanel.updateList.add(hex.getQR());
    }

    private void updateHex(BoardHex hex, String newType) {
        hex.type = newType;
        hex.beenUpdated = true;
        hex.grow = terrainJSON.getJSONObject(newType).getLong("growAbility");
        lPanel.updateList.add(hex.getQR());
    }
    

    // Central game tick

    public void gameTick(String mainAction) {
        
        // Move the main hex towards the target
        if (targetSet && mainAction == "Move") { // Doesn't pursue if the player is manually moving it
            mainHex.pursue(targetHex.q, targetHex.r);
        }
        
        // Make a wall trail
        Unit toWall = unitFromQR(mainHex.q, mainHex.r);
        if (toWall.type != "Bomb") {
            updateHex(toWall,"Wall");
        }

        List<int[]> newGrowth = new ArrayList<>(); // To hold all QRs that turn to plants this tick

        for (int i=0;i<terrainGrid.size();i++) { // First pass switch - bomb overlaid on top of this
            Unit h = unitGrid.get(i);

            // First loop
            switch (h.type) {
                case "New Plant":
                    // Check if it's surrounded - if it is, turn it into an Old Plant; if not, grow
                    int hq = h.q, hr = h.r;
                    int[][] neighbours = Funcs.neighbourRing(hq, hr, 1, rows, cols);
                    boolean canGrow = false;
                    for (int[] qr : neighbours) {
                        if (unitFromQR(qr).type.equals("Null")) { // If there's a space without another unit
                            if (terrainFromQR(qr).grow > 0) { // ...And there's growable terrain
                                canGrow = true;
                            }       
                        }
                    }

                    if (canGrow) { // If there's at least one non-unit neighbour
                        int [] newPlantQR = GrowHex.grow(hq, hr); // The neighbour qr it'll try to grow into
                        if (Funcs.isHexInGrid(newPlantQR[0],newPlantQR[1], rows, cols)) { // Make sure it's not accessing a QR not in hexGrid!
                            String hs = unitFromQR(newPlantQR).type; // Type of unit
                            if (hs.equals("Null")) { // Can only grow if there's no unit there!
                                BoardHex terrain = terrainFromQR(newPlantQR);
                                long hGrow = terrain.grow;
                                if (hGrow*10 >= 1 + rand.nextInt(10) ) {
                                    newGrowth.add(newPlantQR);
                                }
                            }
                        }
                    }

                    else { // If there are no non-unit neighbours
                        updateHex(h, "Old Plant");
                    }
                    
                break;

                default:
                break;
            }
        }

        // Grow new plants
        for (int[] qr : newGrowth) {
            updateHex(unitFromQR(qr), "New Plant");
        }
        newGrowth.clear();

        // Second pass (for bombs)

        for (int i=0;i<terrainGrid.size();i++) {
            Unit h = unitGrid.get(i); 

            switch(h.type) { 
                case "Bomb":
                System.out.println("Bomb!");
                    int bq = h.q, br = h.r;
                    int[][] innerRing = Funcs.neighbourRing(bq, br, 1, rows, cols);
                    int[][] outerRing = Funcs.neighbourRing(bq, br, 2, rows, cols);

                    updateHex(h,"Null");

                    for (int inner=0;inner<innerRing.length;inner++) {
                        updateHex(unitFromQR(innerRing[inner]),"Null");
                    }

                    for (int outer=0;outer<outerRing.length;outer++) {
                        if (unitFromQR(outerRing[outer]).type == "Old Plant") {
                            updateHex(unitFromQR(outerRing[outer]),"New Plant");
                        }
                        else {
                            updateHex(unitFromQR(outerRing[outer]),"Null");
                        }
                    }

                break;

                default:
                break;
            }
        }

        // Count totals
        plantCount = 0;
        for (int i=0;i<terrainGrid.size();i++) {
            if (unitGrid.get(i).type.equals("New Plant") || unitGrid.get(i).type.equals("Old Plant")) {
                plantCount += 1;
            }
        }
    }

    public void dragOver(int x, int y) { // TODO - Any hex!
        if (validXY(x, y) && godMode) {
            // dropTile is name of tile to be used
            int[] qr = Hex.pix2Hex(x - lPanel.offsetX, y - lPanel.offsetY);
            if (unitList.contains(dropTile)) { // If it's a unit
                Unit hex = unitFromQR(qr);
                updateHex(hex, dropTile);
            }
            else { // Assumption: if not in unitList, it's a terrain
                BoardHex hex = terrainFromQR(qr);
                updateHex(hex, dropTile);
            }
        }  
    }


    public void shouldScroll(int x, int y) {
    }


    public void bomb() {
        if (!paused) {
            updateHex(unitFromQR(mainHex.getQR()),"Bomb");  
            gameLoop("Bomb"); // Going via gameLoop means a redundant pause check (TODO: Do I need to call this?)
        } 
    }

    public void levelWipe() {
        for (Unit hex : unitGrid) {
            updateHex(hex, "Null");
        }
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
        int [] mainHex = Hex.pix2Hex(x-lPanel.offsetX, y-lPanel.offsetY);
        moveMainToQR(mainHex[0], mainHex[1]);
    }

    public void moveMainToQR(int q, int r) {
        if (!paused && Funcs.isHexInGrid(q, r, rows, cols)) {
            mainHex.moveToHex(q,r); 
            gameLoop("Move"); // Going via gameLoop means a redundant pause check
        }
        
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

    // Target setting function
    public void mouseClicked(int x, int y) {
        if (!godMode) {
            targetSet = true;
            moveTargetXY(x, y);
        }
        else {
            dragOver(x, y);
        }
    }

    // Check if x,y is on a hex
    private boolean validXY(int x, int y) {
        int [] targetQR = Hex.pix2Hex(x-lPanel.offsetX, y-lPanel.offsetY);
        return Funcs.isHexInGrid(targetQR[0], targetQR[1], rows, cols);
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
        int [] targetQR = Hex.pix2Hex(x-lPanel.offsetX, y-lPanel.offsetY);
        moveTargetToQR(targetQR[0], targetQR[1]);
    }

    public void moveTargetToQR(int q, int r) {
        if (Funcs.isHexInGrid(q, r, rows, cols)) {
            targetHex.moveToHex(q,r);
        }  
    }

    // Menu: cycle through terrain types

    public void cycleTerrain() {
        editTerrain += 1;
        editTerrain = editTerrain%(terrainList.size() + unitList.size()); // Should cycle through all the different tile types!
        if (editTerrain>=terrainList.size()) {
            dropTile = unitList.get(editTerrain - terrainList.size());
        }
        else {
            dropTile = terrainList.get(editTerrain);
        }
    }

    // What to do on button press
    public void onAction(String action) {
        switch (action) {
            case "Next Level Button":
                System.out.println("Next level!");
                paused = false;
                menuType = 0;
                gc.loadLevel("Level002");
            break;

            case "God Mode":
                System.out.println("Editing!");
                levelWipe();
                godMode = true;
                paused = false;
                menuType = 0;
            break;

            case "Terrain Picker":
                cycleTerrain();
                System.out.println("Tile type chosen: " + dropTile);
            default:
            break;
        }
    }
}
