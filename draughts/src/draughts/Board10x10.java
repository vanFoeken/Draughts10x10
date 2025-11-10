package draughts;

import static draughts.PieceBoard.PIECE;
import static draughts.PieceBoard.WB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

/**
 * Board10x10
 
 Board with dark tiles [50]
 
 -getColumn (static)
 -getRow (static)
 -paintTile (g.fillRect(tile)) (static)
 -actionPerformed (rotate)
 * 
 * @author vanFoeken
*/

final class Board10x10 extends JPanel implements ActionListener {    
    final static int GRID = 10;//10x10

    final private static Color LIGHT = new Color(255, 250, 180);//board
    final private static Color DARK = new Color(188, 136, 92);//tile
    
    final Rectangle[] tile = new Rectangle[GRID * GRID / 2];//[50]
    
    Board10x10() {
        super(new BorderLayout());//add game
        
        setBackground(LIGHT);
        setForeground(DARK);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //tile size
                int column = getWidth() / GRID;
                int row = getHeight() / GRID;

                for (int i = 0; i < tile.length; i++) {//sized once -> tile[0] top left (1,0)
                    tile[i] = new Rectangle(getColumn(i) * column, getRow(i) * row, column, row);
                }

                for (char piece : WB.toCharArray()) {//tile size
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + ".png").getScaledInstance(column, row, Image.SCALE_SMOOTH);//pawn
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "" + piece + ".png").getScaledInstance(column, row, Image.SCALE_SMOOTH);//king
                }
            }
        });
    }

    static int getColumn(int index) {//column
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    static int getRow(int index) {//row
       return index / (GRID / 2);
    }

    static void paintTile(Graphics g, Rectangle tile) {
        g.fillRect(tile.x, tile.y, tile.width, tile.height);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Rectangle tile : tile) {
            tile.setLocation(getWidth() - tile.x - tile.width, getHeight() - tile.y - tile.height);
        }
        
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
           
        for (Rectangle tile : tile) {
            paintTile(g, tile);
        }
    }

}
