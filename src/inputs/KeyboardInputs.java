package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import main.LevelPanel;
import main.levels.Level;

public class KeyboardInputs implements KeyListener {

    private LevelPanel lvl;

    private int scrollSpeed = 17;


    public KeyboardInputs(LevelPanel levelPanel) {
        this.lvl = levelPanel;
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        switch (e.getKeyCode()) {

            case KeyEvent.VK_A:
                lvl.lvl.moveMainQR(-1, 1);
            break;  

            case KeyEvent.VK_D:
                lvl.lvl.moveMainQR(1,0);
            break; 

            case KeyEvent.VK_Q:
                lvl.lvl.moveMainQR(-1,0);
            break;

            case KeyEvent.VK_E:
                lvl.lvl.moveMainQR(1,-1);
            break;

            case KeyEvent.VK_W:
                lvl.lvl.moveMainQR(0,-1);
            break;

            case KeyEvent.VK_S:
                lvl.lvl.moveMainQR(0,1);
            break;

            case KeyEvent.VK_B:
                //gamePanel.clearDrag();
                lvl.lvl.bomb();
            break;

            case KeyEvent.VK_M:
                lvl.lvl.toggleMenu();
            break;

            case KeyEvent.VK_SPACE:
                lvl.lvl.gameLoop("Move");
            break;

            case KeyEvent.VK_UP:
                lvl.incOY(scrollSpeed);
            break;

            case KeyEvent.VK_DOWN:
                lvl.incOY(-scrollSpeed);
            break;

            case KeyEvent.VK_LEFT:
                lvl.incOX(scrollSpeed);
            break;

            case KeyEvent.VK_RIGHT:
                lvl.incOX(-scrollSpeed);
            break;

            case KeyEvent.VK_P:
                lvl.lvl.togglePause();
            break;

            case KeyEvent.VK_N:
                lvl.lvl.gc.loadLevel("Level002");

            default:
            break;


        } 
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
            // TODO
        
    }

}
