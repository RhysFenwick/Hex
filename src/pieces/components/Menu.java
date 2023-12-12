package pieces.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;

import main.GamePanel;

public class Menu extends JPanel {
    
    private static ArrayList<GButton> buttonList = new ArrayList<>();
    private static int menuMargin = 10; // Margin aaround edge of menu for buttons

    public Menu(int x, int y, int w, int h) {
        setLayout(new BorderLayout());
        setBounds(x, y, w, h);
        setMinimumSize(new Dimension(w, h));
        setBackground(Color.WHITE);
    }

    public void addButton(GButton btn) {
        add(btn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawRect(1,1, getWidth()-3, getHeight()-3);

        // Get the menu size dynamically (updates as the window is resized)
        Rectangle rec = this.getBounds();
        int width = (int) rec.getWidth();
        int height = (int) rec.getHeight();

        // Button distances - TODO: Draw on btn.height, btn.width for bHeight, bWidth
        int buttonGap = 10; // Gap between buttons
        int bHeight = 40;
        int bWidth = 40;
        int btnLeftX = menuMargin;
        int btnRightX = width - bWidth - menuMargin;

        // Place buttons based on alignment value
        for (GButton btn : buttonList) {
            int bX = 0, bY = 0;
            int bAl = btn.align;

            if (bAl > 0) { // Set height as top or bottom (0 = bottom)
                bY = menuMargin;
            }
            else {
                bY = height - (bHeight + menuMargin);
            }

            if (bAl*bAl == 1) { // If abs(bAl) = 1 (left-aligned)
                bX = btnLeftX;
                btnLeftX += bWidth + buttonGap;
            }
            else if (bAl*bAl == 4) { // If abs(bAl) = 2 (right-aligned)
                bX = btnRightX;
                btnRightX -= bWidth + buttonGap;
            }
            else { // Centres button. Can only be one button at a time!
                bX = (width - bWidth)/2;
            }

            btn.setLocation(bX,bY); 
            btn.setSize(bWidth, bHeight);
        }
    }

    // Specific menus

    public static Menu mainMenu(GamePanel gamePanel) {
        int x=50,y=20,w=100,h=100;
        Menu mainMenu = new Menu(x, y, w, h);


        GButton nextLevel = new GButton("Next Level Button", "New<br>Level", Color.BLACK, -1, gamePanel);
        buttonList.add(nextLevel);
        mainMenu.add(nextLevel);

        
        GButton secondButton = new GButton("God Mode", "Edit",Color.BLUE, -2, gamePanel);
        buttonList.add(secondButton);
        mainMenu.add(secondButton);
        

        return mainMenu;
    }
}
