package com.aldiyarscoy.rockpaperscissors.model;

public enum GameMove {
    ROCK("Rock"),
    PAPER("Paper"),
    SCISSORS("Scissors");
    
    private final String displayName;
    
    GameMove(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static GameMove getWinnerAgainst(GameMove move) {
        switch (move) {
            case ROCK: return PAPER;
            case PAPER: return SCISSORS;
            case SCISSORS: return ROCK;
            default: return null;
        }
    }
    
    public static GameMove getLoserAgainst(GameMove move) {
        switch (move) {
            case ROCK: return SCISSORS;
            case PAPER: return ROCK;
            case SCISSORS: return PAPER;
            default: return null;
        }
    }
}