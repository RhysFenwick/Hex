package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import main.GamePanel;

public class KeyboardInputs implements KeyListener {

    private GamePanel gamePanel;

    private int scrollSpeed = 17;


    public KeyboardInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        switch (e.getKeyCode()) {

            case KeyEvent.VK_A:
                gamePanel.moveMainQR(-1, 1);
            break;  

            case KeyEvent.VK_D:
                gamePanel.moveMainQR(1,0);
            break; 

            case KeyEvent.VK_Q:
                gamePanel.moveMainQR(-1,0);
            break;

            case KeyEvent.VK_E:
                gamePanel.moveMainQR(1,-1);
            break;

            case KeyEvent.VK_W:
                gamePanel.moveMainQR(0,-1);
            break;

            case KeyEvent.VK_S:
                gamePanel.moveMainQR(0,1);
            break;

            case KeyEvent.VK_B:
                //gamePanel.clearDrag();
                gamePanel.bomb();
            break;

            case KeyEvent.VK_M:
                gamePanel.toggleMenu();
            break;

            case KeyEvent.VK_SPACE:
                gamePanel.gameLoop("Move");
            break;

            case KeyEvent.VK_UP:
                gamePanel.incOY(scrollSpeed);
            break;

            case KeyEvent.VK_DOWN:
                gamePanel.incOY(-scrollSpeed);
            break;

            case KeyEvent.VK_LEFT:
                gamePanel.incOX(scrollSpeed);
            break;

            case KeyEvent.VK_RIGHT:
                gamePanel.incOX(-scrollSpeed);
            break;

            case KeyEvent.VK_P:
                gamePanel.togglePause();
            break;

            case KeyEvent.VK_N:
                gamePanel.loadLevel("Level002");

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
