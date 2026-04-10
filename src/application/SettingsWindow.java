package application;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * SettingsWindow — modern settings dialog.
 * Inheritance: extends JDialog.
 * Encapsulation: settings stored in GameSettings object.
 * Abstraction: hides rule configuration behind clean UI.
 */
public class SettingsWindow extends JDialog {

    private GameSettings settings;
    private boolean      confirmed = false;

    // Colors
    private static final Color BG_DARK  = new Color(15,  15,  26);
    private static final Color BG_MAIN  = new Color(26,  26,  46);
    private static final Color BG_CARD  = new Color(15,  22,  40);
    private static final Color BG_HERO  = new Color(22,  33,  62);
    private static final Color ACCENT   = new Color(123, 160, 91);
    private static final Color TEXT     = new Color(220, 220, 220);
    private static final Color MUTED    = new Color(100, 110, 130);
    private static final Color BORDER   = new Color(42,  42,  74);

    // Player fields
    private JTextField   playerNameField;

    // Color selector
    private JRadioButton whiteColorBtn;
    private JRadioButton blackColorBtn;

    // Theme
    private String        selectedTheme = "Green";
    private ThemeButton[] themeButtons;

    // Bot
    private ToggleSwitch botToggle;
    private JRadioButton easyBtn, mediumBtn, advancedBtn;
    private JPanel       diffPanel;

    // Timer
    private ToggleSwitch timerToggle;
    private JSlider      timerSlider;
    private JLabel       timerValueLabel;
    private JPanel       timerSliderPanel;

    // Standard rules
    private ToggleSwitch castlingToggle;
    private ToggleSwitch enPassantToggle;
    private ToggleSwitch pawnPromotionToggle;

    // Custom rules
    private ToggleSwitch pawnDoubleStepToggle;
    private ToggleSwitch extendedKnightToggle;
    private ToggleSwitch kingTwoStepToggle;

