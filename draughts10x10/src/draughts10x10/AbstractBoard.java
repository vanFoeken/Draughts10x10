package draughts10x10;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/* AbstractBoard
 *
 * abstract (parent) board class
 * 
 * squares
 * 
 * -paintSquare (static)
 */

abstract class AbstractBoard extends JComponent {
    final protected Rectangle[] square;
    
    protected AbstractBoard(Rectangle[] square) {
        this.square = square;
    }
    
    //paint square
    final protected static void paintSquare(Graphics g, Rectangle square) {
        g.fillRect(square.x, square.y, square.width, square.height);
    }
    
}
