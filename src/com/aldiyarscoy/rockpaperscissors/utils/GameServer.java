package com.aldiyarscoy.rockpaperscissors.utils;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

import com.aldiyarscoy.rockpaperscissors.model.GameMove;

public class GameServer {
    private ServerSocket serverSocket;
    private ExecutorService clientHandlerPool;
    private boolean isRunning;
    private List<ClientHandler> connectedPlayers;
    private Map<String, GameMove> playerMoves;
    private CountDownLatch moveLatch;
    
    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientHandlerPool = Executors.newFixedThreadPool(2);
        connectedPlayers = new CopyOnWriteArrayList<>();
        playerMoves = new ConcurrentHashMap<>();
        isRunning = true;
        moveLatch = new CountDownLatch(2);
    }
    
    public void start() {
        System.out.println("Game server started on port: " + serverSocket.getLocalPort());
        
        new Thread(() -> {
            while (isRunning && connectedPlayers.size() < 2) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String playerId = "Player" + (connectedPlayers.size() + 1);
                    ClientHandler handler = new ClientHandler(clientSocket, playerId);
                    connectedPlayers.add(handler);
                    clientHandlerPool.execute(handler);
                    
                    System.out.println(playerId + " connected. Total players: " + connectedPlayers.size());
                    
                    // If 2 players, start the game
                    if (connectedPlayers.size() == 2) {
                        broadcastToAll("START_GAME");
                        System.out.println("Game started with 2 players!");
                    }
                    
                } catch (IOException e) {
                    if (isRunning) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlerPool.shutdown();
    }
    
    private synchronized void processMove(String playerId, GameMove move) {
        playerMoves.put(playerId, move);
        broadcastToAll("PLAYER_MOVED:" + playerId);
        
        System.out.println(playerId + " chose: " + move);
        
        if (playerMoves.size() == 2) {
            // Both players have moved, calculating results
            calculateAndSendResults();
        }
    }
    
    private void calculateAndSendResults() {
        try {
            List<String> playerIds = new ArrayList<>(playerMoves.keySet());
            String player1Id = playerIds.get(0);
            String player2Id = playerIds.get(1);
            
            GameMove move1 = playerMoves.get(player1Id);
            GameMove move2 = playerMoves.get(player2Id);
            
            // Calculating results
            String result1 = calculateResult(move1, move2, player1Id, player2Id);
            String result2 = calculateResult(move2, move1, player2Id, player1Id);
            
            // Sending results
            sendToPlayer(player1Id, "GAME_RESULT:" + result1);
            sendToPlayer(player2Id, "GAME_RESULT:" + result2);
            
            // Reset for next round
            playerMoves.clear();
            moveLatch = new CountDownLatch(2);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String calculateResult(GameMove myMove, GameMove opponentMove, String myId, String opponentId) {
        String result;
        if (myMove == opponentMove) {
            result = "DRAW";
        } else if (myMove == GameMove.getWinnerAgainst(opponentMove)) {
            result = "WIN";
        } else {
            result = "LOSS";
        }
        
        return String.format("%s,%s,%s,%s", myMove, opponentMove, result, opponentId);
    }
    
    private void broadcastToAll(String message) {
        for (ClientHandler handler : connectedPlayers) {
            handler.sendMessage(message);
        }
    }
    
    private void sendToPlayer(String playerId, String message) {
        for (ClientHandler handler : connectedPlayers) {
            if (handler.getPlayerId().equals(playerId)) {
                handler.sendMessage(message);
                break;
            }
        }
    }
    
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        private String playerId;
        private boolean connected;
        
        public ClientHandler(Socket socket, String playerId) {
            this.clientSocket = socket;
            this.playerId = playerId;
            this.connected = true;
        }
        
        public String getPlayerId() {
            return playerId;
        }
        
        @Override
        public void run() {
            try {
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                
                // Sending player ID to client
                sendMessage("PLAYER_ID:" + playerId);
                
                while (connected) {
                    try {
                        Object message = inputStream.readObject();
                        if (message instanceof GameMove) {
                            processMove(playerId, (GameMove) message);
                        } else if (message instanceof String) {
                            String textMessage = (String) message;
                            if ("DISCONNECT".equals(textMessage)) {
                                break;
                            }
                        }
                    } catch (EOFException | SocketException e) {
                        break;
                    }
                }
                
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
        
        public void sendMessage(String message) {
            try {
                if (connected && outputStream != null) {
                    outputStream.writeObject(message);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
            }
        }
        
        private void disconnect() {
            connected = false;
            connectedPlayers.remove(this);
            playerMoves.remove(playerId);
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(playerId + " disconnected");
        }
    }
}