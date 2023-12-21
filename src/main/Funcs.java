package main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;


public class Funcs { // All the maths that I don't want clogging up the other files


    public static int[][] neighbourRing(int q, int r, int dist, int hexRows, int hexCols) { // Relies on the fact that distance = half the sum of QRS absolutes
        int[][] vecsFromTop = {{1,0},{0,1},{-1,1},{-1,0},{0,-1},{1,-1}}; // QR vectors in a circle from the top
        r -= dist; // R Coord for top of ring (q remains the same)
        List<int[]> dMatches = new ArrayList<>();
        for (int i=0;i<6;i++) {
            for (int j=0;j<dist;j++) { // Distance = radius = side length - makes sure it repeats for each side!
                int[] newNeighbour = {q,r};
                dMatches.add(newNeighbour);
                // Move QR to the next one around the ring
                q += vecsFromTop[i][0];
                r += vecsFromTop[i][1];
            }
        }
        int[][] ringList = GamePanel.list2nestInt(dMatches);
        int[][] validRing = filterForValid(ringList, hexRows, hexCols);
        return validRing;
    }


    public static int[][] filterForValid(int[][] t, int hexRows, int hexCols) {
        List<int[]> validNeighboursList = new ArrayList<>();
        for (int i=0;i<t.length;i++) {
            if (isHexInGrid(t[i][0],t[i][1], hexRows, hexCols)) {
                validNeighboursList.add(new int[] {t[i][0],t[i][1]}); 
            }
        }
        int validNum = validNeighboursList.size();
        int[][] validNeighboursArray = new int[validNum][2];
        for (int i=0;i<validNum;i++) {
            validNeighboursArray[i] = validNeighboursList.get(i);
        }
        return validNeighboursArray;
    }

    
    public static boolean isHexInGrid(int q, int r, int hexRows, int hexCols) {
        if (0 <=  q/2 + r && r+q/2 < hexRows && 0 <= q && q < hexCols) {
            return true;
        }
        else {
            return false;
        }
    }


    // TODO: Make this add .JSON if not already there
    public static JSONObject readJSON(String fileName){
        JSONObject json = null;
        try {
            FileReader file = new FileReader(fileName);
            JSONTokener token = new JSONTokener(file);
            json = new JSONObject(token);
        }
        catch (FileNotFoundException e) {
            System.out.println(fileName + " not found");
        }
        return json;
    }
}
