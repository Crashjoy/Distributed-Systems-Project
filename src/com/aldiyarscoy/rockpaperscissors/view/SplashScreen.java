package com.aldiyarscoy.rockpaperscissors.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    
    public SplashScreen() {
        createSplash();
    }
    
    private void createSplash() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(45, 45, 65)); 
        
        // App name
        JLabel appName = new JLabel("RockPaperScissors Pro", JLabel.CENTER);
        appName.setFont(new Font("Arial", Font.BOLD, 24));
        appName.setForeground(Color.WHITE);
        appName.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
        
        // Loading bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 150, 136));
        progressBar.setBackground(new Color(30, 30, 50));
        
        content.add(appName, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);
        
        setContentPane(content);
        setSize(400, 300);
        setLocationRelativeTo(null);
    }
    
    public void setProgress(int value) {
        progressBar.setValue(value);
    }
    
    public void showSplash(int duration) {
        setVisible(true);
        
        // Simulating loading process
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    setProgress(i);
                    Thread.sleep(duration / 100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}