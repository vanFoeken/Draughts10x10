package draughts10x10;

import static draughts10x10.board.PositionBoard.WB;
import draughts10x10.board.SquareBoard;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Draughts10x10 (main)
 * 
 * Game (WHITE/BLACK)
 * AI (1-7)
 * 
 * undo move
 * hint on/off
 * rotate board
 * 
 * @author Naardeze
*/

final public class Draughts10x10 extends JFrame {
    //colors
    final static int WHITE = 0;
    final static int BLACK = 1;

    //search depth
    final private static int MIN_DEPTH = 1;
    final private static int MAX_DEPTH = 7;

    //AI search depth
    final static JSlider AI = new JSlider(MIN_DEPTH, MAX_DEPTH); 
    
    //board10x10
    final static SquareBoard SQUAREBOARD = new SquareBoard();
    
    //GUI components
    final static JButton UNDO = new JButton(new ImageIcon("arrow.png"));
    final static JLabel GAME_OVER = new JLabel("Game Over", JLabel.CENTER);
    final public static JCheckBox HINT = new JCheckBox("Hint");
    
    //current game (white by default)
    private Game game = new Game(WHITE);
    
    private Draughts10x10(int boardSize) {
        super("Draughts10x10");

        JMenuBar menuBar = new JMenuBar();//menu, AI
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate SQUAREBOARD

        JPanel center = new JPanel();//SQUAREBOARD
        JPanel south = new JPanel(new GridLayout(1, 3));//left, GAME_OVER, right

        JPanel left = new JPanel();//UNDO
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation

        //memu
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new String[] {"White", "Black"}[color], new ImageIcon(Toolkit.getDefaultToolkit().getImage(WB.toCharArray()[color] + ".png").getScaledInstance(30, 30, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                SQUAREBOARD.remove(game);
                
                game = new Game(color);
                
                SQUAREBOARD.add(game, BorderLayout.CENTER);
                SQUAREBOARD.validate();
            });
        }

        AI.setMajorTickSpacing(1);
        AI.setPaintTicks(true);
        AI.setOpaque(false);
        AI.setToolTipText("" + AI.getValue());//search depth
        AI.addChangeListener(e -> AI.setToolTipText("" + AI.getValue()));//setToolTipText
        
        SQUAREBOARD.setPreferredSize(new Dimension(boardSize, boardSize));
        SQUAREBOARD.addContainerListener(new ContainerAdapter() {//game on/off SQUAREBOARD
            @Override
            public void componentAdded(ContainerEvent e) {
                UNDO.addActionListener((Game) e.getChild());
            }        
            @Override
            public void componentRemoved(ContainerEvent e) {
                UNDO.removeActionListener((Game) e.getChild());
            }
        });
        SQUAREBOARD.add(game, BorderLayout.CENTER);

        UNDO.setContentAreaFilled(false);
        UNDO.setBorder(null);
        UNDO.setFocusable(false);
        UNDO.setToolTipText("Undo");

        GAME_OVER.setFont(GAME_OVER.getFont().deriveFont(16f));
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFont(HINT.getFont().deriveFont(16f));
        HINT.setFocusable(false);
        HINT.addActionListener(e -> game.repaint());
        HINT.setToolTipText("Hint On/Off");
        
        rotation.setContentAreaFilled(false);
        rotation.setBorder(null);
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 16));
        rotation.setFocusable(false);
        rotation.addActionListener(SQUAREBOARD);
        rotation.setToolTipText("Rotate");
        
        menuBar.setLayout(new BorderLayout());
        
        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(AI, BorderLayout.EAST);
        
        center.add(SQUAREBOARD);

        left.add(UNDO);

        right.add(HINT);
        right.add(rotation);
        
        south.add(left);
        south.add(GAME_OVER);
        south.add(right);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        setIconImage(Toolkit.getDefaultToolkit().getImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(menuBar);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args)throws Exception {
        //SQUAREBOARD size
        final int boardSize = 560;

        //start Draughts10x10
        new Draughts10x10(boardSize);
    }

}
