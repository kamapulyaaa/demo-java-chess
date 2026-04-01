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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessGUI extends JFrame {

    private static final int   SQUARE_SIZE = 88;
    private static final Color LIGHT_SQ    = new Color(234, 233, 210);
    private static final Color DARK_SQ     = new Color(119, 153, 84);
    private static final Color SEL_COLOR   = new Color(247, 247, 105, 210);
    private static final Color HINT_COLOR  = new Color(0, 0, 0, 60);
    private static final Color BG          = new Color(30, 30, 30);
    private static final Color SIDE_BG     = new Color(22, 22, 22);
    private static final Color ROW_EVEN    = new Color(38, 38, 38);
    private static final Color ROW_ODD     = new Color(28, 28, 28);
    private static final Color ROW_LAST    = new Color(50, 70, 50);

    private ChessMatch     chessMatch;
    private ChessPiece[][] pieces;
    private boolean[][]    possibleMoves;
    private ChessPosition  selectedPosition;

    private final List<String[]>     moveHistory      = new ArrayList<>();
    private final Map<String, Image> imageCache       = new HashMap<>();
    private String                   pendingWhiteMove = null;

    private BoardPanel  boardPanel;
    private JLabel      statusLabel;
    private JPanel      moveListPanel;
    private JScrollPane moveScroll;

    public ChessGUI() {
        chessMatch = new ChessMatch();
        pieces     = chessMatch.getPieces();
        loadImages();

        setTitle("Java Chess Game — OOP Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

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

    // ── Build UI ───────────────────────────────────────────────────
    private void buildUI() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Side panel
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDE_BG);
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(55, 55, 55)));

        side.add(playerBar("Black Player", false), BorderLayout.NORTH);

        // Move history
        JPanel historyHeader = new JPanel(new BorderLayout());
        historyHeader.setBackground(new Color(18, 18, 18));
        historyHeader.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel histLbl = new JLabel("Move History");
        histLbl.setFont(new Font("Arial", Font.BOLD, 12));
        histLbl.setForeground(new Color(160, 160, 160));
        historyHeader.add(histLbl, BorderLayout.WEST);

        moveListPanel = new JPanel();
        moveListPanel.setLayout(new BoxLayout(moveListPanel, BoxLayout.Y_AXIS));
        moveListPanel.setBackground(SIDE_BG);

        moveScroll = new JScrollPane(moveListPanel);
        moveScroll.setBackground(SIDE_BG);
        moveScroll.getViewport().setBackground(SIDE_BG);
        moveScroll.setBorder(BorderFactory.createEmptyBorder());
        moveScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(SIDE_BG);
        centerPanel.add(historyHeader, BorderLayout.NORTH);
        centerPanel.add(moveScroll, BorderLayout.CENTER);
        side.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setBackground(new Color(18, 18, 18));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton resignBtn = sideBtn("Resign",   new Color(130, 50, 50));
        JButton newBtn    = sideBtn("New Game", new Color(60, 100, 60));
        resignBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "You resigned!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        });
        newBtn.addActionListener(e -> resetGame());
        btnPanel.add(resignBtn);
        btnPanel.add(newBtn);

        JPanel southSide = new JPanel(new BorderLayout());
        southSide.setBackground(SIDE_BG);
        southSide.add(btnPanel, BorderLayout.NORTH);
        southSide.add(playerBar("White Player", true), BorderLayout.SOUTH);
        side.add(southSide, BorderLayout.SOUTH);

        add(side, BorderLayout.EAST);

        // Status bar
        statusLabel = new JLabel("White's turn — click a piece to select");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(190, 190, 190));
        statusLabel.setBackground(new Color(18, 18, 18));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(7, 16, 8, 10));
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

    // ── Move history ───────────────────────────────────────────────
    private void recordMove(String move) {
        if (chessMatch.getCurrentPlayer() == chess.Color.BLACK) {
            pendingWhiteMove = move;
        } else {
            String w = pendingWhiteMove != null ? pendingWhiteMove : "";
            moveHistory.add(new String[]{w, move});
            pendingWhiteMove = null;
            rebuildMoveList();
        }
        if (pendingWhiteMove != null && chessMatch.getCheckMate()) {
            moveHistory.add(new String[]{pendingWhiteMove, ""});
            pendingWhiteMove = null;
            rebuildMoveList();
        }
    }

    private void rebuildMoveList() {
        moveListPanel.removeAll();
        for (int i = 0; i < moveHistory.size(); i++) {
            String[] row    = moveHistory.get(i);
            boolean  isLast = (i == moveHistory.size() - 1);
            JPanel rowPanel = new JPanel(new GridLayout(1, 3));
            rowPanel.setBackground(isLast ? ROW_LAST : (i % 2 == 0 ? ROW_EVEN : ROW_ODD));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            rowPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            rowPanel.add(moveCell(String.valueOf(i + 1), new Color(120, 120, 120), false));
            rowPanel.add(moveCell(row[0], Color.WHITE, true));
            rowPanel.add(moveCell(row.length > 1 ? row[1] : "", new Color(180, 180, 180), true));
            moveListPanel.add(rowPanel);
        }
        moveListPanel.revalidate();
        moveListPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = moveScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JLabel moveCell(String text, Color fg, boolean bold) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Monospaced", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(fg);
        return l;
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
            }
        } else {
            try {
                String fromNotation = selectedPosition.toString();
                chessMatch.performChessMove(selectedPosition, clicked);
                String moveNotation = fromNotation + "-" + clicked.toString();
                pieces           = chessMatch.getPieces();
                selectedPosition = null;
                possibleMoves    = null;
                recordMove(moveNotation);

                if (chessMatch.getCheckMate()) {
                    String winner = chessMatch.getCurrentPlayer() == chess.Color.WHITE ? "Black" : "White";
                    statusLabel.setText("Checkmate! " + winner + " wins!");
                    JOptionPane.showMessageDialog(this, winner + " wins by checkmate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                } else if (chessMatch.getCheck()) {
                    statusLabel.setText(chessMatch.getCurrentPlayer() + "'s turn — CHECK!");
                } else {
                    statusLabel.setText(chessMatch.getCurrentPlayer() + "'s turn — click a piece to select");
                }
            } catch (ChessException ex) {
                selectedPosition = null;
                possibleMoves    = null;
                statusLabel.setText("Invalid move — please try again");
            }
        }
        boardPanel.repaint();
    }

    // ── Reset ──────────────────────────────────────────────────────
    private void resetGame() {
        chessMatch       = new ChessMatch();
        pieces           = chessMatch.getPieces();
        selectedPosition = null;
        possibleMoves    = null;
        moveHistory.clear();
        pendingWhiteMove = null;
        moveListPanel.removeAll();
        moveListPanel.revalidate();
        moveListPanel.repaint();
        statusLabel.setText("White's turn — click a piece to select");
        boardPanel.repaint();
    }

    // ── Board Panel ────────────────────────────────────────────────
    private class BoardPanel extends JPanel implements MouseListener {

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
                        }
                    }

                    // Move hints
                    if (possibleMoves != null && possibleMoves[row][col]) {
                        g2.setColor(HINT_COLOR);
                        int d = 26;
                        g2.fillOval(x + SQUARE_SIZE/2 - d/2, y + SQUARE_SIZE/2 - d/2, d, d);
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
