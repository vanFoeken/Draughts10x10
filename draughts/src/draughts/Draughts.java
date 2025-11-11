package draughts;

import static draughts.PieceBoard.WB;
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
 * Draughts (main)
 *
 * BOARD10X10 -> the board all games are played on
 * 
 * menu -> WHITE or BLACK
 * LEVEL -> 1-5 (AI)
 * ARROW -> undo
 * GAME_INFO -> player and winner
 * HINT -> moveable
 * Rotate -> BOARD10X10
 * 
 * @author vanFoeken
 */

final class Draughts extends JFrame {
    final static int WHITE = 0;
    final static int BLACK = 1;

    final private static int MIN_LEVEL = 1;
    final private static int MAX_LEVEL = 5;//can take some time
    
    final static JSlider LEVEL = new JSlider(MIN_LEVEL, MAX_LEVEL);//1 level = 2 ply
    
    final static Board10x10 BOARD10X10 = new Board10x10();

    final static JButton ARROW = new JButton(new ImageIcon("arrow.png"));//undo
    final static JLabel GAME_INFO = new JLabel();//player and winner
    final static JCheckBox HINT = new JCheckBox("Hint");//moveable
    
    private Game game = new Game(WHITE);//WHITE by default
    
    private Draughts(int boardSize) {
        super("Draughts10x10");

        JMenuBar menuBar = new JMenuBar();//menu, LEVEL
        JMenu menu = new JMenu("Game");//WHITE, BLACK
        
        JButton rotation = new JButton("\ud83d\udd04");//rotate BOARD10X10

        JPanel center = new JPanel();//BOARD10X10
        JPanel south = new JPanel(new GridLayout(1, 3));//left, GAME_INFO, right

        JPanel left = new JPanel();//ARROW
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));//HINT, rotation
        
        menuBar.setLayout(new BorderLayout());
        
        menuBar.add(menu, BorderLayout.WEST);
        menuBar.add(LEVEL, BorderLayout.EAST);
        
        for (int color : new int[] {WHITE, BLACK}) {
            menu.add(new JMenuItem(new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(42, 42, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                BOARD10X10.remove(game);
                
                game = new Game(color);

                BOARD10X10.add(game, BorderLayout.CENTER);
                BOARD10X10.validate();
            });
        }
        
        LEVEL.setMajorTickSpacing(1);
        LEVEL.setPaintTicks(true);//no labels (see ToolTip) -> no distraction
        LEVEL.setOpaque(false);
        LEVEL.setToolTipText("" + LEVEL.getValue());//3
        LEVEL.addChangeListener(e -> LEVEL.setToolTipText("" + LEVEL.getValue()));
        
        BOARD10X10.setPreferredSize(new Dimension(boardSize, boardSize));
        BOARD10X10.addContainerListener(new ContainerAdapter() {//game on/off BOARD10X10
            @Override
            public void componentAdded(ContainerEvent e) {//on
                ARROW.addActionListener((Game) e.getChild());
            }        
            
            @Override
            public void componentRemoved(ContainerEvent e) {//off
                ARROW.removeActionListener((Game) e.getChild());
            }
        });
        BOARD10X10.add(game, BorderLayout.CENTER);
        
        ARROW.setContentAreaFilled(false);
        ARROW.setBorder(null);
        ARROW.setFocusable(false);

        GAME_INFO.setHorizontalAlignment(JLabel.CENTER);
        GAME_INFO.setFont(GAME_INFO.getFont().deriveFont(16f));
        
        HINT.setHorizontalTextPosition(JCheckBox.LEFT);
        HINT.setFont(HINT.getFont().deriveFont(16f));
        HINT.setFocusable(false);
        HINT.addActionListener(e -> game.repaint());
        
        rotation.setContentAreaFilled(false);
        rotation.setBorder(null);
        rotation.setFont(rotation.getFont().deriveFont(Font.PLAIN, 16));
        rotation.setFocusable(false);
        rotation.addActionListener(BOARD10X10);

        center.add(BOARD10X10);

        left.add(ARROW);

        right.add(HINT);
        right.add(rotation);
        
        south.add(left);
        south.add(GAME_INFO);
        south.add(right);
        
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        
        setIconImage(Toolkit.getDefaultToolkit().createImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(menuBar);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        int boardSize = 560;//BOARD10X10

       //start Draughts
        new Draughts(boardSize);
    }

}


