package com.aldiyarscoy.rockpaperscissors.model;

import java.time.LocalDateTime;

public class GameSession {
    private GameMove playerMove;
    private GameMove opponentMove;
    private GameResult result;
    private LocalDateTime timestamp;
    
    public GameSession(GameMove playerMove, GameMove opponentMove) {
        this.playerMove = playerMove;
        this.opponentMove = opponentMove;
        this.timestamp = LocalDateTime.now();
        calculateResult();
    }
    
    private void calculateResult() {
        if (playerMove == opponentMove) {
            result = GameResult.DRAW;
        } else if (playerMove == GameMove.getWinnerAgainst(opponentMove)) {
            result = GameResult.WIN;
        } else {
            result = GameResult.LOSS;
        }
    }
    
    // Method to set result when loading from file
    public void setResult(GameResult result) {
        this.result = result;
    }
    
    // Getters
    public GameMove getPlayerMove() { return playerMove; }
    public GameMove getOpponentMove() { return opponentMove; }
    public GameResult getResult() { return result; }
    public LocalDateTime getTimestamp() { return timestamp; }
}