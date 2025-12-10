package draughts; 

import static draughts.Board10x10.GRID;
import static draughts.Game.KING;
import static draughts.PieceBoard.EMPTY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import static draughts.Game.MAN;

/**
 * MinMax
 * 
 * Minimax with alfa beta pruning using bitboards (bit 0-49)
 * extends HashMap -> no dubbel calculations
 * 1 depth (level) = 2 moves (ALFA, BETA)
 * depth=0 continue while maxCapture > 0
 * value -> -/+ maxCapture
 * 
 * INFINITY -> alfa(+) beta(-)
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bitboards)
 * 
 * -valueOf -> 1 moves, maxCapture
 *             2 alfa beta pruning
 * -getBoard (static) -> color + board + piece + captured + to -> key
 * -getAIMove (static) -> best move
 * 
 * Special Thanx to Logic Crazy Chess!!
 * 
 * @author vanFoeken
 */

final class MinMax extends HashMap<String, Integer> {
    final private static int ALFA = Integer.MAX_VALUE;//alfa beta
    final private static int BETA = Integer.MIN_VALUE;//alfa beta
    
    private static enum Node {
        ALFA {//ai
            @Override
            int toAlfaBeta(int alfa, int value) {
                return Integer.max(alfa, value);
            }

            @Override
            int valueOf(int maxCapture) {
                return maxCapture;
            }
        },
        BETA {//player
            @Override
            int toAlfaBeta(int beta, int value) {
                return Integer.min(beta, value);
            }

            @Override
            int valueOf(int maxCapture) {
                return -maxCapture;
            }
        };

        abstract int toAlfaBeta(int alfaBeta, int value);
        abstract int valueOf(int maxCapture);
    }
    
    final private static int COLUMN = GRID / 2;//5
    final private static int ROW = GRID - 1;//9
    
