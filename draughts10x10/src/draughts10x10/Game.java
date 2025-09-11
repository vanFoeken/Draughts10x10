package draughts10x10;

import static draughts10x10.Draughts10x10.AI;
import static draughts10x10.Draughts10x10.BLACK;
import static draughts10x10.Draughts10x10.GAME_OVER;
import static draughts10x10.Draughts10x10.SQUAREBOARD;
import static draughts10x10.Draughts10x10.UNDO;
import static draughts10x10.Draughts10x10.WHITE;
import static draughts10x10.HintBoard.NOT_SELECTED;
import static draughts10x10.PieceBoard.B;
import static draughts10x10.PieceBoard.B_KING;
import static draughts10x10.PieceBoard.EMPTY;
import static draughts10x10.PieceBoard.IMAGE;
import static draughts10x10.PieceBoard.W;
import static draughts10x10.PieceBoard.WB;
import static draughts10x10.PieceBoard.W_KING;
import static draughts10x10.SquareBoard.SIZE;
import static draughts10x10.SquareBoard.x;
import static draughts10x10.SquareBoard.y;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import javax.swing.JLayeredPane;

/* Game
 *
 * -turn(color) -> 1 pieces, moves, maxCapture
 *                 2 evaluation
 * -actionPerformed: undo move
 * 
 * class PieceMove -> move animation
 *
 * enum Direction -> step in 4 directions
*/

final class Game extends JLayeredPane implements ActionListener {
    //pawn, king
    final static char[] PAWN = {W, B};//{w, b}
    final static char[] KING = {W_KING, B_KING};//{W, B}

    //boards
    final private PieceBoard pieceBoard = new PieceBoard();
    final private HintBoard hintBoard = new HintBoard();

    //player positoins (undo move)
    final private Stack<String> positions = new Stack();
    
    //player color
    final private int player;
    
