package main;

import javax.swing.JFrame;

import main.levels.Level;

public class GameWindow extends JFrame{

    int WIDTH = 500;
    int HEIGHT = 500;
    JFrame jf = new JFrame();
    
    public GameWindow(Level lvl) {

        // jf.setIgnoreRepaint(true);
        jf.setSize(WIDTH, HEIGHT);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);
        jf.add(lvl.lPanel);
        jf.setVisible(true);
    }
}
