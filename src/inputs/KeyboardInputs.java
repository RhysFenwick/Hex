package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import main.LevelPanel;
import main.levels.Level;

public class KeyboardInputs implements KeyListener {

    private LevelPanel lp;
    private Level lvl;

    private int scrollSpeed = 17;


    public KeyboardInputs(LevelPanel levelPanel) {
        this.lp = levelPanel;
        this.lvl = lp.lvl;
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        switch (e.getKeyCode()) {

            case KeyEvent.VK_A:
                lvl.moveMainQR(-1, 1);
            break;  

            case KeyEvent.VK_D:
                lvl.moveMainQR(1,0);
            break; 

            case KeyEvent.VK_Q:
                lvl.moveMainQR(-1,0);
            break;

            case KeyEvent.VK_E:
                lvl.moveMainQR(1,-1);
            break;

            case KeyEvent.VK_W:
                lvl.moveMainQR(0,-1);
            break;

            case KeyEvent.VK_S:
                lvl.moveMainQR(0,1);
            break;

            case KeyEvent.VK_B:
                //gamePanel.clearDrag();
                lvl.bomb();
            break;

            case KeyEvent.VK_M:
                lvl.toggleMenu();
            break;

            case KeyEvent.VK_SPACE:
                lvl.gameLoop("Move");
            break;

            case KeyEvent.VK_UP:
                lp.incOY(scrollSpeed);
            break;

            case KeyEvent.VK_DOWN:
                lp.incOY(-scrollSpeed);
            break;

            case KeyEvent.VK_LEFT:
                lp.incOX(scrollSpeed);
            break;

            case KeyEvent.VK_RIGHT:
                lp.incOX(-scrollSpeed);
            break;

            case KeyEvent.VK_P:
                lvl.togglePause();
            break;

            case KeyEvent.VK_N:
                lvl.gc.loadLevel("Level002");

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
