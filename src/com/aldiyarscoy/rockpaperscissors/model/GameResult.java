package com.aldiyarscoy.rockpaperscissors.model;

public enum GameResult {
    WIN("Win"),
    LOSS("Loss"), 
    DRAW("Draw");
    
    private final String displayName;
    
    GameResult(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
