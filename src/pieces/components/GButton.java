package pieces.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import main.GamePanel;

public class GButton extends JButton{

    public int topX, topY;
    public int width, height;
    public Color color;
    public String label;
    public int align;
    
    public GButton(String name, String txt, Color c, int alignment, GamePanel gamePanel) {

        /*
         * Name: Unique identifier for the button across the entire program. Possibly need some namespace rules?
         * Txt: What shows up on the button - uses HTML (<br> for line breaks etc)
         * Alignment: Where the button appears. 1 for left, 2 for right; + for top, - for bottom; 0 is centre bottom.
         */
        align = alignment;
        color = c;
        label = txt;
        
        setName(name);
        setFocusable(false);
        setBackground(c);
        setBorderPainted(false);
        addActionListener(new CustomActionListener());
        addActionListener(gamePanel);
        setMargin(new Insets(0, 0, 0, 0));
        setFont(new Font("Arial", Font.PLAIN, 16));
        setText("<html><div text-align:center>" + txt + "</div></html>");
    }

    // Setters for x & y
    public void moveToArr(int[] xy) {
        topX = xy[0];
        topY = xy[1];
    }

    public void moveToInt(int x, int y) {
        topX = x;
        topY = y;
    }

    class CustomActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        //System.out.println("Pressed!");
        }
    }
}
