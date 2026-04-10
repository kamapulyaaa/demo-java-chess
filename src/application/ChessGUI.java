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
import java.util.Random;

/**
 * ChessGUI — main graphical chess interface.
 * Inheritance: extends JFrame. BoardPanel extends JPanel.
 * Encapsulation: all fields are private.
 * Polymorphism: paintComponent() overridden in BoardPanel.
 * Abstraction: board rendering hidden inside BoardPanel.
 */
public class ChessGUI extends JFrame {

    private static final int   SQUARE_SIZE = 88;
    private static final Color SEL_COLOR   = new Color(247, 247, 105, 210);
    private static final Color HINT_COLOR  = new Color(0, 0, 0, 65);
    private static final Color BG          = new Color(30, 30, 30);
    private static final Color SIDE_BG     = new Color(22, 22, 22);
    private static final Color ROW_EVEN    = new Color(38, 38, 38);
    private static final Color ROW_ODD     = new Color(28, 28, 28);
    private static final Color ROW_LAST    = new Color(50, 70, 50);

    // Board colors — set by theme
    private Color lightSq;
    private Color darkSq;

    // Game state
    private ChessMatch     chessMatch;
    private ChessPiece[][] pieces;
    private boolean[][]    possibleMoves;
    private ChessPosition  selectedPosition;
    private GameSettings   settings;

    // Bot — the color the bot plays
    private chess.Color botColor;
    private chess.Color playerColor;

    // Image cache
    private final Map<String, Image> imageCache = new HashMap<>();

    // Move history
    private final List<String[]> moveHistory      = new ArrayList<>();
    private String               pendingWhiteMove = null;

    // Timer
    private int   whiteSeconds;
    private int   blackSeconds;
    private javax.swing.Timer gameTimer;

    // UI components
    private BoardPanel  boardPanel;
    private JLabel      statusLabel;
    private JLabel      whiteClock;
    private JLabel      blackClock;
    private JLabel      whiteNameLabel;
    private JLabel      blackNameLabel;
    private JPanel      moveListPanel;
    private JScrollPane moveScroll;

    private final Random rng = new Random();

