package pieces.components;

import java.awt.Color;
import java.awt.Graphics;

import main.levels.Level;

public class HUD extends Menu{

    private Level lvl;

    public HUD(int x, int y, int w, int h, Level level) {
        super(x, y, w, h);

        lvl = level;

        setLayout(null);

        // Edit button
        GButton editButton = new GButton("God Mode", "Edit",Color.BLUE, 2, lvl);
        buttonList.add(editButton);
        add(editButton);

        GButton terrainPicker = new GButton("Terrain Picker", "Pick", Color.GREEN, 2, lvl);
        buttonList.add(terrainPicker);
        add(terrainPicker);
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(lvl.gc.hexImg.get(lvl.dropTile), 20, 10, null);
    }
}
