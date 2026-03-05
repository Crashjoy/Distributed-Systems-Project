package com.aldiyarscoy.rockpaperscissors.controller;

import com.aldiyarscoy.rockpaperscissors.view.*;
import com.aldiyarscoy.rockpaperscissors.utils.*;

public class AppController {
    private SplashScreen splashScreen;
    private PrivacyScreen privacyScreen;
    private GameScreen gameScreen;
    private GameDataManager dataManager;
    private GameClient gameClient;
    private GameServer gameServer;
    private boolean isServerRunning;
    
    public void initialize() {
        showSplashScreen();
    }
    
    private void showSplashScreen() {
        splashScreen = new SplashScreen();
        splashScreen.showSplash(2000);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                splashScreen.dispose();
                showPrivacyScreen();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showPrivacyScreen() {
        privacyScreen = new PrivacyScreen();
        privacyScreen.setContinueAction(e -> {
            if (privacyScreen.isAccepted()) {
                privacyScreen.dispose();
                initializeApplication();
            }
        });
        privacyScreen.setVisible(true);
    }
    
    private void initializeApplication() {
        dataManager = new GameDataManager();
        gameScreen = new GameScreen(this);
        gameScreen.setVisible(true);
    }
    
    public GameDataManager getDataManager() {
        return dataManager;
    }
    
    // Network methods
    public void startGameServer(int port) {
        try {
            if (gameServer != null) {
                gameServer.stop();
            }
            gameServer = new GameServer(port);
            gameServer.start();
            isServerRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stopGameServer() {
        if (gameServer != null) {
            gameServer.stop();
            isServerRunning = false;
        }
    }
    
    public boolean connectToGameServer(String host, int port) {
        try {
            gameClient = new GameClient();
            boolean connected = gameClient.connect(host, port);
            if (connected) {
                // Start a thread to listen for server messages
                startServerMessageListener();
            }
            return connected;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void disconnectFromServer() {
        if (gameClient != null) {
            gameClient.disconnect();
        }
    }
    
    public GameClient getGameClient() {
        return gameClient;
    }
    
    public boolean isServerRunning() {
        return isServerRunning;
    }
    
    private void startServerMessageListener() {
        new Thread(() -> {
            while (gameClient != null && gameClient.isConnected()) {
                try {
                    Object message = gameClient.getNextMessage();
                    if (message instanceof String) {
                        String textMessage = (String) message;
                        handleServerMessage(textMessage);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    private void handleServerMessage(String message) {
        if (gameScreen != null) {
            gameScreen.handleNetworkMessage(message);
        }
    }
}