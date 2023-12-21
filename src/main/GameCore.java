package main;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import org.json.JSONObject;

import main.levels.Level;
import pieces.hexes.Hex;

public class GameCore {

    public static int FPS = 65;
    public static int TPS = 10;

    int hexHeight = Hex.height;
    int hexWidth = Hex.width;

    public GameWindow gameWindow;
    public Map<String, Image> hexImg = new HashMap<String, Image>();
    public Level lvl;
    public LevelPanel lPanel;
    public ArrayList<String> tileList = new ArrayList<>();
    private volatile boolean keepRunning = true;

    public GameCore() {
        fillImageMap(hexImg, "TerrainTypes");
        fillImageMap(hexImg, "PlaceableTypes");

        lvl = new Level("Level001", this);
        lPanel = lvl.lPanel;

        System.setProperty("sun.java2d.opengl", "true");
        gameWindow = new GameWindow(lvl);
        gameWindow.getAlignmentX();
        lPanel.requestFocus();

        runGameLoop(lPanel);
    }

    private void runGameLoop(LevelPanel lp) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                double timePerFrame = 1_000_000_000.0 / FPS;
                long lastFrame = System.nanoTime();
                long lastCheck = System.currentTimeMillis();
                double timePerTick = 1_000_000_000.0 / TPS;
                long lastTick = System.nanoTime();

                System.out.println("Starting run!");
                lp.repaint();

                while (keepRunning) {
                    long now = System.nanoTime();

                    if (now - lastTick > timePerTick) {
                        lp.lvl.gameLoop("Move");
                        lastTick = now;
                    }

                    if (now - lastFrame > timePerFrame) {
                        lp.repaint();
                        lastFrame = now;
                    }

                    if (System.currentTimeMillis() - lastCheck > 1000) {
                        lastCheck = System.currentTimeMillis();
                        System.out.println("FPS: " + lp.frameCount);
                        lp.frameCount = 0;
                    }
                }
                return null;
            }
        };

        worker.execute();
    }

    public void fillImageMap(Map<String, Image> map, String jsonName) {
        JSONObject tileJSON = Funcs.readJSON("src/main/resources/" + jsonName + ".JSON");
        Iterator<String> tileKeys = tileJSON.keys();
        
        while (tileKeys.hasNext()) {
            String tString = tileKeys.next();
            if (!tString.equals("Null")) {
                tileList.add(tString);
                map.put(tString, getImageFromFile(tString));
            }
        }
    }

    public Image getImageFromFile(String filename) {
        Image image = null;
        try {
            File imgFile = new File("src/main/resources/img/terrain/" + filename + ".png");
            image = ImageIO.read(imgFile);
        } catch (IOException e) {
            System.out.println(filename);
            System.out.println(e);
        }
        return image.getScaledInstance(hexWidth, hexHeight, Image.SCALE_DEFAULT);
    }

    public void loadLevel(String levelName) {
        int newWidth = lPanel.getWidth(), newHeight = lPanel.getHeight();
        keepRunning = false;
        Level newLevel = new Level(levelName, this);
        lPanel = newLevel.lPanel;
        gameWindow.updateScreen(lPanel, newWidth, newHeight);
        keepRunning = true;
        runGameLoop(lPanel);
    }
}

