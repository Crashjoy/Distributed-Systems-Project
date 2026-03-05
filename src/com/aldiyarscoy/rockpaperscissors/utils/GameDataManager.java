package com.aldiyarscoy.rockpaperscissors.utils;

import com.aldiyarscoy.rockpaperscissors.model.GameSession;
import com.aldiyarscoy.rockpaperscissors.model.GameMove;
import com.aldiyarscoy.rockpaperscissors.model.GameResult;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameDataManager {
    private static final String DATA_FILE = "game_history.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private List<GameSession> gameHistory;
    
    public GameDataManager() {
        gameHistory = new ArrayList<>();
        
        // Deleting the existing data file to reset stats
        File file = new File(DATA_FILE);
        if (file.exists()) {
            file.delete();
            System.out.println("Previous game data cleared.");
        }    
    }
    
    public void saveGameSession(GameSession session) {
        gameHistory.add(session);
        saveGameData();
    }
    
    public List<GameSession> getGameHistory() {
        return new ArrayList<>(gameHistory);
    }
    
    public GameStats getGameStats() {
        long wins = gameHistory.stream().filter(s -> s.getResult() == GameResult.WIN).count();
        long losses = gameHistory.stream().filter(s -> s.getResult() == GameResult.LOSS).count();
        long draws = gameHistory.stream().filter(s -> s.getResult() == GameResult.DRAW).count();
        
        return new GameStats(wins, losses, draws, gameHistory.size());
    }
    
    private void loadGameData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                GameSession session = parseGameSession(line);
                if (session != null) {
                    gameHistory.add(session);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading game data: " + e.getMessage());
        }
    }
    
    private void saveGameData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (GameSession session : gameHistory) {
                writer.write(serializeGameSession(session));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving game data: " + e.getMessage());
        }
    }
    
    private String serializeGameSession(GameSession session) {
        return session.getTimestamp().format(formatter) + "," +
               session.getPlayerMove() + "," +
               session.getOpponentMove() + "," +
               session.getResult();
    }
    
    private GameSession parseGameSession(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length == 4) {
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], formatter);
                GameMove playerMove = GameMove.valueOf(parts[1]);
                GameMove opponentMove = GameMove.valueOf(parts[2]);
                GameResult result = GameResult.valueOf(parts[3]);
                
                // Creating session and setting the result
                GameSession session = new GameSession(playerMove, opponentMove);
                session.setResult(result);
                return session;
            }
        } catch (Exception e) {
            System.err.println("Error parsing game session: " + e.getMessage());
        }
        return null;
    }
    
    public static class GameStats {
        public final long totalWins;
        public final long totalLosses;
        public final long totalDraws;
        public final long totalGames;
        
        public GameStats(long wins, long losses, long draws, long totalGames) {
            this.totalWins = wins;
            this.totalLosses = losses;
            this.totalDraws = draws;
            this.totalGames = totalGames;
        }
        
        public double getWinPercentage() {
            return totalGames > 0 ? (double) totalWins / totalGames * 100 : 0;
        }
    }
}