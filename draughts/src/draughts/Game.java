package draughts;

import static draughts.Board10x10.getColumn;
import static draughts.Board10x10.getRow;
import static draughts.Board10x10.GRID;
import static draughts.Draughts.ARROW;
import static draughts.Draughts.BLACK;
import static draughts.Draughts.BOARD10X10;
import static draughts.Draughts.GAME_INFO;
import static draughts.Draughts.LEVEL;
import static draughts.Draughts.WHITE;
import static draughts.HintBoard.NOT_SELECTED;
import static draughts.PieceBoard.EMPTY;
import static draughts.PieceBoard.PIECE;
import static draughts.PieceBoard.WB;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
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

/**
 * Game
 * 
 * Gameloop, logic and move animation
 * 
 * enum Direction -> move in 4 directions (x, y)
 *
 * pieceBoard -> pieces and colored move tiles
 * hintBoard -> selected or moveable player pieces, mouseadapter for player move
 * 
 * -turn(color) -> 1: pieces, moves, maxCapture
 *                 2: evaluation -> gameover or get move (player (mouse) or ai (minimax))
 * -actionPerformed -> undo move
 * 
 * class BoardMove -> animation
 * 
 * @author vanFoeken
 */

final class Game extends JLayeredPane implements ActionListener {
    final static char[] PAWN = WB.toCharArray();//wb
    final static char[] KING = WB.toUpperCase().toCharArray();//WB
    
    final private static String[] COLOR = {"White", "Black"};

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

        boolean hasNext(int index) {
            int x = getColumn(index) + this.x;
            int y = getRow(index) + this.y;

            return x >= 0 && x < GRID && y >= 0 && y < GRID;
        }

        int getNext(int index) {
            return (getColumn(index) + x) / 2 + (getRow(index) + y) * (GRID / 2);
        }