    // ── Constructor ────────────────────────────────────────────────
    public ChessGUI(GameSettings settings) {
        this.settings = settings;
        applyTheme(settings.getBoardTheme());
        loadImages();
        initGame();

        setTitle("Java Chess Game — OOP Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Start timer AFTER UI is visible
        if (settings.isTimerEnabled()) startTimer();

        // If bot plays White (user chose Black), bot moves first
        if (settings.isBotEnabled() && botColor == chess.Color.WHITE) {
            scheduleBotMove();
        }
    }

    // ── Init game state ────────────────────────────────────────────
    private void initGame() {
        chessMatch = new ChessMatch();
        pieces     = chessMatch.getPieces();
        moveHistory.clear();
        pendingWhiteMove = null;

        // Determine colors
        if (settings.isBotEnabled()) {
            if ("Black".equals(settings.getPlayerColor())) {
                playerColor = chess.Color.BLACK;
                botColor    = chess.Color.WHITE;
            } else {
                playerColor = chess.Color.WHITE;
                botColor    = chess.Color.BLACK;
            }
        } else {
            playerColor = chess.Color.WHITE;
            botColor    = null;
        }

        whiteSeconds = settings.getTimerMinutes() * 60;
        blackSeconds = settings.getTimerMinutes() * 60;
    }

    // ── Theme ──────────────────────────────────────────────────────
    private void applyTheme(String theme) {
        switch (theme) {
            case "Blue":
                lightSq = new Color(220, 230, 245);
                darkSq  = new Color(70,  110, 170);
                break;
            case "Brown":
                lightSq = new Color(240, 217, 181);
                darkSq  = new Color(181, 136, 99);
                break;
            case "Classic":
                lightSq = new Color(240, 240, 240);
                darkSq  = new Color(70,  70,  70);
                break;
            default: // Green
                lightSq = new Color(234, 233, 210);
                darkSq  = new Color(119, 153, 84);
                break;
        }
    }

    // ── Load images ────────────────────────────────────────────────
    private void loadImages() {
        String[] keys = {"wK","wQ","wR","wB","wN","wP","bK","bQ","bR","bB","bN","bP"};
        for (String key : keys) {
            try {
                BufferedImage img = ImageIO.read(new File("src/images/" + key + ".png"));
                if (img != null) imageCache.put(key, img);
            } catch (IOException ignored) {}
        }
    }

    private Image getImage(ChessPiece piece) {
        boolean w = piece.getColor() == chess.Color.WHITE;
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
        return imageCache.get((w ? "w" : "b") + type);
    }

    // ── Build UI ───────────────────────────────────────────────────
    private void buildUI() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDE_BG);
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(55, 55, 55)));

        // Determine display names
        String blackName = settings.isBotEnabled() && botColor == chess.Color.BLACK
            ? "AI Bot (" + settings.getBotDifficulty() + ")"
            : (settings.getPlayerColor().equals("Black") ? settings.getPlayer1Name() : settings.getPlayer2Name());

        String whiteName = settings.isBotEnabled() && botColor == chess.Color.WHITE
            ? "AI Bot (" + settings.getBotDifficulty() + ")"
            : (settings.getPlayerColor().equals("White") ? settings.getPlayer1Name() : settings.getPlayer2Name());

        // Black player bar at top
        side.add(buildPlayerBar(blackName, false), BorderLayout.NORTH);

        // Move history
        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setBackground(new Color(18, 18, 18));
        histHeader.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel histLbl = new JLabel("Move History");
        histLbl.setFont(new Font("Arial", Font.BOLD, 12));
        histLbl.setForeground(new Color(160, 160, 160));
        histHeader.add(histLbl, BorderLayout.WEST);

        moveListPanel = new JPanel();
        moveListPanel.setLayout(new BoxLayout(moveListPanel, BoxLayout.Y_AXIS));
        moveListPanel.setBackground(SIDE_BG);

        moveScroll = new JScrollPane(moveListPanel);
        moveScroll.setBackground(SIDE_BG);
        moveScroll.getViewport().setBackground(SIDE_BG);
        moveScroll.setBorder(BorderFactory.createEmptyBorder());
        moveScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(SIDE_BG);
        center.add(histHeader, BorderLayout.NORTH);
        center.add(moveScroll, BorderLayout.CENTER);
        side.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setBackground(new Color(18, 18, 18));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton settingsBtn = sideBtn("Settings", new Color(60, 60, 120));
        JButton newBtn      = sideBtn("New Game", new Color(60, 100, 60));
        settingsBtn.addActionListener(e -> openSettings());
        newBtn.addActionListener(e -> resetGame());
        btnPanel.add(settingsBtn);
        btnPanel.add(newBtn);

        JPanel southSide = new JPanel(new BorderLayout());
        southSide.setBackground(SIDE_BG);
        southSide.add(btnPanel, BorderLayout.NORTH);
        southSide.add(buildPlayerBar(whiteName, true), BorderLayout.SOUTH);
        side.add(southSide, BorderLayout.SOUTH);

        add(side, BorderLayout.EAST);

        // Status bar
        String firstTurn = (botColor == chess.Color.WHITE)
            ? "AI Bot is thinking..."
            : settings.getPlayer1Name() + "'s turn — click a piece to select";

        statusLabel = new JLabel(firstTurn);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(190, 190, 190));
        statusLabel.setBackground(new Color(18, 18, 18));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(7, 16, 8, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildPlayerBar(String name, boolean isWhite) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 18, 18));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(40, 40, 40)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(new Color(18, 18, 18));

        JLabel icon = new JLabel(isWhite ? "♙" : "♟");
        icon.setFont(new Font("Serif", Font.PLAIN, 20));
        icon.setForeground(isWhite ? Color.WHITE : new Color(160, 160, 160));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 13));
        nameLbl.setForeground(Color.WHITE);

        left.add(icon);
        left.add(nameLbl);

        // Clock label
        String clockText = settings.isTimerEnabled()
            ? formatTime(isWhite ? whiteSeconds : blackSeconds) : "--:--";

        JLabel clock = new JLabel(clockText);
        clock.setFont(new Font("Monospaced", Font.BOLD, 14));
        clock.setForeground(new Color(123, 160, 91));
        clock.setBackground(new Color(20, 35, 20));
        clock.setOpaque(true);
        clock.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 90, 60), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        if (isWhite) whiteClock = clock;
        else         blackClock = clock;

        bar.add(left,  BorderLayout.WEST);
        bar.add(clock, BorderLayout.EAST);
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

    // ── Timer ──────────────────────────────────────────────────────
    private void startTimer() {
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new javax.swing.Timer(1000, e -> {
            if (chessMatch.getCheckMate()) { gameTimer.stop(); return; }

            chess.Color current = chessMatch.getCurrentPlayer();
            if (current == chess.Color.WHITE) {
                whiteSeconds--;
                updateClock(whiteClock, whiteSeconds);
                if (whiteSeconds <= 0) { gameTimer.stop(); onTimeout("White"); }
            } else {
                blackSeconds--;
                updateClock(blackClock, blackSeconds);
                if (blackSeconds <= 0) { gameTimer.stop(); onTimeout("Black"); }
            }
        });
        gameTimer.start();
    }

    private void updateClock(JLabel clock, int seconds) {
        clock.setText(formatTime(seconds));
        clock.setForeground(seconds <= 30
            ? new Color(220, 80, 80)
            : new Color(123, 160, 91));
    }

    private void onTimeout(String player) {
        String winner = player.equals("White") ? "Black" : "White";
        statusLabel.setText(player + " ran out of time! " + winner + " wins!");
        JOptionPane.showMessageDialog(this,
            winner + " wins on time!", "Time Up!", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatTime(int secs) {
        int m = Math.max(0, secs) / 60;
        int s = Math.max(0, secs) % 60;
        return String.format("%d:%02d", m, s);
    }

    // ── Click handling ─────────────────────────────────────────────
    private void handleSquareClick(int row, int col) {
        if (chessMatch.getCheckMate()) return;

        // Block input when it's bot's turn
        if (settings.isBotEnabled() && chessMatch.getCurrentPlayer() == botColor) return;

        ChessPosition clicked = new ChessPosition((char)('a' + col), 8 - row);

        if (selectedPosition == null) {
            ChessPiece p = pieces[row][col];
            if (p != null && p.getColor() == chessMatch.getCurrentPlayer()) {
                selectedPosition = clicked;
                possibleMoves    = chessMatch.possibleMoves(selectedPosition);
                statusLabel.setText("Piece selected — click a highlighted square to move");
                boardPanel.repaint(); // ← show yellow + dots immediately
            }
        } else {
            try {
                String from = selectedPosition.toString();
                chessMatch.performChessMove(selectedPosition, clicked);
                pieces           = chessMatch.getPieces();
                selectedPosition = null;
                possibleMoves    = null;
                recordMove(from + "-" + clicked);

                if (chessMatch.getCheckMate()) {
                    handleGameOver();
                } else if (chessMatch.getCheck()) {
                    statusLabel.setText(currentPlayerName() + "'s turn — CHECK!");
                } else {
                    statusLabel.setText(currentPlayerName() + "'s turn — click a piece to select");
                }

                boardPanel.repaint();

                // Trigger bot move
                if (settings.isBotEnabled() && !chessMatch.getCheckMate()) {
                    scheduleBotMove();
                }

            } catch (ChessException ex) {
                selectedPosition = null;
                possibleMoves    = null;
                statusLabel.setText("Invalid move — please try again");
                boardPanel.repaint();
            }
        }
    }

    private String currentPlayerName() {
        chess.Color c = chessMatch.getCurrentPlayer();
        if (settings.isBotEnabled() && c == botColor)
            return "AI Bot";
        return settings.getPlayer1Name();
    }

    private void handleGameOver() {
        chess.Color loser = chessMatch.getCurrentPlayer();
        String winner;
        if (settings.isBotEnabled() && loser == playerColor) {
            winner = "AI Bot";
        } else if (settings.isBotEnabled() && loser == botColor) {
            winner = settings.getPlayer1Name();
        } else {
            winner = loser == chess.Color.WHITE ? "Black" : "White";
        }
        if (gameTimer != null) gameTimer.stop();
        statusLabel.setText("Checkmate! " + winner + " wins!");
        JOptionPane.showMessageDialog(this,
            winner + " wins by checkmate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Bot AI ─────────────────────────────────────────────────────
    private void scheduleBotMove() {
        int delay;
        switch (settings.getBotDifficulty()) {
            case "Medium":   delay = 900;  break;
            case "Advanced": delay = 1400; break;
            default:         delay = 500;  break; // Easy
        }
        statusLabel.setText("AI Bot is thinking...");
        javax.swing.Timer t = new javax.swing.Timer(delay, e -> makeBotMove());
        t.setRepeats(false);
        t.start();
    }

    private void makeBotMove() {
        if (chessMatch.getCheckMate()) return;
        if (chessMatch.getCurrentPlayer() != botColor) return;

        // Collect all bot moves
        List<int[]> allMoves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = pieces[r][c];
                if (p != null && p.getColor() == botColor) {
                    ChessPosition from = new ChessPosition((char)('a' + c), 8 - r);
                    try {
                        boolean[][] moves = chessMatch.possibleMoves(from);
                        for (int tr = 0; tr < 8; tr++)
                            for (int tc = 0; tc < 8; tc++)
                                if (moves[tr][tc])
                                    allMoves.add(new int[]{r, c, tr, tc});
                    } catch (Exception ignored) {}
                }
            }
        }

        if (allMoves.isEmpty()) return;

        int[] chosen = selectBotMove(allMoves);
        if (chosen == null) return;

        try {
            ChessPosition from = new ChessPosition((char)('a' + chosen[1]), 8 - chosen[0]);
            ChessPosition to   = new ChessPosition((char)('a' + chosen[3]), 8 - chosen[2]);
            chessMatch.performChessMove(from, to);
            pieces = chessMatch.getPieces();
            recordMove(from + "-" + to);

            if (chessMatch.getCheckMate()) {
                handleGameOver();
            } else if (chessMatch.getCheck()) {
                statusLabel.setText(settings.getPlayer1Name() + "'s turn — CHECK!");
            } else {
                statusLabel.setText(settings.getPlayer1Name() + "'s turn — click a piece to select");
            }
            boardPanel.repaint();
        } catch (Exception ignored) {}
    }

    private int[] selectBotMove(List<int[]> moves) {
        switch (settings.getBotDifficulty()) {

            case "Advanced": {
                // Try checkmate, then capture highest value, then center control
                int[] best = findCheckmate(moves);
                if (best != null) return best;
                best = findBestCapture(moves);
                if (best != null) return best;
                return findCenterMove(moves);
            }

            case "Medium": {
                // Try capture, otherwise random
                int[] cap = findBestCapture(moves);
                return cap != null ? cap : moves.get(rng.nextInt(moves.size()));
            }

            default: // Easy — fully random
                return moves.get(rng.nextInt(moves.size()));
        }
    }

    private int[] findCheckmate(List<int[]> moves) {
        // Try each move and see if it results in checkmate
        for (int[] m : moves) {
            try {
                ChessPosition from = new ChessPosition((char)('a' + m[1]), 8 - m[0]);
                ChessPosition to   = new ChessPosition((char)('a' + m[3]), 8 - m[2]);
                // Simple check: if target square has the enemy king, it's checkmate
                ChessPiece target = pieces[m[2]][m[3]];
                if (target != null && target.getColor() != botColor
                        && target.getClass().getSimpleName().equals("King")) {
                    return m;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private int[] findBestCapture(List<int[]> moves) {
        int[]  best      = null;
        int    bestValue = 0;
        int[]  pieceValues = {0, 1, 3, 3, 5, 9, 100}; // none,pawn,knight,bishop,rook,queen,king

        for (int[] m : moves) {
            ChessPiece target = pieces[m[2]][m[3]];
            if (target != null && target.getColor() != botColor) {
                int val = pieceValue(target);
                if (val > bestValue) {
                    bestValue = val;
                    best = m;
                }
            }
        }
        return best;
    }

    private int pieceValue(ChessPiece p) {
        switch (p.getClass().getSimpleName()) {
            case "Pawn":   return 1;
            case "Knight": return 3;
            case "Bishop": return 3;
            case "Rook":   return 5;
            case "Queen":  return 9;
            case "King":   return 100;
            default:       return 0;
        }
    }

    private int[] findCenterMove(List<int[]> moves) {
        // Prefer moves toward center squares (3,3), (3,4), (4,3), (4,4)
        int[] best = null;
        int   bestScore = Integer.MAX_VALUE;
        for (int[] m : moves) {
            int dist = Math.abs(m[2] - 3) + Math.abs(m[3] - 3);
            if (dist < bestScore) {
                bestScore = dist;
                best = m;
            }
        }
        return best != null ? best : moves.get(rng.nextInt(moves.size()));
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
            JPanel rp = new JPanel(new GridLayout(1, 3));
            rp.setBackground(isLast ? ROW_LAST : (i % 2 == 0 ? ROW_EVEN : ROW_ODD));
            rp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            rp.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            rp.add(moveCell(String.valueOf(i + 1), new Color(120, 120, 120), false));
            rp.add(moveCell(row[0], Color.WHITE, true));
            rp.add(moveCell(row.length > 1 ? row[1] : "", new Color(180, 180, 180), true));
            moveListPanel.add(rp);
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

    // ── Settings ───────────────────────────────────────────────────
    private void openSettings() {
        if (gameTimer != null) gameTimer.stop();
        SettingsWindow sw = new SettingsWindow(this);
        sw.setVisible(true);
        if (sw.isConfirmed()) {
            settings = sw.getSettings();
            applyTheme(settings.getBoardTheme());
            getContentPane().removeAll();
            setLayout(new BorderLayout());
            getContentPane().setBackground(BG);
            initGame();
            buildUI();
            revalidate();
            repaint();
            pack();
            if (settings.isTimerEnabled()) startTimer();
            if (settings.isBotEnabled() && botColor == chess.Color.WHITE) scheduleBotMove();
        } else {
            if (settings.isTimerEnabled()) startTimer();
        }
    }

    // ── Reset ──────────────────────────────────────────────────────
    private void resetGame() {
        if (gameTimer != null) gameTimer.stop();
        initGame();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        buildUI();
        revalidate();
        repaint();
        pack();
        if (settings.isTimerEnabled()) startTimer();
        if (settings.isBotEnabled() && botColor == chess.Color.WHITE) scheduleBotMove();
    }

    // ── Board Panel ────────────────────────────────────────────────
    private class BoardPanel extends JPanel implements MouseListener {

        BoardPanel() {
            setPreferredSize(new Dimension(8 * SQUARE_SIZE, 8 * SQUARE_SIZE));
            setBackground(darkSq);
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

                    // Square color
                    g2.setColor((row + col) % 2 == 0 ? lightSq : darkSq);
                    g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

                    // Yellow selection highlight
                    if (selectedPosition != null) {
                        int sr = 8 - selectedPosition.getRow();
                        int sc = selectedPosition.getColumn() - 'a';
                        if (row == sr && col == sc) {
                            g2.setColor(new Color(247, 247, 105, 200));
                            g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
                        }
                    }

                    // Move hints — chess.com style
                    if (possibleMoves != null && possibleMoves[row][col]) {
                        ChessPiece target = pieces[row][col];
                        if (target != null) {
                            // Capture square — green overlay on corners
                            g2.setColor(new Color(0, 0, 0, 60));
                            int thickness = 8;
                            int size = SQUARE_SIZE / 4;
                            // Top-left corner
                            g2.fillRect(x, y, size, thickness);
                            g2.fillRect(x, y, thickness, size);
                            // Top-right corner
                            g2.fillRect(x + SQUARE_SIZE - size, y, size, thickness);
                            g2.fillRect(x + SQUARE_SIZE - thickness, y, thickness, size);
                            // Bottom-left corner
                            g2.fillRect(x, y + SQUARE_SIZE - thickness, size, thickness);
                            g2.fillRect(x, y + SQUARE_SIZE - size, thickness, size);
                            // Bottom-right corner
                            g2.fillRect(x + SQUARE_SIZE - size, y + SQUARE_SIZE - thickness, size, thickness);
                            g2.fillRect(x + SQUARE_SIZE - thickness, y + SQUARE_SIZE - size, thickness, size);
                        } else {
                            // Empty square — small dot in center
                            int d = 28;
                            g2.setColor(new Color(0, 0, 0, 80));
                            g2.fillOval(x + SQUARE_SIZE/2 - d/2,
                                        y + SQUARE_SIZE/2 - d/2, d, d);
                        }
                    }

                    // Piece image
                    ChessPiece piece = pieces[row][col];
                    if (piece != null) {
                        Image img = getImage(piece);
                        if (img != null) g2.drawImage(img, x, y, SQUARE_SIZE, SQUARE_SIZE, null);
                    }
                }
            }

            // Coordinates
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            for (int i = 0; i < 8; i++) {
                g2.setColor((7 + i) % 2 == 0 ? darkSq : lightSq);
                g2.drawString(String.valueOf((char)('a' + i)),
                    i * SQUARE_SIZE + SQUARE_SIZE - 13, 8 * SQUARE_SIZE - 4);
                g2.setColor(i % 2 == 0 ? darkSq : lightSq);
                g2.drawString(String.valueOf(8 - i), 4, i * SQUARE_SIZE + 14);
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

    // ── Entry point ────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SettingsWindow sw = new SettingsWindow(null);
            sw.setVisible(true);
            if (sw.isConfirmed()) new ChessGUI(sw.getSettings());
            else System.exit(0);
        });
    }
}
