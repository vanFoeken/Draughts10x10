package draughts;

import static draughts.PieceBoard.PIECE;
import static draughts.PieceBoard.WB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
 * Board10x10
 * 
 * Board with tiles (50).
 * 
 * BOARD10X10 -> this
 * LEVEL (1-5) -> 1 level = 2 moves
 * ARROW -> undo
 * GAME_INFO -> player and winner
 * HINT -> moveable (on/off)
 * 
 * main (jframe)
 * 
 * menu -> WHITE or BLACK
 * rotation -> rotate BOARD10X10
 * 
 * @author vanFoeken
 */

final class Board10x10 extends JPanel implements ActionListener {
    final static int WHITE = 0;
    final static int BLACK = 1;
    
    final static int GRID = 10;//10x10

    final private static Color LIGHT = new Color(255, 250, 174);//board
    final private static Color DARK = new Color(177, 127, 84);//tile
    
    final static Board10x10 BOARD10X10 = new Board10x10();//this

    final static JSlider LEVEL = new JSlider(1, 5);//ai
    
    final static JButton ARROW = new JButton(new ImageIcon("arrow.png"));//undo
    final static JLabel GAME_INFO = new JLabel();
    final static JCheckBox HINT = new JCheckBox("Hint");//moveable
    
    private static Game game = new Game(WHITE);//WHITE by default
    
    final Rectangle[] tile = new Rectangle[GRID * GRID / 2];//[50]

    private Board10x10() {
        super(new BorderLayout());
   
        setBackground(LIGHT);
        setForeground(DARK);
        
        addContainerListener(new ContainerAdapter() {//game on/off BOARD10X10
            @Override
            public void componentAdded(ContainerEvent e) {//on
                ARROW.addActionListener((Game) e.getChild());
            }        
            
            @Override
            public void componentRemoved(ContainerEvent e) {//off
                ARROW.removeActionListener((Game) e.getChild());
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth() / GRID;//tile width
                int height = getHeight() / GRID;//tile height

                for (int i = 0; i < tile.length; i++) {//sized once -> tile[0] top left (1,0)
                    tile[i] = new Rectangle(x(i) * width, y(i) * height, width, height);//bounds
                }

                for (char piece : WB.toCharArray()) {//tile size
                    PIECE[0][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().getImage(piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//man
                    PIECE[1][WB.indexOf(piece)] = Toolkit.getDefaultToolkit().createImage(piece + "" + piece + ".png").getScaledInstance(width, height, Image.SCALE_SMOOTH);//king
                }
            }
        });
    }

    static int x(int index) {//column
        return index % (GRID / 2) * 2 + 1 - index / (GRID / 2) % 2;
    }

    static int y(int index) {//row
       return index / (GRID / 2);
    }
    
    static void paintTile(Graphics g, Rectangle tile) {
        g.fillRect(tile.x, tile.y, tile.width, tile.height);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
           
        for (Rectangle tile : tile) {
            paintTile(g, tile);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {//rotate
        for (Rectangle tile : tile) {
            tile.setLocation(getWidth() - tile.x - tile.width, getHeight() - tile.y - tile.height);
        }
        
        repaint();
    }
    
    public static void main(String[] args) {
        int boardSize = 560;//BOARD10X10

        JFrame frame = new JFrame("Dam10x10");

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
            menu.add(new JMenuItem(new ImageIcon(Toolkit.getDefaultToolkit().createImage(WB.toCharArray()[color] + ".png").getScaledInstance(40, 40, Image.SCALE_SMOOTH)))).addActionListener(e -> {
                BOARD10X10.remove(game);
                
                game = new Game(color);

                BOARD10X10.add(game, BorderLayout.CENTER);
                BOARD10X10.validate();
            });
        }
        
        BOARD10X10.setPreferredSize(new Dimension(boardSize, boardSize));
        BOARD10X10.add(game, BorderLayout.CENTER);
        
        LEVEL.setMajorTickSpacing(1);
        LEVEL.setPaintTicks(true);//no labels -> see ToolTip
        LEVEL.setOpaque(false);
        LEVEL.setToolTipText("" + LEVEL.getValue());//3
        LEVEL.addChangeListener(e -> LEVEL.setToolTipText("" + LEVEL.getValue()));//level
        
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

        south.add(left);
        south.add(GAME_INFO);
        south.add(right);
        
        left.add(ARROW);

        right.add(HINT);
        right.add(rotation);
        
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("bb.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));//black king
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        frame.setJMenuBar(menuBar);
        frame.add(center, BorderLayout.CENTER);
        frame.add(south, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

}
