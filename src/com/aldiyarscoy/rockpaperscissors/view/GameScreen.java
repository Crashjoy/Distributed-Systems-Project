// view/GameScreen.java
package com.aldiyarscoy.rockpaperscissors.view;

import com.aldiyarscoy.rockpaperscissors.controller.AppController;
import com.aldiyarscoy.rockpaperscissors.model.GameMove;
import com.aldiyarscoy.rockpaperscissors.model.GameSession;
import com.aldiyarscoy.rockpaperscissors.model.GameResult;
import com.aldiyarscoy.rockpaperscissors.utils.GameDataManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GameScreen extends JFrame {
    private AppController controller;
    private GameDataManager dataManager;
    
    // UI Components
    private JLabel titleLabel;
    private JLabel resultLabel;
    private JLabel playerMoveLabel;
    private JLabel opponentMoveLabel;
    private JLabel statsLabel;
    private JButton rockButton;
    private JButton paperButton;
    private JButton scissorsButton;
    private JButton statsButton;
    private JButton networkButton;
    private JPanel gamePanel;
    private JPanel controlPanel;
    
    // Network components
    private boolean networkMode = false;
    private boolean waitingForOpponent = false;
    private String currentPlayerId;
    private JLabel networkStatusLabel;
    private JButton disconnectButton;
    
    public GameScreen(AppController controller) {
        this.controller = controller;
        this.dataManager = controller.getDataManager();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("RockPaperScissors Pro - Your Ultimate Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main container with WCAG compliant colors
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // Title section
        titleLabel = new JLabel("RockPaperScissors Pro", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(45, 45, 65));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Network status panel
        JPanel networkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        networkPanel.setBackground(new Color(240, 240, 240));
        networkStatusLabel = new JLabel("Single Player Mode");
        networkStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        networkStatusLabel.setForeground(new Color(100, 100, 120));
        
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setVisible(false);
        disconnectButton.addActionListener(e -> disconnectFromNetwork());
        
        networkPanel.add(networkStatusLabel);
        networkPanel.add(disconnectButton);
        
        // Game result display
        resultLabel = new JLabel("Make your move!", JLabel.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setForeground(new Color(70, 70, 90));
        
        // Moves display
        playerMoveLabel = new JLabel("Your move: -", JLabel.CENTER);
        opponentMoveLabel = new JLabel("Opponent move: -", JLabel.CENTER);
        
        // Game control panel
        setupGameControls();
        
        // Statistics display
        statsLabel = new JLabel(getStatsText(), JLabel.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsLabel.setForeground(new Color(100, 100, 120));
        
        // Additional controls
        setupControlPanel();
        
        // Layout assembly
        JPanel displayPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        displayPanel.setBackground(new Color(240, 240, 240));
        displayPanel.add(resultLabel);
        displayPanel.add(playerMoveLabel);
        displayPanel.add(opponentMoveLabel);
        displayPanel.add(statsLabel);
        
        // Create a container for the top section
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(networkPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);
        mainPanel.add(gamePanel, BorderLayout.WEST);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupGameControls() {
        gamePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        gamePanel.setBorder(BorderFactory.createTitledBorder("Your Move"));
        gamePanel.setBackground(new Color(220, 220, 240));
        
        // Rock button
        rockButton = createGameButton("Rock", new Color(156, 39, 176));
        rockButton.addActionListener(new MoveListener(GameMove.ROCK));
        
        // Paper button  
        paperButton = createGameButton("Paper", new Color(33, 150, 243));
        paperButton.addActionListener(new MoveListener(GameMove.PAPER));
        
        // Scissors button
        scissorsButton = createGameButton("Scissors", new Color(76, 175, 80));
        scissorsButton.addActionListener(new MoveListener(GameMove.SCISSORS));
        
        gamePanel.add(rockButton);
        gamePanel.add(paperButton);
        gamePanel.add(scissorsButton);
    }
    
    private JButton createGameButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private void setupControlPanel() {
        controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(new Color(240, 240, 240));
        
        statsButton = new JButton("View Statistics");
        statsButton.addActionListener(e -> showStatistics());
        
        networkButton = new JButton("Network Game");
        networkButton.addActionListener(e -> showNetworkOptions());
        
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        
        controlPanel.add(statsButton);
        controlPanel.add(networkButton);
        controlPanel.add(exitButton);
    }
    
    private class MoveListener implements ActionListener {
        private GameMove move;
        
        public MoveListener(GameMove move) {
            this.move = move;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            playGame(move);
        }
    }
    
    private void playGame(GameMove playerMove) {
        if (networkMode && controller.getGameClient() != null && controller.getGameClient().isConnected()) {
            // Network mode
            try {
                controller.getGameClient().sendMove(playerMove);
                setWaitingState(true, "Waiting for opponent...");
                playerMoveLabel.setText("Your move: " + playerMove.getDisplayName());
                opponentMoveLabel.setText("Opponent move: Waiting...");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to send move: " + ex.getMessage(), 
                                            "Network Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Single player mode
            GameMove opponentMove = generateComputerMove();
            GameSession session = new GameSession(playerMove, opponentMove);
            dataManager.saveGameSession(session);
            updateGameDisplay(session);
        }
    }
    
    private GameMove generateComputerMove() {
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0: return GameMove.ROCK;
            case 1: return GameMove.PAPER;
            case 2: return GameMove.SCISSORS;
            default: return GameMove.ROCK;
        }
    }
    
    private void updateGameDisplay(GameSession session) {
        playerMoveLabel.setText("Your move: " + session.getPlayerMove().getDisplayName());
        opponentMoveLabel.setText("Opponent move: " + session.getOpponentMove().getDisplayName());
        
        String resultText;
        Color resultColor;
        
        switch (session.getResult()) {
            case WIN:
                resultText = "You Win!";
                resultColor = new Color(76, 175, 80);
                break;
            case LOSS:
                resultText = "You Lose!";
                resultColor = new Color(244, 67, 54);
                break;
            case DRAW:
                resultText = "It's a Draw!";
                resultColor = new Color(255, 152, 0);
                break;
            default:
                resultText = "Make your move!";
                resultColor = new Color(70, 70, 90);
        }
        
        resultLabel.setText(resultText);
        resultLabel.setForeground(resultColor);
        
        // Update statistics
        statsLabel.setText(getStatsText());
    }
    
    private String getStatsText() {
        GameDataManager.GameStats stats = dataManager.getGameStats();
        return String.format("Games: %d | Wins: %d (%.1f%%)", 
            stats.totalGames, stats.totalWins, stats.getWinPercentage());
    }
    
    private void showStatistics() {
        GameDataManager.GameStats stats = dataManager.getGameStats();
        
        String message = String.format(
            "Game Statistics:\n\n" +
            "Total Games: %d\n" +
            "Wins: %d\n" +
            "Losses: %d\n" +
            "Draws: %d\n" +
            "Win Rate: %.1f%%",
            stats.totalGames, stats.totalWins, stats.totalLosses, 
            stats.totalDraws, stats.getWinPercentage()
        );
        
        JOptionPane.showMessageDialog(this, message, "Game Statistics", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Network methods
    private void setWaitingState(boolean waiting, String status) {
        waitingForOpponent = waiting;
        resultLabel.setText(status);
        
        rockButton.setEnabled(!waiting);
        paperButton.setEnabled(!waiting);
        scissorsButton.setEnabled(!waiting);
    }
    
    private void setNetworkMode(boolean enabled) {
        networkMode = enabled;
        disconnectButton.setVisible(enabled);
        
        if (enabled) {
            networkStatusLabel.setText("Network Mode - Connected");
            setWaitingState(false, "Connected to server - Waiting for game...");
        } else {
            networkStatusLabel.setText("Single Player Mode");
            setWaitingState(false, "Make your move!");
        }
    }
    
    private void disconnectFromNetwork() {
        if (controller.getGameClient() != null) {
            controller.disconnectFromServer();
        }
        if (controller.isServerRunning()) {
            controller.stopGameServer();
        }
        setNetworkMode(false);
    }
    
    public void handleNetworkMessage(String message) {
    SwingUtilities.invokeLater(() -> {
        if (message.startsWith("PLAYER_ID:")) {
            currentPlayerId = message.substring(10);
            networkStatusLabel.setText("Connected as: " + currentPlayerId);
        } else if (message.equals("START_GAME")) {
            networkStatusLabel.setText("Game Started! Waiting for players to move...");
            setWaitingState(false, "Make your move!");
        } else if (message.startsWith("PLAYER_MOVED:")) {
            String movedPlayer = message.substring(13);
            if (!movedPlayer.equals(currentPlayerId)) {
                networkStatusLabel.setText(movedPlayer + " has made their move");
            }
        } else if (message.startsWith("GAME_RESULT:")) {
            String resultData = message.substring(12);
            String[] parts = resultData.split(",");
            if (parts.length == 4) {
                GameMove myMove = GameMove.valueOf(parts[0]);
                GameMove opponentMove = GameMove.valueOf(parts[1]);
                String result = parts[2];
                String opponentId = parts[3];
                
                GameSession session = new GameSession(myMove, opponentMove);
                // Overriding the calculated result with the server's result
                switch (result) {
                    case "WIN": session.setResult(GameResult.WIN); break;
                    case "LOSS": session.setResult(GameResult.LOSS); break;
                    case "DRAW": session.setResult(GameResult.DRAW); break;
                }
                
                dataManager.saveGameSession(session);
                
                // Updating the display and moves
                playerMoveLabel.setText("Your move: " + session.getPlayerMove().getDisplayName());
                opponentMoveLabel.setText("Opponent move: " + session.getOpponentMove().getDisplayName());
                
                // Result text with colors
                String resultText;
                Color resultColor;
                switch (result) {
                    case "WIN":
                        resultText = "You Win!";
                        resultColor = new Color(76, 175, 80); // Green
                        break;
                    case "LOSS":
                        resultText = "You Lose!";
                        resultColor = new Color(244, 67, 54); // Red
                        break;
                    case "DRAW":
                        resultText = "It's a Draw!";
                        resultColor = new Color(255, 152, 0); // Orange
                        break;
                    default:
                        resultText = "Game Over!";
                        resultColor = new Color(70, 70, 90); // Default (black)
                }
                
                resultLabel.setText(resultText);
                resultLabel.setForeground(resultColor);
                
                // Updating statistics
                statsLabel.setText(getStatsText());
                
                networkStatusLabel.setText("Game completed. Opponent: " + opponentId);
                
                // Disabling buttons while showing result
                setWaitingState(true, resultText);
                
                // After 3 seconds, transition to new round
                Timer timer = new Timer(2000, e -> {
                    setWaitingState(false, "New round - Make your move!");
                    resultLabel.setForeground(new Color(70, 70, 90)); // Default color
                    
                    // Reseting move displays for new round
                    playerMoveLabel.setText("Your move: -");
                    opponentMoveLabel.setText("Opponent move: -");
                    
                    networkStatusLabel.setText("New round - Make your move!");
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    });
}
    
    private void showNetworkOptions() {
        JPanel networkPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField hostField = new JTextField("localhost");
        JTextField portField = new JTextField("5000");
        JButton hostGameButton = new JButton("Host Game");
        JButton connectButton = new JButton("Connect to Game");
        
        networkPanel.add(new JLabel("Host:"));
        networkPanel.add(hostField);
        networkPanel.add(new JLabel("Port:"));
        networkPanel.add(portField);
        networkPanel.add(hostGameButton);
        networkPanel.add(connectButton);
        
        hostGameButton.addActionListener(e -> {
            try {
                int port = Integer.parseInt(portField.getText());
                controller.startGameServer(port);
                boolean connected = controller.connectToGameServer("localhost", port);
                if (connected) {
                    setNetworkMode(true);
                    ((JComponent)e.getSource()).getTopLevelAncestor().setVisible(false);
                    JOptionPane.showMessageDialog(this, 
                        "Server started and connected! Waiting for second player...", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to start server", "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid port number", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        connectButton.addActionListener(e -> {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                boolean connected = controller.connectToGameServer(host, port);
                if (connected) {
                    setNetworkMode(true);
                    ((JComponent)e.getSource()).getTopLevelAncestor().setVisible(false);
                    JOptionPane.showMessageDialog(this, 
                        "Connected to game server! Waiting for game to start...", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to connect to server", "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid port number", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JOptionPane.showMessageDialog(this, networkPanel, 
                "Network Game", JOptionPane.PLAIN_MESSAGE);
    }
}