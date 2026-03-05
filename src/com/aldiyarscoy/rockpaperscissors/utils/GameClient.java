package com.aldiyarscoy.rockpaperscissors.utils;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.aldiyarscoy.rockpaperscissors.model.GameMove;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private BlockingQueue<Object> messageQueue;
    private boolean connected;
    private String playerId;
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            messageQueue = new LinkedBlockingQueue<>();
            connected = true;
            
            startMessageListener();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            while (connected) {
                try {
                    Object message = inputStream.readObject();
                    if (message instanceof String) {
                        String textMessage = (String) message;
                        if (textMessage.startsWith("PLAYER_ID:")) {
                            playerId = textMessage.substring(10);
                        }
                    }
                    messageQueue.put(message);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    if (connected) {
                        e.printStackTrace();
                    }
                    connected = false;
                }
            }
        }).start();
    }
    
    public void sendMove(GameMove move) throws IOException {
        if (connected) {
            outputStream.writeObject(move);
            outputStream.flush();
        }
    }
    
    public void sendMessage(String message) throws IOException {
        if (connected) {
            outputStream.writeObject(message);
            outputStream.flush();
        }
    }
    
    public Object getNextMessage() throws InterruptedException {
        return messageQueue.take();
    }
    
    public boolean hasMessage() {
        return !messageQueue.isEmpty();
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void disconnect() {
        connected = false;
        try {
            sendMessage("DISCONNECT");
        } catch (IOException e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}