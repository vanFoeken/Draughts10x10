package draughts10x10;

import static draughts10x10.Game.KING;
import static draughts10x10.Game.PAWN;
import static draughts10x10.PieceBoard.EMPTY;
import static draughts10x10.SquareBoard.SIZE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/* MinMax
 *
 * minimax with alfa beta pruning
 * 
 * -valueOf -> move result
 * -getBoard (static) -> moved board
 * -getAIMove (static) -> best move
 * 
 * enum Node -> evaluation
 * enum Diagonal -> move in 4 directions (bitboards)
 */

final class MinMax extends HashMap<String, Integer> {//<board, value>    
    //static constants
    final private static int COLUMN = SIZE / 2;//5
    final private static int ROW = SIZE - 1;//9

    //min max
    final private static int ALFA = Integer.MAX_VALUE;
    final private static int BETA = Integer.MIN_VALUE;

    //center squares (x>0 & x<9 & y>0 & y<9)
    private static long middle = 0l;
    
    final private Node node;
    final private int color;
    
    private MinMax(Node node, int color) {
        this.node = node;
        this.color = color;
    }
    
    //like Game.turn (don't invent the wheel twice)
    //1: moves and maxCapture
    //2: evaluation
    //3: pruning
    private int valueOf(char[] board, long hasTurn, long noTurn, MinMax minMax, int[] alfaBeta, int depth) {
        //1 moves and maxCapture
        HashMap<Integer, HashSet<Long>> moves = new HashMap();
        int maxCapture = 0;
        
        for (long empty = ~(hasTurn ^ noTurn), captureMiddle = noTurn & middle, turn = hasTurn; turn != 0l; turn ^= Long.lowestOneBit(turn)) {
            int index = Long.numberOfTrailingZeros(turn);
            boolean isKing = board[index] == KING[color];
            HashSet<Long> pieceMoves = new HashSet();
            
            for (Diagonal[] horizontal : new Diagonal[][] {{Diagonal.MIN_DIAGONAL, Diagonal.PLUS_COUNTER_DIAGONAL}, {Diagonal.MIN_COUNTER_DIAGONAL, Diagonal.PLUS_DIAGONAL}}) {//-+
                for (Diagonal vertical : horizontal) {//-+
                    if (vertical.hasNext(index)) {
                        long move = vertical.getNext(index);

                        if (isKing && (move & middle & empty) == move) {
                            move = vertical.getKingSteps(index, ~empty, move);
                        }

                        //capture
                        if ((move & captureMiddle) != 0l) {
                            long capture = move & captureMiddle;
                            long next = vertical.getNext(Long.numberOfTrailingZeros(capture));
                            
                            if ((next & empty) == next) {
                                if (isKing && (next & middle) == next) {
                                    next = vertical.getKingSteps(index, ~empty, next) & empty;
                                }
                                
                                //capturemoves to check for extra captures
                                ArrayList<Long> captureMoves = new ArrayList(Arrays.asList(new Long[] {capture ^ next}));//<capture, empty squares>
                                
                                empty ^= 1l << index;
                               
                                //extra captures
                                do {
                                    move = captureMoves.remove(0);
                                 
                                    long captures = move & captureMiddle;
                                    
                                    //maxCapture +1
                                    if (Long.bitCount(captures) >= maxCapture) {
                                        if (Long.bitCount(captures) > maxCapture) {
                                            pieceMoves.clear();
                                            moves.clear();
                                            
                                            maxCapture++;
                                        }
                                        
                                        pieceMoves.add(move);
                                    }
                                    
                                    //not all captured
                                    if (captures != captureMiddle) {
                                        for (long destinations = move ^ captures; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {//empty square(s)
                                            int step = Long.numberOfTrailingZeros(destinations);
                                            
                                            for (Diagonal diagonal : Diagonal.values()) {
                                                if (diagonal.hasNext(step)) {
                                                    next = diagonal.getNext(step);
                                                    
                                                    if (isKing && (next & middle & empty) == next) {
                                                        next = diagonal.getKingSteps(step, ~empty, next);
                                                    }

                                                    //no dubbel steps and captures & extra capture
                                                    if ((next & move) == 0l && (next & captureMiddle) != 0l) {
                                                        capture = next & captureMiddle;
                                                        next = diagonal.getNext(Long.numberOfTrailingZeros(capture));

                                                        if ((next & empty) == next) {
                                                            if (isKing && (next & middle) == next) {
                                                                next = diagonal.getKingSteps(step, ~empty, next) & empty;
                                                            }

                                                            captureMoves.add(captures ^ capture ^ next);//<captures, extra capture, empty squares>
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (!captureMoves.isEmpty());//all captures are checked
                                
                                empty ^= 1l << index;
                            }
                        }
                        
                        //no capture move
                        if (maxCapture == 0 && (isKing || vertical == horizontal[color])) {
                            move &= empty;
                            
                            if (move != 0l) {
                                pieceMoves.add(move);
                            }
                        }
                    }
                }
            }
            
            if (!pieceMoves.isEmpty()) {
                moves.put(index, pieceMoves);
            }
        }
        
        //2 evaluation
        if (moves.isEmpty()) {//game over
            return alfaBeta[node.ordinal()];
        } else if (depth > 0) {//extra search
            depth--;
        } else if (maxCapture == 0) {//move result
            return node.valueOf(Long.bitCount(hasTurn) - Long.bitCount(noTurn));
        }

        //3 pruning
        for (int from : moves.keySet()) {
            char piece = board[from];

            board[from] = EMPTY;

            for (long move : moves.get(from)) {
                long capture = move & noTurn;
                ArrayList<Integer> captures = new ArrayList();

                //capture -> captures
                for (long copy = capture; copy != 0l; copy ^= Long.lowestOneBit(copy)) {
                    captures.add(Long.numberOfTrailingZeros(copy));
                }
                
                for (long destinations = move ^ capture; destinations != 0l; destinations ^= Long.lowestOneBit(destinations)) {
                    int to = Long.numberOfTrailingZeros(destinations);                    
                    String key = String.valueOf(getBoard(color, board.clone(), piece, captures, to));//moves board

                    //check if key (board) already occured
                    if (!containsKey(key)) {
                        put(key, minMax.valueOf(key.toCharArray(), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), this, alfaBeta.clone(), depth));
                    }
                    
                    //move result
                    int value = get(key);

                    if (node.isAlfaBeta(alfaBeta[node.ordinal()], value)) {
                        alfaBeta[node.ordinal()] = value;

                        //prune
                        if (alfaBeta[Node.MAX.ordinal()] >= alfaBeta[Node.MIN.ordinal()]) {
                            return value;
                        }
                    }
                }
            }

            board[from] = piece;
        }
        
        //move result
        return alfaBeta[node.ordinal()];
    }

    //board (key) -> promotion, move, captures
    private static char[] getBoard(int color, char[] board, char piece, ArrayList<Integer> captures, int to) {
        if (piece == PAWN[color] && to / COLUMN == color * ROW) {
            piece = KING[color];
        }

        board[to] = piece;
        
        captures.forEach(capture -> board[capture] = EMPTY);
        
        return board;
    }

    static ArrayList<Integer> getAIMove(int ai, char[] board, HashSet<Integer>[] pieces, HashMap<Integer, Move[]> moves, int depth) {
        //opponent
        int player = 1 - ai;
        
        //pieces bitboards
        long hasTurn = 0l;//ai
        long noTurn = 0l;//player

        //pieces[ai] -> hasTurn
        for (int index : pieces[ai]) {
            hasTurn ^= 1l << index;
        }
        
        //pieces[player] -> noTurn
        for (int index : pieces[player]) {
            noTurn ^= 1l << index;
        }

        //best moves
        ArrayList<ArrayList<Integer>> alfaMoves = new ArrayList();
        int alfa = BETA;

        for (int from : moves.keySet()) {
            char piece = board[from];
            
            board[from] = EMPTY;
            
            for (Move move : moves.get(from)) {
                ArrayList<Integer> captures = move.getCaptures();
                long capture = 0l;//captures bitboard
                
                //captures -> capture
                for (int index : captures) {
                    capture ^= 1l << index;
                }

                int to = move.getTo();
                int beta = new MinMax(Node.MIN, player).valueOf(getBoard(ai, board.clone(), piece, captures, to), noTurn ^ capture, hasTurn ^ (1l << from ^ 1l << to), new MinMax(Node.MAX, ai), new int[] {ALFA, BETA}, depth);
                
                //alfa move
                if (beta >= alfa) {
                    if (beta > alfa) {
                        alfaMoves.clear();
                        
                        alfa = beta;
                    }
                    
                    //boardmove
                    alfaMoves.add(move.getBoardMove(from));
                }
            }
            
            board[from] = piece;
        }
        
        //<- best alfa move (random)
        return alfaMoves.get((int) (Math.random() * alfaMoves.size()));
    }
    
    //evaluation
    private static enum Node {
        MIN {//beta
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value < alfaBeta;
            }
            @Override
            int valueOf(int value) {
                return -value;
            }
        },
        MAX {//alfa
            @Override
            boolean isAlfaBeta(int alfaBeta, int value) {
                return value > alfaBeta;
            }
            @Override
            int valueOf(int value) {
                return value;
            }
        };

        abstract boolean isAlfaBeta(int alfaBeta, int value);//>alfa, <beta
        abstract int valueOf(int value);//isTurn-noTurn
    }
    
    //move in 4 directions
    private static enum Diagonal {
        MIN_DIAGONAL(COLUMN, 0, -COLUMN) {//-x-y
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        MIN_COUNTER_DIAGONAL(COLUMN - 1, 0, -COLUMN + 1) {//+x-y
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long mask = COUNTER_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ Long.reverse(Long.reverse(mask & occupied) - Long.reverse(from)));
            }
        }, 
        PLUS_COUNTER_DIAGONAL(COLUMN, ROW, COLUMN) {//-x+y
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long mask = COUNTER_DIAGONAL[index % COLUMN + index / SIZE];

                return mask & (occupied ^ ((mask & occupied) - from));
            }
        }, 
        PLUS_DIAGONAL(COLUMN - 1, ROW, COLUMN + 1) {//+x+y
            @Override
            long getKingSteps(int index, long occupied, long from) {
                long mask = DIAGONAL[COLUMN - 1 - index % COLUMN + index / COLUMN % 2 + index / SIZE];

                return mask & (occupied ^ ((mask & occupied) - from));
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
            return index % SIZE != column && index / COLUMN != row;
        }

        long getNext(int index) {
            return 1l << index + step - index / COLUMN % 2;
        }

        abstract long getKingSteps(int index, long occupied, long from);

        final private static long[] DIAGONAL = new long[SIZE];//-+
        final private static long[] COUNTER_DIAGONAL = new long[SIZE - 1];//+-

        static {//masks
            for (int i = 0; i < DIAGONAL.length; i++) {
                DIAGONAL[i] = 0l;

                //4-0, 5-45
                for (int j = 0, bit = COLUMN - 1 - Math.min(i, COLUMN - 1) + i / COLUMN * COLUMN + Math.max(0, i - COLUMN) * SIZE; j < 1 + (Math.min(i, COLUMN - 1) - Math.max(0, i - COLUMN)) * 2; j++, bit += COLUMN + 1 - bit / COLUMN % 2) {
                    DIAGONAL[i] ^= 1l << bit;
                }
            }

            for (int i = 0; i < COUNTER_DIAGONAL.length; i++) {
                COUNTER_DIAGONAL[i] = 0l;
        
                //0-4-44
                for (int j = 0, bit = Math.min(i, COLUMN - 1) + Math.max(0, i - (COLUMN - 1)) * SIZE; j < 2 + (Math.min(i, COLUMN - 1) - Math.max(0, i - (COLUMN - 1))) * 2; j++, bit += COLUMN - bit / COLUMN % 2) {
                    COUNTER_DIAGONAL[i] ^= 1l << bit;
                }
            }
        }
    }
    
    static {//middle
        for (int i = COLUMN; i < ROW * COLUMN; i++) {//5<45
            if (i % SIZE != COLUMN - 1 && i % SIZE != COLUMN) {//!=4 & !=5
                middle ^= 1l << i;
            }
        }
    }

}
