package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessException;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChessGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int   SQUARE_SIZE = 88;
    private static final Color LIGHT_SQ    = new Color(238, 238, 210);
    private static final Color DARK_SQ     = new Color(118, 150, 86);
    private static final Color SEL_COLOR   = new Color(205, 210, 106, 180);
    private static final Color HINT_COLOR  = new Color(32, 195, 154, 160);
    private static final Color SIDE_BG     = new Color(16, 16, 16);

    private ChessMatch     chessMatch;
    private ChessPiece[][] pieces;
    private boolean[][]    possibleMoves;
    private ChessPosition  selectedPosition;

    private final Map<String, Image> imageCache       = new HashMap<>();

    private BoardPanel  boardPanel;
    private JLabel      statusLabel;

    public ChessGUI() {
        chessMatch = new ChessMatch();
        pieces     = chessMatch.getPieces();
        loadImages();

        setTitle("Chess — Java OOP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(25, 25, 25));

        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Load images ────────────────────────────────────────────────
    private void loadImages() {
        String[] keys = {"wK","wQ","wR","wB","wN","wP","bK","bQ","bR","bB","bN","bP"};
        for (String key : keys) {
            try {
                BufferedImage img = ImageIO.read(new File("src/images/" + key + ".png"));
                if (img != null) imageCache.put(key, img);
            } catch (IOException e) {
                System.out.println("Missing image: " + key);
            }
        }
    }

    private Image getImage(ChessPiece piece) {
        boolean white = piece.getColor() == chess.Color.WHITE;
        String color  = white ? "w" : "b";
        String type;
        switch (piece.getClass().getSimpleName()) {
            case "King":   type = "K"; break;
            case "Queen":  type = "Q"; break;
            case "Rook":   type = "R"; break;
            case "Bishop": type = "B"; break;
            case "Knight": type = "N"; break;
            case "Pawn":   type = "P"; break;
            default: return null;
        }
        return imageCache.get(color + type);
    }

    // ── Build UI ─────────────────────────────────────────────────
    private void buildUI() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Side panel
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDE_BG);
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(55, 55, 55)));

        side.add(playerBar("Black Player", false), BorderLayout.NORTH);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 1, 4, 0));
        btnPanel.setBackground(new Color(18, 18, 18));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton newBtn = sideBtn("New Game", new Color(60, 100, 60));
        newBtn.addActionListener(e -> resetGame());
        btnPanel.add(newBtn);

        JPanel southSide = new JPanel(new BorderLayout());
        southSide.setBackground(SIDE_BG);
        southSide.add(btnPanel, BorderLayout.NORTH);
        southSide.add(playerBar("White Player", true), BorderLayout.SOUTH);
        side.add(southSide, BorderLayout.SOUTH);

        add(side, BorderLayout.EAST);

        // Status bar
        statusLabel = new JLabel("White to move");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(210, 210, 210));
        statusLabel.setBackground(new Color(20, 20, 20));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel playerBar(String name, boolean isWhite) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(new Color(18, 18, 18));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(45, 45, 45)));
        JLabel icon = new JLabel(isWhite ? "♙" : "♟");
        icon.setFont(new Font("Serif", Font.PLAIN, 22));
        icon.setForeground(isWhite ? Color.WHITE : new Color(160, 160, 160));
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 13));
        nameLbl.setForeground(Color.WHITE);
        bar.add(icon);
        bar.add(nameLbl);
        return bar;
    }

    private JButton sideBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Click Handling ─────────────────────────────────────────────
    private void handleSquareClick(int row, int col) {
        if (chessMatch.getCheckMate()) return;

        ChessPosition clicked = new ChessPosition((char)('a' + col), 8 - row);

        if (selectedPosition == null) {
            ChessPiece p = pieces[row][col];
            if (p != null && p.getColor() == chessMatch.getCurrentPlayer()) {
                selectedPosition = clicked;
                possibleMoves    = chessMatch.possibleMoves(selectedPosition);
                statusLabel.setText("Piece selected — click a highlighted square to move");
                boardPanel.repaint();
            }
            return;
        }

        ChessPiece p = pieces[row][col];
        if (p != null && p.getColor() == chessMatch.getCurrentPlayer()) {
            selectedPosition = clicked;
            possibleMoves    = chessMatch.possibleMoves(selectedPosition);
            statusLabel.setText("Piece reselected — click a highlighted square to move");
            boardPanel.repaint();
            return;
        }

        try {
            chessMatch.performChessMove(selectedPosition, clicked);
            pieces = chessMatch.getPieces();
            selectedPosition = null;
            possibleMoves = null;

                if (chessMatch.getCheckMate()) {
                    chess.Color winnerColor = (chessMatch.getCurrentPlayer() == chess.Color.WHITE) ? chess.Color.BLACK : chess.Color.WHITE;
                    String winner = getPlayerName(winnerColor);
                    statusLabel.setText("Checkmate! " + winner + " wins!");
                    JOptionPane.showMessageDialog(this, winner + " wins by checkmate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (chessMatch.getCheck()) {
                    statusLabel.setText(getPlayerNameForBoard(chessMatch.getCurrentPlayer()) + "'s turn — CHECK!");
                } else {
                    updateStatusText();
                }
            } catch (ChessException ex) {
                selectedPosition = null;
                possibleMoves = null;
                statusLabel.setText("Invalid move — please try again");
            } finally {
                boardPanel.repaint();
            }
        }

    private String getPlayerName(chess.Color color) {
        return color == chess.Color.WHITE ? "White" : "Black";
    }

    private String getPlayerNameForBoard(chess.Color color) {
        return color == chess.Color.WHITE ? "White" : "Black";
    }

    private void updateStatusText() {
        String player = getPlayerNameForBoard(chessMatch.getCurrentPlayer());
        statusLabel.setText(player + " to move");
    }

    // ── Reset ──────────────────────────────────────────────────────
    private void resetGame() {
        chessMatch       = new ChessMatch();
        pieces           = chessMatch.getPieces();
        selectedPosition = null;
        possibleMoves    = null;
        boardPanel.repaint();
    }

    // ── Board Panel ────────────────────────────────────────────────
    private class BoardPanel extends JPanel implements MouseListener {

        private static final long serialVersionUID = 1L;

        BoardPanel() {
            setPreferredSize(new Dimension(8 * SQUARE_SIZE, 8 * SQUARE_SIZE));
            setBackground(DARK_SQ);
            addMouseListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    int x = col * SQUARE_SIZE;
                    int y = row * SQUARE_SIZE;

                    // Square — no border, no gap
                    g2.setColor((row + col) % 2 == 0 ? LIGHT_SQ : DARK_SQ);
                    g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

                    // Selection highlight
                    if (selectedPosition != null) {
                        int sr = 8 - selectedPosition.getRow();
                        int sc = selectedPosition.getColumn() - 'a';
                        if (row == sr && col == sc) {
                            g2.setColor(SEL_COLOR);
                            g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                            g2.setColor(new Color(10, 190, 255, 220));
                            g2.setStroke(new BasicStroke(3));
                            g2.drawRect(x+1, y+1, SQUARE_SIZE-2, SQUARE_SIZE-2);
                            g2.setStroke(new BasicStroke(1));
                        }
                    }

                    // Move hints (chess.com style — circle for empty, highlight for capture)
                    if (possibleMoves != null && possibleMoves[row][col]) {
                        ChessPiece targetPiece = pieces[row][col];
                        if (targetPiece != null) {
                            // Capture highlight
                            g2.setColor(new Color(200, 50, 50, 120));
                            g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                        } else {
                            // Regular move dot
                            int dotSize = 10;
                            int dotX = x + (SQUARE_SIZE - dotSize) / 2;
                            int dotY = y + (SQUARE_SIZE - dotSize) / 2;
                            g2.setColor(HINT_COLOR);
                            g2.fillOval(dotX, dotY, dotSize, dotSize);
                        }
                    }

                    // Piece image
                    ChessPiece piece = pieces[row][col];
                    if (piece != null) {
                        Image img = getImage(piece);
                        if (img != null) {
                            g2.drawImage(img, x, y, SQUARE_SIZE, SQUARE_SIZE, null);
                        }
                    }
                }
            }

            // Coordinates
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            for (int i = 0; i < 8; i++) {
                boolean light = (7 + i) % 2 == 0;
                g2.setColor(light ? DARK_SQ : LIGHT_SQ);
                g2.drawString(String.valueOf((char)('a' + i)),
                    i * SQUARE_SIZE + SQUARE_SIZE - 13,
                    8 * SQUARE_SIZE - 4);
                g2.setColor(i % 2 == 0 ? DARK_SQ : LIGHT_SQ);
                g2.drawString(String.valueOf(8 - i),
                    4, i * SQUARE_SIZE + 14);
            }
        }

        @Override public void mouseClicked(MouseEvent e) {
            int col = e.getX() / SQUARE_SIZE;
            int row = e.getY() / SQUARE_SIZE;
            if (row >= 0 && row < 8 && col >= 0 && col < 8)
                handleSquareClick(row, col);
        }
        @Override public void mousePressed(MouseEvent e)  {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e)  {}
        @Override public void mouseExited(MouseEvent e)   {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
