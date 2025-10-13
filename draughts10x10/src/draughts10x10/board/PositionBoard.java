package draughts10x10.board;

import static draughts10x10.board.SquareBoard.GRID;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * PositionBoard
 * 
 * position (begin)
 * move (yellow, green)
 * 
 * @author Naardeze
 */

final public class PositionBoard extends Board {
    //pawn
    final private static char W = 'w';//hite
    final private static char B = 'b';//lack

    //"wb"
    final public static String WB = W + "" + B;

    //empty square
    final public static char EMPTY = '_';

    //move colors
    final private static Color[] MOVE = {Color.yellow, Color.green};//<<captures>, piece>

    private char[] position;
    private ArrayList<Integer> move = new ArrayList();
    
    public PositionBoard(Rectangle[] square) {
        super(square);
        
        //begin position
        position = new char[square.length];
  
        Arrays.fill(position, 0, position.length / 2 - GRID / 2, B);
        Arrays.fill(position, position.length / 2 - GRID / 2, position.length / 2 + GRID / 2, EMPTY);
        Arrays.fill(position, position.length / 2 + GRID / 2, position.length, W);
    }

    //<-piece
    public char getIndex(int index) {
        return position[index];
    }
    
    //piece of board
    public void setIndex(int index, char piece) {
        position[index] = piece;
    }
    
    //<-pieces
    public String getPosition() {
        return String.valueOf(position);
    }
    
    //pieces on board
    public void setPosition(String position) {
        move.clear();
        
        this.position = position.toCharArray();
    }
    
    //<-colored squares
    public ArrayList<Integer> getMove() {
        return move;
    }
            
    //color squares
    public void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    //paint move and pieces
    @Override
    public void paintComponent(Graphics g) {
        //move
        for (int index : move) {
            g.setColor(MOVE[(move.indexOf(index) + 1) / move.size()]);//[0..0, 1]
            
            paintSquare(g, square[index]);
        }

        //pieces
        for (int i = 0; i < position.length; i++) {
            if (position[i] != EMPTY) {
                g.drawImage(PIECE[(Character.toLowerCase(position[i]) + "" + Character.toUpperCase(position[i])).indexOf(position[i])][WB.indexOf(Character.toLowerCase(position[i]))], square[i].x, square[i].y, this);
            }
        }
    }
    
}
