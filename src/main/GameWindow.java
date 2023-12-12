package main;

import javax.swing.JFrame;

public class GameWindow extends JFrame{
    
    public GameWindow(GamePanel gamePanel) {

        int WIDTH = 500;
        int HEIGHT = 500;

        JFrame f = new JFrame();
        // f.setIgnoreRepaint(true);
        f.setSize(WIDTH, HEIGHT);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(gamePanel);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

    }
}