    public SettingsWindow(JFrame parent) {
        super(parent, "Game Settings", true);
        settings = new GameSettings();
        // ⚠️ buildUI called ONCE only — fixes duplication bug
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void buildUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        add(buildHero(),   BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Hero ───────────────────────────────────────────────────────
    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(BG_HERO);
        hero.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(BG_HERO);

        JLabel icon = new JLabel("♟");
        icon.setFont(new Font("Serif", Font.PLAIN, 26));
        icon.setForeground(ACCENT);

        JPanel tb = new JPanel();
        tb.setLayout(new BoxLayout(tb, BoxLayout.Y_AXIS));
        tb.setBackground(BG_HERO);

        JLabel title = new JLabel("Chess Game Settings");
        title.setFont(new Font("Arial", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Customize your rules before playing");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(MUTED);

        tb.add(title);
        tb.add(Box.createVerticalStrut(3));
        tb.add(sub);
        left.add(icon);
        left.add(tb);
        hero.add(left, BorderLayout.WEST);
        return hero;
    }

    // ── Body ───────────────────────────────────────────────────────
    private JScrollPane buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        // 1. Player setup
        body.add(sectionLabel("Player Setup"));
        body.add(vgap(6));
        body.add(buildPlayerCard());
        body.add(vgap(14));

        // 2. Board theme
        body.add(sectionLabel("Board Theme"));
        body.add(vgap(6));
        body.add(buildThemeCard());
        body.add(vgap(14));

        // 3. Bot control
        body.add(sectionLabel("Bot Control"));
        body.add(vgap(6));
        body.add(buildBotCard());
        body.add(vgap(14));

        // 4. Timer
        body.add(sectionLabel("Timer"));
        body.add(vgap(6));
        body.add(buildTimerCard());
        body.add(vgap(14));

        // 5. Standard rules
        body.add(sectionLabel("Standard Rules"));
        body.add(vgap(6));
        castlingToggle      = new ToggleSwitch(true);
        enPassantToggle     = new ToggleSwitch(true);
        pawnPromotionToggle = new ToggleSwitch(true);
        body.add(buildRulesCard(
            new String[][]{
                {"Castling",       "King and Rook swap positions"},
                {"En Passant",     "Special pawn capture move"},
                {"Pawn Promotion", "Pawn becomes a piece on last rank"}
            },
            new ToggleSwitch[]{castlingToggle, enPassantToggle, pawnPromotionToggle}
        ));
        body.add(vgap(14));

        // 6. Custom rules
        body.add(sectionLabel("Custom Rules"));
        body.add(vgap(6));
        pawnDoubleStepToggle  = new ToggleSwitch(true);
        extendedKnightToggle  = new ToggleSwitch(false);
        kingTwoStepToggle     = new ToggleSwitch(false);
        body.add(buildRulesCard(
            new String[][]{
                {"Pawn Double Step",     "Pawn moves 2 squares on first move"},
                {"Extended Knight Move", "Knight moves in a 3+1 L-shape"},
                {"King Two-Step",        "King moves up to 2 squares"}
            },
            new ToggleSwitch[]{pawnDoubleStepToggle, extendedKnightToggle, kingTwoStepToggle}
        ));
        body.add(vgap(14));

        // 7. Advanced editors
        body.add(sectionLabel("Advanced Customization"));
        body.add(vgap(6));
        body.add(buildCustomEditorsCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBackground(BG_MAIN);
        scroll.getViewport().setBackground(BG_MAIN);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(500, 480));
        return scroll;
    }

    // ── Player card ────────────────────────────────────────────────
    private JPanel buildPlayerCard() {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Player name
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameRow.setBackground(BG_CARD);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel nameLbl = new JLabel("Your Name:");
        nameLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        nameLbl.setForeground(TEXT);
        nameLbl.setPreferredSize(new Dimension(90, 30));

        playerNameField = styledField("Enter your name");
        playerNameField.setPreferredSize(new Dimension(280, 32));

        nameRow.add(nameLbl);
        nameRow.add(playerNameField);
        card.add(nameRow);
        card.add(vgap(12));

        // Color selector
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        colorRow.setBackground(BG_CARD);
        colorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel colorLbl = new JLabel("Play as:");
        colorLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        colorLbl.setForeground(TEXT);
        colorLbl.setPreferredSize(new Dimension(90, 30));

        ButtonGroup colorGroup = new ButtonGroup();
        whiteColorBtn = colorRadio("♙  White", true,  new Color(220, 220, 180));
        blackColorBtn = colorRadio("♟  Black", false, new Color(140, 140, 200));
        colorGroup.add(whiteColorBtn);
        colorGroup.add(blackColorBtn);

        colorRow.add(colorLbl);
        colorRow.add(whiteColorBtn);
        colorRow.add(Box.createHorizontalStrut(16));
        colorRow.add(blackColorBtn);
        card.add(colorRow);

        return card;
    }

    private JRadioButton colorRadio(String text, boolean sel, Color color) {
        JRadioButton rb = new JRadioButton(text, sel);
        rb.setFont(new Font("Arial", Font.BOLD, 13));
        rb.setForeground(color);
        rb.setBackground(BG_CARD);
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    // ── Theme card ─────────────────────────────────────────────────
    private JPanel buildThemeCard() {
        JPanel card = card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        String[]  names  = {"Green",  "Blue",   "Brown",  "Classic"};
        Color[][] colors = {
            {new Color(74,  124, 89),  new Color(238, 232, 200)},
            {new Color(70,  112, 170), new Color(208, 223, 245)},
            {new Color(181, 136, 99),  new Color(240, 217, 181)},
            {new Color(60,  60,  60),  new Color(240, 240, 240)}
        };

        themeButtons = new ThemeButton[names.length];
        for (int i = 0; i < names.length; i++) {
            themeButtons[i] = new ThemeButton(names[i], colors[i][0], colors[i][1],
                names[i].equals(selectedTheme));
            final String t = names[i];
            themeButtons[i].addActionListener(e -> selectTheme(t));
            card.add(themeButtons[i]);
        }
        return card;
    }

    private void selectTheme(String theme) {
        selectedTheme = theme;
        for (ThemeButton b : themeButtons) {
            b.setSelected(b.getThemeName().equals(theme));
            b.repaint();
        }
    }

    // ── Bot card ───────────────────────────────────────────────────
    private JPanel buildBotCard() {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Toggle row
        JPanel toggleRow = new JPanel(new BorderLayout(12, 0));
        toggleRow.setBackground(BG_CARD);
        toggleRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        toggleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel tb = new JPanel();
        tb.setLayout(new BoxLayout(tb, BoxLayout.Y_AXIS));
        tb.setBackground(BG_CARD);

        JLabel n = new JLabel("Play against AI Bot");
        n.setFont(new Font("Arial", Font.BOLD, 13));
        n.setForeground(TEXT);

        JLabel d = new JLabel("Bot plays as your opponent");
        d.setFont(new Font("Arial", Font.PLAIN, 11));
        d.setForeground(MUTED);

        tb.add(n);
        tb.add(Box.createVerticalStrut(2));
        tb.add(d);

        botToggle = new ToggleSwitch(false);
        toggleRow.add(tb,        BorderLayout.CENTER);
        toggleRow.add(botToggle, BorderLayout.EAST);
        card.add(toggleRow);

        // Difficulty panel
        diffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        diffPanel.setBackground(new Color(18, 18, 30));
        diffPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)));
        diffPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        diffLbl.setForeground(MUTED);

        ButtonGroup grp = new ButtonGroup();
        easyBtn     = diffRadio("Easy",     true,  new Color(80,  160, 80));
        mediumBtn   = diffRadio("Medium",   false, new Color(200, 140, 40));
        advancedBtn = diffRadio("Advanced", false, new Color(200, 60,  60));
        grp.add(easyBtn);
        grp.add(mediumBtn);
        grp.add(advancedBtn);

        diffPanel.add(diffLbl);
        diffPanel.add(easyBtn);
        diffPanel.add(mediumBtn);
        diffPanel.add(advancedBtn);

        // Start disabled
        setEnabled(diffPanel, false);
        card.add(diffPanel);

        // Listener — enable/disable difficulty when bot toggled
        botToggle.addChangeListener(on -> setEnabled(diffPanel, on));

        return card;
    }

