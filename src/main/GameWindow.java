package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.levels.Level;

public class GameWindow extends JFrame{

    int WIDTH = 500;
    int HEIGHT = 500;
    LevelPanel lPanel;
    
    public GameWindow(Level lvl) {

        lPanel = lvl.lPanel;
        //this.setIgnoreRepaint(true);
        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.add(lPanel);
        this.setVisible(true);
    }

    public void updateScreen(LevelPanel newPanel, int newWidth, int newHeight) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameWindow.this.getContentPane().removeAll(); // Remove all components
                lPanel = newPanel;
                GameWindow.this.add(lPanel);
                GameWindow.this.invalidate();
                GameWindow.this.revalidate();
                GameWindow.this.repaint();
                System.out.println("Revalidated");
            }
        });
    }
}
