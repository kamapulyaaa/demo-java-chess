package application;

/**
 * GameSettings — stores all customizable game rules.
 * Encapsulation: all fields are private, accessed via getters/setters.
 */
public class GameSettings {

    private String  player1Name;
    private String  player2Name;
    private String  boardTheme;
    private String  playerColor;      // "White" or "Black"

    private boolean castlingEnabled;
    private boolean enPassantEnabled;
    private boolean pawnPromotionEnabled;
    private boolean pawnDoubleStepEnabled;
    private boolean extendedKnightEnabled;
    private boolean kingTwoStepEnabled;

    private boolean botEnabled;
    private String  botDifficulty;    // "Easy", "Medium", "Advanced"

    private boolean timerEnabled;
    private int     timerMinutes;

    public GameSettings() {
        player1Name           = "White Player";
        player2Name           = "Black Player";
        boardTheme            = "Green";
        playerColor           = "White";
        castlingEnabled       = true;
        enPassantEnabled      = true;
        pawnPromotionEnabled  = true;
        pawnDoubleStepEnabled = true;
        extendedKnightEnabled = false;
        kingTwoStepEnabled    = false;
        botEnabled            = false;
        botDifficulty         = "Easy";
        timerEnabled          = false;
        timerMinutes          = 10;
    }

    public String  getPlayer1Name()          { return player1Name; }
    public String  getPlayer2Name()          { return player2Name; }
    public String  getBoardTheme()           { return boardTheme; }
    public String  getPlayerColor()          { return playerColor; }
    public boolean isCastlingEnabled()       { return castlingEnabled; }
    public boolean isEnPassantEnabled()      { return enPassantEnabled; }
    public boolean isPawnPromotionEnabled()  { return pawnPromotionEnabled; }
    public boolean isPawnDoubleStepEnabled() { return pawnDoubleStepEnabled; }
    public boolean isExtendedKnightEnabled() { return extendedKnightEnabled; }
    public boolean isKingTwoStepEnabled()    { return kingTwoStepEnabled; }
    public boolean isBotEnabled()            { return botEnabled; }
    public String  getBotDifficulty()        { return botDifficulty; }
    public boolean isTimerEnabled()          { return timerEnabled; }
    public int     getTimerMinutes()         { return timerMinutes; }

    public void setPlayer1Name(String v)            { player1Name = v; }
    public void setPlayer2Name(String v)            { player2Name = v; }
    public void setBoardTheme(String v)             { boardTheme = v; }
    public void setPlayerColor(String v)            { playerColor = v; }
    public void setCastlingEnabled(boolean v)       { castlingEnabled = v; }
    public void setEnPassantEnabled(boolean v)      { enPassantEnabled = v; }
    public void setPawnPromotionEnabled(boolean v)  { pawnPromotionEnabled = v; }
    public void setPawnDoubleStepEnabled(boolean v) { pawnDoubleStepEnabled = v; }
    public void setExtendedKnightEnabled(boolean v) { extendedKnightEnabled = v; }
    public void setKingTwoStepEnabled(boolean v)    { kingTwoStepEnabled = v; }
    public void setBotEnabled(boolean v)            { botEnabled = v; }
    public void setBotDifficulty(String v)          { botDifficulty = v; }
    public void setTimerEnabled(boolean v)          { timerEnabled = v; }
    public void setTimerMinutes(int v)              { timerMinutes = v; }
}