    private JRadioButton diffRadio(String text, boolean sel, Color color) {
        JRadioButton rb = new JRadioButton(text, sel);
        rb.setFont(new Font("Arial", Font.BOLD, 12));
        rb.setForeground(color);
        rb.setBackground(new Color(18, 18, 30));
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    // ── Timer card ─────────────────────────────────────────────────
    private JPanel buildTimerCard() {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Toggle row
        JPanel toggleRow = new JPanel(new BorderLayout(12, 0));
        toggleRow.setBackground(BG_CARD);
        toggleRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        toggleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel tb = new JPanel();
        tb.setLayout(new BoxLayout(tb, BoxLayout.Y_AXIS));
        tb.setBackground(BG_CARD);

        JLabel n = new JLabel("Enable Timer");
        n.setFont(new Font("Arial", Font.BOLD, 13));
        n.setForeground(TEXT);

        JLabel d = new JLabel("Set a time limit per player");
        d.setFont(new Font("Arial", Font.PLAIN, 11));
        d.setForeground(MUTED);

        tb.add(n);
        tb.add(Box.createVerticalStrut(2));
        tb.add(d);

        timerToggle = new ToggleSwitch(false);
        toggleRow.add(tb,          BorderLayout.CENTER);
        toggleRow.add(timerToggle, BorderLayout.EAST);
        card.add(toggleRow);

        // Slider panel
        timerSliderPanel = new JPanel(new BorderLayout(10, 0));
        timerSliderPanel.setBackground(new Color(18, 18, 30));
        timerSliderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        timerSliderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lbl = new JLabel("Time per player:");
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        lbl.setPreferredSize(new Dimension(120, 20));

        timerSlider = new JSlider(1, 60, 10);
        timerSlider.setBackground(new Color(18, 18, 30));
        timerSlider.setMajorTickSpacing(15);
        timerSlider.setMinorTickSpacing(5);
        timerSlider.setPaintTicks(true);

        timerValueLabel = new JLabel("10 min");
        timerValueLabel.setFont(new Font("Arial", Font.BOLD, 13));
        timerValueLabel.setForeground(ACCENT);
        timerValueLabel.setPreferredSize(new Dimension(55, 20));
        timerValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        timerSlider.addChangeListener(e ->
            timerValueLabel.setText(timerSlider.getValue() + " min"));

        timerSliderPanel.add(lbl,            BorderLayout.WEST);
        timerSliderPanel.add(timerSlider,    BorderLayout.CENTER);
        timerSliderPanel.add(timerValueLabel, BorderLayout.EAST);

        // Start disabled
        setEnabled(timerSliderPanel, false);
        card.add(timerSliderPanel);

        // Listener
        timerToggle.addChangeListener(on -> setEnabled(timerSliderPanel, on));

        return card;
    }

    // ── Rules card ─────────────────────────────────────────────────
    private JPanel buildRulesCard(String[][] rules, ToggleSwitch[] toggles) {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        for (int i = 0; i < rules.length; i++) {
            if (i > 0) {
                JPanel sep = new JPanel();
                sep.setBackground(BORDER);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setPreferredSize(new Dimension(100, 1));
                card.add(sep);
            }
            card.add(buildRuleRow(rules[i][0], rules[i][1], toggles[i]));
        }
        return card;
    }

    private JPanel buildRuleRow(String name, String desc, ToggleSwitch toggle) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_CARD);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel tb = new JPanel();
        tb.setLayout(new BoxLayout(tb, BoxLayout.Y_AXIS));
        tb.setBackground(BG_CARD);

