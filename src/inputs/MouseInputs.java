package inputs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import main.LevelPanel;

public class MouseInputs implements MouseMotionListener, MouseWheelListener, MouseListener{

    private LevelPanel lvl;

    public MouseInputs(LevelPanel levelPanel) {
        lvl = levelPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        int x = e.getX(), y = e.getY();
        lvl.lvl.mouseClicked(x, y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        lvl.lvl.dragOver(x, y);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        lvl.shouldScroll(e.getX(),e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO Auto-generated method stub
        if (e.isShiftDown()) {
            System.out.println("Horizontal " + e.getWheelRotation());
        } else {
            System.out.println("Vertical " + e.getWheelRotation());                    
        }
    }
}
