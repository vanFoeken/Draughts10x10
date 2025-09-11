package draughts10x10;

import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.SquareBoard.SIZE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

/* PieceBoard
 *
 * board (begin position)
 * move
 */

final class PieceBoard extends AbstractBoard {
    //pieces
    final static char W = 'w';//w(hite) pawn
    final static char B = 'b';//b(lack) pawn
    final static char W_KING = Character.toUpperCase(W);//W king
    final static char B_KING = Character.toUpperCase(B);//B king

    //empty square
    final static char EMPTY = '_';

    //"wb"
    final static String WB = W + "" + B;

    //piece images [wb][WB]
    final static Image[][] IMAGE = new Image[2][WB.length()];
    
    //move colors
    final private static Color[] COLOR = {Color.yellow, Color.green};

    private char[] board;
    private ArrayList<Integer> move = new ArrayList();
    
    PieceBoard() {
        super(SQUAREBOARD.square);
        
        board = new char[square.length];

        //begin position
        //1-20: 'b'
        for (int i = 0; i < board.length / 2 - SIZE / 2; i++) {
            board[i] = B;
        }

        //21-30: ' '
        for (int i = board.length / 2 - SIZE / 2; i < board.length / 2 + SIZE / 2; i++) {
            board[i] = EMPTY;
        }

        //31-50: 'w'
        for (int i = board.length / 2 + SIZE / 2; i < board.length; i++) {
            board[i] = W;
        }
    }
    
    char getIndex(int index) {
        return board[index];
    }
    
    void setIndex(int index, char piece) {
        board[index] = piece;
    }
    
    String getBoard() {
        return String.valueOf(board);
    }
    
    void setBoard(String board) {
        this.board = board.toCharArray();
    }
    
    ArrayList<Integer> getMove() {
        return move;
    }
            
    void setMove(ArrayList<Integer> move) {
        this.move = move;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        //move
        for (int index : move) {
            g.setColor(COLOR[(move.indexOf(index) + 1) / move.size()]);//<0..0, 1>
            
            paintSquare(g, square[index]);
        }

        //board
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                g.drawImage(IMAGE[(Character.toLowerCase(board[i]) + "" + Character.toUpperCase(board[i])).indexOf(board[i])][WB.indexOf(Character.toLowerCase(board[i]))], square[i].x, square[i].y, this);
            }
        }
    }
    
}