        JLabel n = new JLabel(name);
        n.setFont(new Font("Arial", Font.BOLD, 13));
        n.setForeground(TEXT);

        JLabel d = new JLabel(desc);
        d.setFont(new Font("Arial", Font.PLAIN, 11));
        d.setForeground(MUTED);

        tb.add(n);
        tb.add(Box.createVerticalStrut(2));
        tb.add(d);

        row.add(tb,     BorderLayout.CENTER);
        row.add(toggle, BorderLayout.EAST);
        return row;
    }

    // ── Custom editors card ────────────────────────────────────────
    private JPanel buildCustomEditorsCard() {
        JPanel card = card();
        card.setLayout(new GridLayout(1, 2, 10, 0));

        JButton posBtn  = editorBtn("Starting Positions", "Design your own board layout",  new Color(50, 80, 130));
        JButton moveBtn = editorBtn("Piece Movements",    "Define how pieces move",         new Color(80, 50, 130));

        posBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Starting Position Editor\n\nDrag and drop pieces to create a custom starting layout.",
                "Starting Positions", JOptionPane.INFORMATION_MESSAGE));

        moveBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Piece Movement Editor\n\nDefine custom movement rules for each piece.",
                "Piece Movements", JOptionPane.INFORMATION_MESSAGE));

        card.add(posBtn);
        card.add(moveBtn);
        return card;
    }

    private JButton editorBtn(String title, String desc, Color bg) {
        JButton btn = new JButton("<html><center><b>" + title + "</b><br>" +
            "<span style='font-size:10px;color:#aaa'>" + desc + "</span></center></html>");
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 54));
        return btn;
    }

    // ── Footer ─────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        footer.setBackground(BG_DARK);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton cancelBtn = new JButton("Cancel / Reset");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        cancelBtn.setBackground(new Color(60, 30, 30));
        cancelBtn.setForeground(new Color(200, 140, 140));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setPreferredSize(new Dimension(150, 38));
        cancelBtn.addActionListener(e -> resetDefaults());

        JButton applyBtn = new JButton("Apply and Start");
        applyBtn.setFont(new Font("Arial", Font.BOLD, 14));
        applyBtn.setBackground(ACCENT);
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorderPainted(false);
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBtn.setPreferredSize(new Dimension(160, 38));
        applyBtn.addActionListener(e -> applyAndStart());

        footer.add(cancelBtn);
        footer.add(applyBtn);
        return footer;
    }

    // ── Reset ──────────────────────────────────────────────────────
    private void resetDefaults() {
        playerNameField.setText("Enter your name");
        whiteColorBtn.setSelected(true);
        selectTheme("Green");
        botToggle.setOn(false);
        setEnabled(diffPanel, false);
        easyBtn.setSelected(true);
        timerToggle.setOn(false);
        setEnabled(timerSliderPanel, false);
        timerSlider.setValue(10);
        timerValueLabel.setText("10 min");
        castlingToggle.setOn(true);
        enPassantToggle.setOn(true);
        pawnPromotionToggle.setOn(true);
        pawnDoubleStepToggle.setOn(true);
        extendedKnightToggle.setOn(false);
        kingTwoStepToggle.setOn(false);
    }

    // ── Apply and start ────────────────────────────────────────────
    private void applyAndStart() {
        String name = playerNameField.getText().trim();
        boolean playAsWhite = whiteColorBtn.isSelected();

        settings.setPlayerColor(playAsWhite ? "White" : "Black");
        settings.setPlayer1Name(name.isEmpty() ? (playAsWhite ? "White Player" : "Black Player") : name);
        settings.setPlayer2Name(botToggle.isOn()
            ? "AI Bot (" + getBotDiff() + ")"
            : (playAsWhite ? "Black Player" : "White Player"));

        settings.setBoardTheme(selectedTheme);
        settings.setBotEnabled(botToggle.isOn());
        settings.setBotDifficulty(getBotDiff());
        settings.setTimerEnabled(timerToggle.isOn());
        settings.setTimerMinutes(timerSlider.getValue());
        settings.setCastlingEnabled(castlingToggle.isOn());
        settings.setEnPassantEnabled(enPassantToggle.isOn());
        settings.setPawnPromotionEnabled(pawnPromotionToggle.isOn());
        settings.setPawnDoubleStepEnabled(pawnDoubleStepToggle.isOn());
        settings.setExtendedKnightEnabled(extendedKnightToggle.isOn());
        settings.setKingTwoStepEnabled(kingTwoStepToggle.isOn());

        confirmed = true;
        dispose();
    }

    private String getBotDiff() {
        if (advancedBtn.isSelected()) return "Advanced";
        if (mediumBtn.isSelected())   return "Medium";
        return "Easy";
    }

    // ── Helpers ────────────────────────────────────────────────────
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Arial", Font.BOLD, 10));
        l.setForeground(ACCENT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBackground(new Color(26, 26, 46));
        f.setForeground(TEXT);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private void setEnabled(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component c : panel.getComponents()) c.setEnabled(enabled);
    }

    public GameSettings getSettings() { return settings; }
    public boolean      isConfirmed() { return confirmed; }

    // ══════════════════════════════════════════════════════════════
    // ToggleSwitch — custom toggle component
    // ══════════════════════════════════════════════════════════════
    static class ToggleSwitch extends JComponent {
        private boolean on;
        private java.util.function.Consumer<Boolean> changeListener;

        ToggleSwitch(boolean on) {
            this.on = on;
            setPreferredSize(new Dimension(46, 26));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    ToggleSwitch.this.on = !ToggleSwitch.this.on;
                    repaint();
                    if (changeListener != null)
                        changeListener.accept(ToggleSwitch.this.on);
                }
            });
        }

        public boolean isOn()           { return on; }
        public void    setOn(boolean v)  { this.on = v; repaint(); }
        public void    addChangeListener(java.util.function.Consumer<Boolean> l) {
            this.changeListener = l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(on ? new Color(123, 160, 91) : new Color(55, 55, 75));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            g2.setColor(Color.WHITE);
            int ks = h - 6;
            g2.fillOval(on ? w - ks - 3 : 3, 3, ks, ks);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ThemeButton — custom board theme preview button
    // ══════════════════════════════════════════════════════════════
    static class ThemeButton extends JButton {
        private final String themeName;
        private final Color  dark, light;
        private boolean      selected;

        ThemeButton(String name, Color dark, Color light, boolean selected) {
            this.themeName = name;
            this.dark = dark;
            this.light = light;
            this.selected = selected;
            setPreferredSize(new Dimension(96, 56));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public String  getThemeName()         { return themeName; }
        public void    setSelected(boolean s)  { this.selected = s; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int sq = (h - 10) / 2;
            int sx = (w - sq * 2) / 2, sy = 4;
            for (int r = 0; r < 2; r++)
                for (int c = 0; c < 2; c++) {
                    g2.setColor((r + c) % 2 == 0 ? light : dark);
                    g2.fillRoundRect(sx + c*sq, sy + r*sq, sq, sq, 3, 3);
                }
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.setColor(new Color(200, 200, 200));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(themeName, (w - fm.stringWidth(themeName)) / 2, h - 4);
            g2.setColor(selected ? new Color(123, 160, 91) : new Color(55, 55, 75));
            g2.setStroke(new BasicStroke(selected ? 2.5f : 1f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 10, 10);
        }
    }
}
