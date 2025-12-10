package draughts;

import static draughts.Board10x10.GRID;
import static draughts.Board10x10.BOARD10X10;
import static draughts.Board10x10.paintTile;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;

/**
 * PieceBoard
 * 
 * Board with pieces (begin) and move.
 *
 * board (pieces)
 * move (yellow, green)
 * 
 * @author vanFoeken
 */

final class PieceBoard extends JComponent {
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack
    
    final static char EMPTY = '_';

    final static String WB = W + "" + B;//"wb"

    final static Image[][] PIECE = new Image[2][WB.length()];//[wb][WB]

    final private static char[] BOARD = new char[BOARD10X10.tile.length];//begin position
    final private static Color[] MOVE = {Color.yellow, Color.green};//{capture, piece}

    private char[] board = BOARD.clone();
    private ArrayList<Integer> move = new ArrayList();
    
    char getIndex(int index) {
        return board[index];
    }
    
    void setIndex(int index, char piece) {
        board[index] = piece;
    }
    
    char[] getBoard() {
        return board;
    }

    void setBoard(char[] board) {
        this.board = board;
    }
    
    ArrayList<Integer> getMove() {
        return move;
    }
            
    void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        for (int index : move) {
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//[0..0, 1]
            paintTile(g, BOARD10X10.tile[index]);            
        }

        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(board[i]) + "" + Character.toUpperCase(board[i])).indexOf(board[i])][WB.indexOf(Character.toLowerCase(board[i]))], BOARD10X10.tile[i].x, BOARD10X10.tile[i].y, this);
            }
        }
    }
    
    static {//begin position
        Arrays.fill(BOARD, 0, BOARD.length / 2 - GRID / 2, B);//0-20
        Arrays.fill(BOARD, BOARD.length / 2 - GRID / 2, BOARD.length / 2 + GRID / 2, EMPTY);//20-30
        Arrays.fill(BOARD, BOARD.length / 2 + GRID / 2, BOARD.length, W);//30-50
    }
}