    //used in turn
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];//pieces WHITE and BLACK
    private HashMap<Integer, Move[]> moves;//legal moves
    private int maxCapture;//captures p move
    
    Game(int player) {
        this.player = player;
        
        UNDO.setEnabled(false);
        GAME_OVER.setVisible(false);

        //manual move
        hintBoard.addMouseListener(new MouseAdapter() {
            Rectangle[] square = SQUAREBOARD.getSquares();
            
            @Override
            public void mousePressed(MouseEvent e) {
                for (int index = 0; index < square.length; index++) {
                    if (square[index].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();                        

                        //multiple moves same destination (3+ captures)
                        if (selected != NOT_SELECTED && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(index) == EMPTY || index == selected)) {
                            ArrayList<Integer> captures = new ArrayList(pieceBoard.getMove());
                            int step = captures.remove(captures.size() - 1);
                            
                            //x==y
                            if (index != step && Math.abs(x(index) - x(step)) == Math.abs(y(index) - y(step))) {
                                Direction direction = Direction.getDirection(step, index);
                             
                                step = direction.getNext(step);
                                
                                if (pieceBoard.getIndex(selected) == KING[player]) {
                                    while (step != index && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getNext(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !captures.contains(step)) {
                                    captures.add(step);
                                    step = direction.getNext(step);
                                    
                                    if (pieceBoard.getIndex(selected) == KING[player]) {
                                        while (step != index && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getNext(step);
                                        }
                                    }
        
                                    if (step == index) {
                                        if (captures.size() == maxCapture) {//move
                                            new Thread(new PieceMove(player, new Move(captures, step).getBoardMove(selected))).start();
                                        } else {//extra step
                                            captures.add(step);
                                            pieceBoard.setMove(captures);

                                            repaint();
                                        }
                                    }
                                }
                            }
                        //index = occupied
                        } else if (pieceBoard.getIndex(index) != EMPTY) {
                            pieceBoard.getMove().clear();
                            
                            //moveable
                            if (moves.containsKey(index)) {
                                Move[] move = moves.get(index);
                                
                                if (move.length == 1) {//1 move
                                    new Thread(new PieceMove(player, move[0].getBoardMove(index))).start();
                                } else {//set selected (index)
                                    hintBoard.setSelected(index);
                        
                                    //multiple moves same destination (3+ captures)
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int j = 0; j < i; j++) {
                                            if (move[j].getTo() == move[i].getTo()) {
                                                pieceBoard.getMove().add(index);
                                                
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else if (selected != NOT_SELECTED) {
                                hintBoard.setSelected(NOT_SELECTED);
                            }
                        
                            repaint();
                        //selected, index = empty
                        } else if (moves.containsKey(selected)) {
                            for (Move move : moves.get(selected)) {
                                if (move.getTo() == index) {//move
                                    new Thread(new PieceMove(player, move.getBoardMove(selected))).start();
                                }
                            }
                        }                        
                        
                        break;
                    }
                }
            }
        });
        
        add(hintBoard);
        add(pieceBoard, new Integer(1));
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hintBoard.setSize(getSize());
                pieceBoard.setSize(getSize());
                
                //begin game when boards are sized
                //prevent: player = BLACK & AI = 1 (or 2) and boardmove starts before boards are sized (doesn't look nice)
                turn(WHITE);//WHITE begins game
            }
        });
    }
    
    //1 pieces, moves and maxCapture
    //2 evaluation
    private void turn(int color) {
        //1: pieces[WB], moves and maxCapture
        char[] board = pieceBoard.getBoard().toCharArray();

        pieces[WHITE] = new HashSet();
        pieces[BLACK] = new HashSet();
        
        for (int i = 0; i < board.length; i++) {
            if (board[i] != EMPTY) {
                pieces[WB.indexOf(Character.toLowerCase(board[i]))].add(i);
            }
        }
        
        moves = new HashMap();
        maxCapture = 0;
        
        for (int index : pieces[color]) {
            char piece = board[index];
            HashSet<Move> pieceMoves = new HashSet();

            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {
                for (Direction vertical : horizontal) {
                    if (vertical.hasNext(index)) {
                        int next = vertical.getNext(index);

                        //empty step(s)
                        if(board[next] == EMPTY) {
                            if (maxCapture == 0 && (piece == KING[color] || vertical == horizontal[color])) {
                                pieceMoves.add(new Move(next));
                            }

                            if (piece == KING[color] && vertical.hasNext(next)) {
                                do {
                                    next = vertical.getNext(next);

                                    if (maxCapture == 0 && board[next] == EMPTY) {
                                        pieceMoves.add(new Move(next));
                                    }
                                } while (board[next] == EMPTY && vertical.hasNext(next));
                            }
                        }

                        //capture
                        if (pieces[1 - color].contains(next) && vertical.hasNext(next)) {
                            int capture = next;
                            
                            next = vertical.getNext(capture);
                          
                            if (board[next] == EMPTY) {
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, next}));//<capture, next>

                                if (piece == KING[color] && vertical.hasNext(next)) {
                                    do {
                                        next = vertical.getNext(next);
                                        
                                        if (board[next] == EMPTY) {
                                            captureMove.add(next);
                                        }
                                    } while (board[next] == EMPTY && vertical.hasNext(next));
                                }

                                //captures to check for extra captures
                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));//<captureMove>

                                board[index] = EMPTY;

                                //check captures
                                do {
                                    ArrayList<Integer> move = captureMoves.remove(0);//<capturesMove>, <empty>>
                                    ArrayList<Integer> captures = new ArrayList();

                                    //captures <-> empty
                                    do {
                                        captures.add(move.remove(0));
                                    } while (pieces[1 - color].contains(move.get(0)));

                                    //maxCapture +1
                                    if (captures.size() > maxCapture) {
                                        pieceMoves.clear();
                                        moves.clear();

                                        maxCapture++;
                                    }

                                    for (int step : move) {//empty square(s)
                                        if (captures.size() == maxCapture) {
                                            pieceMoves.add(new Move(captures, step));
                                        }

                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.hasNext(step)) {
                                                next = diagonal.getNext(step);                                                

                                                if (piece == KING[color] && !move.contains(next)) {
                                                    while (board[next] == EMPTY && diagonal.hasNext(next)) {
                                                        next = diagonal.getNext(next);
                                                    }
                                                }

                                                //extra capture
                                                if (pieces[1 - color].contains(next) && !captures.contains(next) && diagonal.hasNext(next)) {
                                                    capture = next;
                                                    next = diagonal.getNext(capture);

                                                    if (board[next] == EMPTY) {
                                                        captureMove = new ArrayList(captures);//<<captures>>
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, next}));//<capture, next>

                                                        if (piece == KING[color] && diagonal.hasNext(next)) {
                                                            do {
                                                                next = diagonal.getNext(next);
                                                                
                                                                if (board[next] == EMPTY) {
                                                                    captureMove.add(next);
                                                                }
                                                            } while (board[next] == EMPTY && diagonal.hasNext(next));
                                                        }

                                                        captureMoves.add(captureMove);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());

                                board[index] = piece;
                            }
                        }
                    }
                }
            }
            
            //moveable
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves.toArray(new Move[pieceMoves.size()]));
            }
        }

        //2 evaluation
        //continue only if this (game) is on SQUAREBOARD
        if (getParent() == SQUAREBOARD) {
            if (moves.isEmpty()) {//gameover
                GAME_OVER.setVisible(true);
            } else if (color == player) {//player
                hintBoard.setHint(moves.keySet());
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...please

                        new PieceMove(color, MinMax.getAIMove(color, board, pieces, moves, AI.getValue())).run();
                    }
                }.start();
            }

            //enable undo
            if (moves.isEmpty() || (color == player && !positions.isEmpty())) {
                UNDO.setEnabled(true);
            }
        }
    }
    
    //undo move
    @Override
    public void actionPerformed(ActionEvent e) {
        //quit turn
        UNDO.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_OVER.setVisible(false);
        } else {
            hintBoard.setVisible(false);
        }
        
        //previous position
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(positions.pop());
        pieceBoard.repaint();

        turn(player);
    }
    
    //step in 4 directions
    private static enum Direction {
        MIN_X_MIN_Y(-1, -1),
        PLUS_X_MIN_Y(1, -1),
        MIN_X_PLUS_Y(-1, 1),
        PLUS_X_PLUS_Y(1, 1);

        final int x;
        final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean hasNext(int index) {//can move?
            int x = x(index) + this.x;
            int y = y(index) + this.y;

            return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
        }

        int getNext(int index) {//to
            return (x(index) + x) / 2 + (y(index) + y) * (SIZE / 2);
        }

        static Direction getDirection(int from, int to) {//from -> to
            if (x(from) > x(to)) {//-x
                if (from > to) {//-y
                    return MIN_X_MIN_Y;
                } else {//+y
                    return MIN_X_PLUS_Y;
                }
            } else {//+x
                if (from > to) {//-y
                    return PLUS_X_MIN_Y;
                } else {//+y
                    return PLUS_X_PLUS_Y;
                }
            }
        }
    }
    
    //animation constants
    final private static int FRAMES = 38;//frames p square
    final private static int MILLI = 4;//milliseconds p frame
    final private static int DELAY = 120;//milliseconds delay time
    
    //animation, promotion, captures
    private class PieceMove extends AbstractBoard implements Runnable {
        int color;
        int index;
        char piece;
        Image image;
        
        PieceMove(int color, ArrayList<Integer> move) {
            super(SQUAREBOARD.square);
            
            this.color = color;
            
            //finish turn color
            if (color == player) {//player
                hintBoard.setVisible(false);
                UNDO.setEnabled(false);
                positions.push(pieceBoard.getBoard());
            } else {//ai
                pieceBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            index = move.remove(0);
            piece = pieceBoard.getIndex(index);
            image = IMAGE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            
            setBounds(square[index]);
            
            //prepare animation
            pieceBoard.setMove(move);
            pieceBoard.repaint();
            pieceBoard.setIndex(index, EMPTY);
            pieceBoard.add(this);
        }
        
        @Override
        public void run() {
            //animation 
            Direction direction = Direction.getDirection(index, pieceBoard.getMove().get(0));

            for (int step : pieceBoard.getMove()) {//<<captures>, destination>
                do {
                    index = direction.getNext(index);
                    
                    //move to square[index]
                    for (int horizontal = square[index].x - getX(), vertical = square[index].y - getY(), i = FRAMES - 1; i >= 0; i--) {
                        long time = System.currentTimeMillis();
                        
                        setLocation(square[index].x - (int) (i * (float) horizontal / FRAMES), square[index].y - (int) (i * (float) vertical / FRAMES));
                        
                        try {
                            Thread.sleep(Math.max(0, time + MILLI - System.currentTimeMillis()));
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (x(step) - x(index)) != -direction.y * (y(step) - y(index)));//x!=-y -> step=index (0=-0) or 90 degree angle (x=-y)
    
                //90 degree angle
                if (index != step) {
                    direction = Direction.getDirection(index, step);
                    
                    try {
                        Thread.sleep(DELAY);
                    } catch (Exception ex) {}
                }
            }            
            
            //promotion
            if (piece == W && index < SIZE / 2) {//<5
                piece = W_KING;//w->W
            } else if (piece == B && index >= square.length - SIZE / 2) {//>=45
                piece = B_KING;//b->B
            }

            //finish move
            pieceBoard.remove(this);
            pieceBoard.setIndex(index, piece);
            pieceBoard.repaint();
            
            try {
                Thread.sleep(DELAY);
            } catch (Exception ex) {}
            
            //captures
            for (int i = 0; i < maxCapture; i++) {
                pieceBoard.setIndex(pieceBoard.getMove().remove(0), EMPTY);
                pieceBoard.repaint();
                        
                try {
                    Thread.sleep(DELAY);
                } catch (Exception ex) {}
            }

            //turn opponent
            turn(1 - color);
        }
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }

}