    private static enum Diagonal {
        MIN_LEFT(COLUMN, 0, -COLUMN) {//--
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_RIGHT(COLUMN - 1, 0, -COLUMN + 1) {//+-
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ Long.reverse(Long.reverse(diagonal & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_LEFT(COLUMN, ROW, COLUMN) {//-+
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = RIGHT_LEFT[index % COLUMN + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        }, 
        PLUS_RIGHT(COLUMN - 1, ROW, COLUMN + 1) {//++
            @Override
            long getLine(int index, long occupied, long from) {
                long diagonal = LEFT_RIGHT[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / GRID];

                return diagonal & (occupied ^ ((diagonal & occupied) - from));
            }
        };

        final int column;
        final int row;
        final int step;

        Diagonal(int column, int row, int step) {
            this.column = column;
            this.row = row;
            this.step = step;
        }

        boolean hasNext(int index) {
            return index % GRID != column && index / COLUMN != row;
        }

        long getNext(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        abstract long getLine(int index, long occupied, long from);

        final private static long[] LEFT_RIGHT = new long[GRID];//-+
        final private static long[] RIGHT_LEFT = new long[GRID - 1];//+-

        static {
            for (int i = 0; i < LEFT_RIGHT.length; i++) {
                LEFT_RIGHT[i] = 0l;

                //j: 4-0, 5-45 (1-9, 9-1)
                for (int j = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * GRID; Long.bitCount(LEFT_RIGHT[i]) < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j += COLUMN + 1 - j / COLUMN % 2) {
                    LEFT_RIGHT[i] ^= 1l << j;
                }
            }

            for (int i = 0; i < RIGHT_LEFT.length; i++) {
                RIGHT_LEFT[i] = 0l;

                //j: 0-4-44 (2-10-2)
                for (int j = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * GRID; Long.bitCount(RIGHT_LEFT[i]) < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j += COLUMN - j / COLUMN % 2) {
                    RIGHT_LEFT[i] ^= 1l << j;
                }
            }
        }
    }

    private static long middle = 0l;//can move in 4 directions
    
    static {//0<x<9 & 0<y<9
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % GRID != COLUMN - 1 && i % GRID != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //1 moves maxCapture
    //2 alfa beta pruning
    //Like Game.turn (don't invent the wheel twice)
    private int valueOf(char[] board, long hasTurn, long opponent, MinMax minMax, int[] alfaBeta, int value, int depth) {
        //1 moves maxCapture
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
    
        for (long empty = ~(hasTurn ^ opponent), pieces = hasTurn; pieces != 0l; pieces ^= Long.lowestOneBit(pieces)) {
            int from = Long.numberOfTrailingZeros(pieces);
            boolean isKing = board[from] == KING[color];
            HashSet<Long> movesPiece = new HashSet();
            int maxCapturePiece = maxCapture;

            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_LEFT, Diagonal.PLUS_LEFT}, {Diagonal.MIN_RIGHT, Diagonal.PLUS_RIGHT}}) {//x
                for (Diagonal vertical : horizontal) {//y -> [WB]
                    if (vertical.hasNext(from)) {
                        long move = vertical.getNext(from);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getLine(from, ~empty, move);
                        }
                       
                        long capture = move & opponent;
                        
                        if ((capture & middle) != 0l) {//can jump
                            long step = vertical.getNext(Long.numberOfTrailingZeros(capture));//tile after capture
                            
                            if ((step & empty) == step) {//legal capture
                                if (isKing && (step & middle) == step) {
                                    step = vertical.getLine(from, ~empty, step) & empty;
                                }
                                
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ step}));
                                
                                empty ^= 1l << from;//piece off board
                                
                                do {//check captureMoves for extra captures
                                    move = captureMoves.remove(0);//captureMove

                                    long captures = move & opponent;
                
                                    if (Long.bitCount(captures) >= maxCapturePiece) {
                                        if (Long.bitCount(captures) > maxCapturePiece) {
                                            movesPiece.clear();
                                            maxCapturePiece++;
                                        }
                                        
                                        movesPiece.add(move);//legal move
                                    }
                                    
                                    for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {//empty
                                        int to = Long.numberOfTrailingZeros(destination);

                                        for (Diagonal diagonal : Diagonal.values()) {
                                            if (diagonal.hasNext(to)) {
                                                step = diagonal.getNext(to);
                                                
                                                if (isKing && (step & middle & empty) == step) {
                                                    step = diagonal.getLine(to, ~empty, step);
                                                }

                                                if ((step & move) == 0l) {//no dubbels
                                                    capture = step & opponent;

                                                    if ((capture & middle) != 0l) {//can jump
                                                        step = diagonal.getNext(Long.numberOfTrailingZeros(capture));//tile after capture

                                                        if ((step & empty) == step) {//legal capture
                                                            if (isKing && (step & middle) == step) {
                                                                step = diagonal.getLine(to, ~empty, step) & empty;
                                                            }

                                                            captureMoves.add(captures ^ capture ^ step);//captureMove
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all captureMoves checked
                                
                                empty ^= 1l << from;//piece on board
                            }
                        }
            
                        if (maxCapturePiece == 0 && (isKing || vertical == horizontal[color])) {//0 capture move
                            move &= empty;//empty tiles only

                            if (move != 0l) {//legal move
                                movesPiece.add(move);
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
                
                moves.put(from, movesPiece);
            }
        }
        
        //2 alfa beta pruning
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//continue
            depth -= node.ordinal();//ALFA-0, BETA-1
        } else if (maxCapture == 0) {
            return value;
        }

        value += node.valueOf(maxCapture);//value -/+ maxCapture

        for (int from : moves.keySet()) {
            char piece = board[from];

            board[from] = EMPTY;//piece off board

            for (long move : moves.get(from)) {
                long captures = move & opponent;
                ArrayList<Integer> captured = new ArrayList();

                for (long l = captures; l != 0l; l ^= Long.lowestOneBit(l)) {//captures -> captured
                    captured.add(Long.numberOfTrailingZeros(l));
                }

                for (long destination = move ^ captures; destination != 0l; destination ^= Long.lowestOneBit(destination)) {//empty
                    int to = Long.numberOfTrailingZeros(destination);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captured, to));//board after move

                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), opponent ^ captures, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), value, depth));
                    }

                    alfaBeta[node.ordinal()] = node.toAlfaBeta(alfaBeta[node.ordinal()], get(key));

                    if (alfaBeta[Node.ALFA.ordinal()] >= alfaBeta[Node.BETA.ordinal()]) {//alfa>=beta
                        return alfaBeta[node.ordinal()];//prune
                    }
                }
            }

            board[from] = piece;//piece on board
        }
        
        return alfaBeta[node.ordinal()];
    }
    
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captured, int to) {
        if (piece == MAN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        board[to] = piece;        
        captured.forEach(capture -> board[capture] = EMPTY);
        
        return board;//key
    }
    
    static ArrayList<Integer> getAIMove(int ai, char[] board, HashSet<Integer>[] pieces, HashMap<Integer, ArrayList<Integer>[]> moves, int maxCapture, int level) {
        int player = 1 - ai;//opponent
        
        long hasTurn = 0l;//piece[ai]
        long opponent = 0l;//pieces[player]

        for (int index : pieces[ai]) {
            hasTurn ^= 1l << index;
        }
        
        for (int index : pieces[player]) {
            opponent ^= 1l << index;
        }
            
        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int max = BETA;//alfa
        
        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;//piece off board
            
            for (ArrayList<Integer> move : moves.get(from)) {//<<captured>, to>
                int to = move.remove(maxCapture);//only captured left
                long captures = 0l;
                
                for (int capture : move) {//<captured>
                    captures ^= 1l << capture;
                }

                int min = new MinMax(Node.BETA, player).valueOf(getBoard(ai, board.clone(), piece, move, to), opponent ^ captures, hasTurn ^ (1l << from ^ 1l << to), new MinMax(Node.ALFA, ai), new int[] {BETA, ALFA}, maxCapture, level);//beta
                
                if (min >= max) {
                    if (min > max) {
                        alfaMoves.clear();                        
                        
                        max = min;
                    }
                    
                    move.add(0, from);
                    move.add(to);
                    
                    alfaMoves.add(move);
                }
            }
            
            board[from] = piece;//piece on board
        }
       
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));//ai move
    }
    
}
