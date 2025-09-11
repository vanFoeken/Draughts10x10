package draughts10x10;

import static draughts10x10.Draughts10x10.HINT;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

/* HintBoard
 *
 * select (pressed)
 * hint (moveable)
 *
 * manual move (player)
*/

final class HintBoard extends AbstractBoard {
    //not selected
    final static int NOT_SELECTED = -1;
    
    //square color
    final private static Color ORANGE = Color.orange;
    
    private int selected;
    private Set<Integer> hint;
    
    HintBoard() {
        super(SQUAREBOARD.square);

        setForeground(ORANGE);
        setVisible(false);
    }
    
    int getSelected() {
        return selected;
    }
    
    void setSelected(int selected) {
        this.selected = selected;
    }
    
    void setHint(Set<Integer> hint) {
        selected = NOT_SELECTED;

        this.hint = hint;
        
        setVisible(true);
    }
    
    @Override
    public void paint(Graphics g) {
        if (selected != NOT_SELECTED) {
            paintSquare(g, square[selected]);
        } else if (HINT.isSelected()) {//not the best but the simplest way
            hint.forEach(hint -> paintSquare(g, square[hint]));
        }
    }
    
}