        static Direction getDirection(int from, int to) {//from -> to
            if (getColumn(from) > getColumn(to)) {//-getColumn
                if (from > to) {//-getRow
                    return MIN_X_MIN_Y;
                } else {//+getRow
                    return MIN_X_PLUS_Y;
                }
            } else {//+getColumn
                if (from > to) {//-getRow
                    return PLUS_X_MIN_Y;
                } else {//+getRow
                    return PLUS_X_PLUS_Y;
                }
            }
        }
    }
 
    final private PieceBoard pieceBoard = new PieceBoard();//pieces, move
    final private HintBoard hintBoard = new HintBoard();//selected, moveable

    final private Stack<String> boards = new Stack();//undo
    
    final private int player;//color
    
    private HashSet<Integer>[] pieces = new HashSet[WB.length()];
    private HashMap<Integer, ArrayList<Integer>[]> moves;
    private int maxCapture;
    
    Game(int player) {
        this.player = player;

        ARROW.setEnabled(false);
        GAME_INFO.setText("Player is " + COLOR[player]);//text disappears after first player move

        hintBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int pressed = 0; pressed < BOARD10X10.tile.length; pressed++) {
                    if (BOARD10X10.tile[pressed].contains(e.getPoint())) {
                        int selected = hintBoard.getSelected();

                        //1 multiple moves with same destination -> press all steps of move
                        if (selected != NOT_SELECTED && !pieceBoard.getMove().isEmpty() && (pieceBoard.getIndex(pressed) == EMPTY || pressed == selected)) {
                            ArrayList<Integer> move = new ArrayList(pieceBoard.getMove());//captured
                            int step = move.remove(move.size() - 1);

                            if (pressed != step && Math.abs(getColumn(pressed) - getColumn(step)) == Math.abs(getRow(pressed) - getRow(step))) {//diagonal
                                Direction direction = Direction.getDirection(step, pressed);

                                step = direction.getNext(step);

                                if (pieceBoard.getIndex(selected) == KING[player]) {
                                    while (step != pressed && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                        step = direction.getNext(step);
                                    }
                                }

                                //capture
                                if (pieces[1 - player].contains(step) && !move.contains(step)) {
                                    move.add(step);

                                    step = direction.getNext(step);

                                    if (pieceBoard.getIndex(selected) == KING[player]) {
                                        while (step != pressed && (pieceBoard.getIndex(step) == EMPTY || step == selected)) {
                                            step = direction.getNext(step);
                                        }
                                    }

                                    if (step == pressed) {
                                        move.add(step);
                                        
                                        if (move.indexOf(step) == maxCapture) {//legal move
                                            new Thread(new BoardMove(selected, move)).start();
                                        } else {
                                            pieceBoard.setMove(move);
                                            pieceBoard.repaint();
                                        }
                                    }
                                }
                            }
                        //2 pressed = occupied
                        } else if (pieceBoard.getIndex(pressed) != EMPTY) {
                            pieceBoard.getMove().clear();

                            if (moves.containsKey(pressed)) {
                                ArrayList<Integer>[] move = moves.get(pressed);

                                if (move.length == 1) {//1 option
                                    new Thread(new BoardMove(pressed, move[0])).start();
                                } else {
                                    hintBoard.setSelected(pressed);

                                    //check: multiple moves with same destination
                                    loop : for (int i = 1; i < move.length; i++) {
                                        for (int to = move[i].get(maxCapture), j = 0; j < i; j++) {
                                            if (move[j].get(maxCapture) == to) {
                                                pieceBoard.getMove().add(pressed);

                                                break loop;
                                            }
                                        }
                                    }
                                }
                            } else {
                                hintBoard.setSelected(NOT_SELECTED);
                            }
                            
                            hintBoard.repaint();
                        //3 pressed = EMPTY, from != NOT_SELECTED
                        } else if (moves.containsKey(selected)) {
                            for (ArrayList<Integer> move : moves.get(selected)) {
                                if (move.get(maxCapture) == pressed) {
                                    new Thread(new BoardMove(selected, move)).start();
                                }
                            }
                        }                        

                        break;
                    }
                }
            }
        });
        hintBoard.addComponentListener(new ComponentAdapter() {//used only once by first player move
            @Override
            public void componentHidden(ComponentEvent e) {
                hintBoard.removeComponentListener(this);

                GAME_INFO.setText("");
            }
        });
         
        add(pieceBoard, new Integer(1));
        add(hintBoard);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pieceBoard.setSize(getSize());
                hintBoard.setSize(getSize());
   
                //prevent: player = black and animation starts before boards are sized (don't look nice)
                turn(WHITE);//WHITE begins
            }
        });
    }
    
    //1 pieces, moves, maxCapture
    //2 evaluation
    private void turn(int color) {
        //1 pieces, moves, maxCapture
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
        
        int opponent = 1 - color;

        for (int from : pieces[color]) {
            char piece = board[from];
            HashSet<ArrayList<Integer>> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;
            
            for (Direction[] horizontal : new Direction[][] {{Direction.MIN_X_MIN_Y, Direction.MIN_X_PLUS_Y}, {Direction.PLUS_X_MIN_Y, Direction.PLUS_X_PLUS_Y}}) {//x
                for (Direction vertical : horizontal) {//y -> [WB]
                    if (vertical.hasNext(from)) {
                        int step = vertical.getNext(from);
                        
                        //empty
                        if(board[step] == EMPTY && (piece == KING[color] || vertical == horizontal[color])) {
                            if (maxCapturePiece == 0) {//legal move
                                movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));//<step>
                            }

                            if (piece == KING[color] && vertical.hasNext(step)) {
                                do {
                                    step = vertical.getNext(step);

                                    if (maxCapturePiece == 0 && board[step] == EMPTY) {//legal move
                                        movesPiece.add(new ArrayList(Arrays.asList(new Integer[] {step})));//<step>
                                    }
                                } while (board[step] == EMPTY && vertical.hasNext(step));
                            }
                        }

                        //capture
                        if (pieces[opponent].contains(step) && vertical.hasNext(step)) {
                            int capture = step;

                            step = vertical.getNext(capture);
                            
                            if (board[step] == EMPTY) {//capture is legal
                                ArrayList<Integer> captureMove = new ArrayList(Arrays.asList(new Integer[] {capture, step}));

                                if (piece == KING[color] && vertical.hasNext(step)) {
                                    do {
                                        step = vertical.getNext(step);

                                        if (board[step] == EMPTY) {
                                            captureMove.add(step);
                                        }
                                    } while (board[step] == EMPTY && vertical.hasNext(step));
                                }

                                ArrayList<ArrayList<Integer>> captureMoves = new ArrayList(Arrays.asList(new ArrayList[] {captureMove}));//<<capture, step(s)>>

                                board[from] = EMPTY;

                                //check captureMoves for extra captures
                                do {
                                    ArrayList<Integer> destination = captureMoves.remove(0);//<captureMove>
                                    ArrayList<Integer> captured = new ArrayList();

                                    do {//opponent<->empty;
                                        captured.add(destination.remove(0));
                                    } while (pieces[opponent].contains(destination.get(0)));

                                    if (captured.size() > maxCapturePiece) {
                                        movesPiece.clear();                                       
                                        maxCapturePiece++;
                                    }

                                    for (int to : destination) {//empty
                                        if (captured.size() == maxCapturePiece) {//legal move
                                            ArrayList<Integer> move = new ArrayList(captured);
                                            
                                            move.add(to);
                                            movesPiece.add(move);
                                        }

                                        for (Direction diagonal : Direction.values()) {
                                            if (diagonal.hasNext(to)) {
                                                step = diagonal.getNext(to);                                                
                                                
                                                if (piece == KING[color] && !destination.contains(step)) {//no dubbel check
                                                    while (board[step] == EMPTY && diagonal.hasNext(step)) {
                                                        step = diagonal.getNext(step);
                                                    }
                                                }

                                                //extra capture
                                                if (pieces[opponent].contains(step) && !captured.contains(step) && diagonal.hasNext(step)) {
                                                    capture = step;
                                                    step = diagonal.getNext(capture);

                                                    if (board[step] == EMPTY) {//extra capture is legal
                                                        captureMove = new ArrayList(captured);
                                                        captureMove.addAll(Arrays.asList(new Integer[] {capture, step}));

                                                        if (piece == KING[color] && diagonal.hasNext(step)) {
                                                            do {
                                                                step = diagonal.getNext(step);
                                                                
                                                                if (board[step] == EMPTY) {
                                                                    captureMove.add(step);
                                                                }
                                                            } while (board[step] == EMPTY && diagonal.hasNext(step));
                                                        }

                                                        captureMoves.add(captureMove);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all captureMoves checked

                                board[from] = piece;
                            }
                        }
                    }
                }
            }
            
            if (!movesPiece.isEmpty()) {
                if (maxCapturePiece > maxCapture) {
                    moves.clear();
                    maxCapture = maxCapturePiece;
                }

                moves.put(from, movesPiece.toArray(new ArrayList[movesPiece.size()]));
            }
        }
        
        //2 evaluation
        if (BOARD10X10.isAncestorOf(this)) {//continue only if this (game) is on BOARD10X10; prevent enable ARROW by new game during LEVEL search
            if (moves.isEmpty()) {//game over
                GAME_INFO.setText(COLOR[opponent] + " is Winner");
            } else if (color == player) {//player
                hintBoard.setMoveable(moves.keySet());
                hintBoard.setVisible(true);
            } else {//ai
                new Thread(){
                    @Override
                    public void run() {
                        pieceBoard.setCursor(new Cursor(Cursor.WAIT_CURSOR));//one moment...

                        new BoardMove(MinMax.getAIMove(color, board, pieces, moves, maxCapture, LEVEL.getValue())).run();
                    }
                }.start();
            }
            
            if (moves.isEmpty() || (color == player && !boards.isEmpty())) {
                ARROW.setEnabled(true);
            }
        }
    }
    
    //undo -> player is color or game over
    @Override
    public void actionPerformed(ActionEvent e) {
        ARROW.setEnabled(false);
        
        if (moves.isEmpty()) {
            GAME_INFO.setText("");
        } else {
            hintBoard.setVisible(false);
        }
        
        pieceBoard.getMove().clear();
        pieceBoard.setBoard(boards.pop());

        turn(player);
    }
    
    //animation: move, promotion, capture
    private class BoardMove extends Component implements Runnable {
        final static int FRAMES = 33;//frames p tile
        final static int MILLI = 4;//milliseconds p frame
        final static int DELAY = 124;//milliseconds
    
        int color;        
        char piece;
        Image image;
        Point point;
        int to;
        
        BoardMove(int from, ArrayList<Integer> move) {//player
            this(player, from, move);
        }

        BoardMove(ArrayList<Integer> move) {//ai
            this(1 - player, move.remove(0), move);
        }

        BoardMove(int color, int from, ArrayList<Integer> move) {//color
            this.color = color; 
            
            if (color == player) {
                hintBoard.setVisible(false);
                ARROW.setEnabled(false);
                boards.push(pieceBoard.getBoard());
            } else {//ai
                pieceBoard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            piece = pieceBoard.getIndex(from);
            image = PIECE[(PAWN[color] + "" + KING[color]).indexOf(piece)][color];
            point = BOARD10X10.tile[from].getLocation();
            to = from;
  
            setSize(pieceBoard.getSize());

            pieceBoard.setMove(move);
            pieceBoard.setIndex(from, EMPTY);
            pieceBoard.add(this, BorderLayout.CENTER);
        }
        
        //animation
        @Override
        public void run() {
            //move
            Direction direction = Direction.getDirection(to, pieceBoard.getMove().get(0));

            for (int step : pieceBoard.getMove()) {//<<captured>, to>
                do {
                    to = direction.getNext(to);
                    
                    //point -> tile[to]
                    for (int horizontal = BOARD10X10.tile[to].x - point.x, vertical = BOARD10X10.tile[to].y - point.y, i = FRAMES - 1; i >= 0; i--) {
                        point.setLocation(BOARD10X10.tile[to].x - (int) (i * (float) horizontal / FRAMES), BOARD10X10.tile[to].y - (int) (i * (float) vertical / FRAMES));

                        repaint();
                        
                        try {
                            Thread.sleep(MILLI);
                        } catch (Exception ex) {}
                    }
                } while (direction.x * (getColumn(step) - getColumn(to)) != direction.y * (getRow(to) - getRow(step)));//x!=-getRow
    
                if (to != step) {//90 degree angle
                    direction = Direction.getDirection(to, step);
                    
                    try {
                        Thread.sleep(DELAY);
                    } catch (Exception ex) {}
                }
            }            
            
            //promotion
            if (piece == PAWN[color] && ((color == WHITE && to < GRID / 2) || (color == BLACK && to >= BOARD10X10.tile.length - GRID / 2))) {
                piece = KING[color];
            }

            pieceBoard.remove(this);
            pieceBoard.setIndex(to, piece);
            pieceBoard.repaint();
            
            try {
                Thread.sleep(DELAY);
            } catch (Exception ex) {}
            
            //capture
            for (int i = 0; i < maxCapture; i++) {
                pieceBoard.setIndex(pieceBoard.getMove().remove(0), EMPTY);
                pieceBoard.repaint();
                        
                try {
                    Thread.sleep(DELAY);
                } catch (Exception ex) {}
            }

            turn(1 - color);
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(image, point.x, point.y, null);
        }
    }
    
}

