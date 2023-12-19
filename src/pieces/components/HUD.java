package pieces.components;

import java.awt.Color;
import java.awt.Graphics;

import main.GamePanel;

public class HUD extends Menu{

    private GamePanel gp;

    public HUD(int x, int y, int w, int h, GamePanel gp) {
        super(x, y, w, h);

        this.gp = gp;

        setLayout(null);

        // Edit button
        GButton editButton = new GButton("God Mode", "Edit",Color.BLUE, 2, gp);
        buttonList.add(editButton);
        add(editButton);

        GButton terrainPicker = new GButton("Terrain Picker", "Pick", Color.GREEN, 2, gp);
        buttonList.add(terrainPicker);
        add(terrainPicker);
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(gp.terrainImg.get(gp.tileList.get(gp.editTerrain)), 20, 5, null);
        g.drawString(gp.tileList.get(gp.editTerrain), 45, 19);
    }
}
