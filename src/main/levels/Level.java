package main.levels;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import pieces.hexes.FocusHex;
import pieces.hexes.Hex;

import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Level {
    
    // Grid values
    public List<Hex> hexGrid = new ArrayList<>(); // Should be the list of all hexes!
    public int cols, rows;
    public FocusHex mainHex;
    public Hex targetHex;
    public Map<String,Integer> tileCounters = new HashMap<>();
    public int plantCount;
    private String[] plantNames = {"New Plant", "Old Plant"};


    public Level(String levelJSONName) throws FileNotFoundException {

        // Extract level from file, turn it into JSON
        FileReader levelFile = new FileReader(levelJSONName);
        JSONTokener levelToken = new JSONTokener(levelFile);
        JSONObject lvl = new JSONObject(levelToken);

        // Read important variables from JSON
        cols = lvl.getInt("cols");
        rows = lvl.getInt("rows");
        int[] mainHexQR = convertArray(lvl.getJSONArray("mainHexQR"));

        JSONObject tilesJSON = lvl.getJSONObject("mapFeatures");
        Map<String, int[][]>  mapTiles = extractTiles(tilesJSON);

        // Set up the map
        initialiseGrid(cols,rows);
        fillMap(mapTiles); // This one will also fill tileCounters
        plantCount = sumValues(tileCounters, plantNames);
        mainHex = new FocusHex(mainHexQR[0], mainHexQR[1],-mainHexQR[0]-mainHexQR[1]); 
        targetHex = new Hex(mainHexQR[0], mainHexQR[1],-mainHexQR[0]-mainHexQR[1]); // Initialise as on target
        hexFromQR(mainHex.q, mainHex.r).toType("Stone"); // Only for stone trail!        

    }


    private void initialiseGrid(int cols, int rows) {
        for (int col=0;col<cols;col++) {   // Fill hexGridQR with QR coords - might not render all of them!
            for (int row=0;row<rows;row++) {
                // hexGridQR[col*rows + row] = new int[] {col,row-col/2};
                hexGrid.add(new Hex(col,row - col/2,-col-(row-col/2)));
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


    private Map<String,int[][]> extractTiles(JSONObject tiles) { // Takes the object with a list of tileType:[[q,r],[q,r]] arrays
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


    private void fillMap(Map<String,int[][]> tileMap) { // Takes Map of <"Type":[[q,r],[q,r]] and updates hexGrid with these
        for (Map.Entry<String,int[][]> tileType : tileMap.entrySet()) {
            String typeName =  tileType.getKey();
            int[][] tileList = tileType.getValue();
            for (int[] qr : tileList) {
                Hex hex = hexFromQR(qr[0], qr[1]);
                hex.toType(typeName);
                if (tileCounters.containsKey(typeName)) {
                    int c = tileCounters.get(typeName);
                    tileCounters.put(typeName, c+1);
                }
                else {
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

    public List<Hex> getHexGrid() {
        return hexGrid;
    }

    public FocusHex getMainHex() {
        return mainHex;
    }

    public Hex getTargetHex() {
        return targetHex;
    }

    // Other methods (misc)
    private Hex hexFromQR(int q, int r) {
        int i = q * rows + r + q/2;
        return hexGrid.get(i);
    }
}
