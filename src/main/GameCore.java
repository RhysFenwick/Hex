package main;

/*
 * Currently mostly just a holder for FPS/TPS counters
 * TODO: Move core game functions here! 
 * Final model goal: World logic happens here, GamePanel just for display
 */

public class GameCore{

    private GamePanel gamePanel;

    public static int FPS = 65; // Would like to make this not static! Used for GamePanel scrolling.
    public static int TPS = 10; // Game ticks per second

    public GameCore() {
        System.setProperty("sun.java2d.opengl", "true"); // Should speed up rendering!
        gamePanel = new GamePanel();
        gamePanel.setDoubleBuffered(true); // Maybe speed up?
        GameWindow gameWindow = new GameWindow(gamePanel);
        gameWindow.getAlignmentX(); // Exists only to avoid the yellow squiggle
        gamePanel.requestFocus(); // Gets focus to pick up keypresses
        run();
    }

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
                gamePanel.gameLoop("Move");
                lastTick = now;
            }

            // FPS
            if (now - lastFrame > timePerFrame) {
                gamePanel.repaint();
                lastFrame = now; // Calls the Sys anew?
            }

            // FPS Counter
            if (System.currentTimeMillis() - lastCheck > 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + gamePanel.frameCount);
                gamePanel.frameCount = 0;
            }
        }
    }
}
