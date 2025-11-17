package draughts;

import static draughts.Board10x10.paintTile;
import static draughts.Draughts.BOARD10X10;
import static draughts.Draughts.HINT;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Set;

/**
 * HintBoard
 *
 * Color moveable or selected (orange)
 * MouseAdapter is added in Game class for player move
 * 
 * selected -> one of moveable
 * moveable -> pieces to select 
 * 
 * @author vanFoeken
*/

final class HintBoard extends Component {
    final static int NOT_SELECTED = -1;//no piece selected
    
    final private static Color ORANGE = Color.orange;

    private int selected;
    private Set<Integer> moveable;
    
    HintBoard() {
        setVisible(false);
        setForeground(ORANGE);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    void setHintBoard(Set<Integer> moveable) {
        selected = NOT_SELECTED;
        this.moveable = moveable;
        
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintTile(g, BOARD10X10.tile[selected]);
        } else if (HINT.isSelected()) {
            moveable.forEach(moveable -> paintTile(g, BOARD10X10.tile[moveable]));
        }
    }
    
}
