package com.aldiyarscoy.rockpaperscissors.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PrivacyScreen extends JFrame {
    private JCheckBox acceptCheckbox;
    private JButton continueButton;
    
    public PrivacyScreen() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Privacy Policy - RockPaperScissors Pro");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Privacy Policy", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(45, 45, 65));
        
        // Privacy policy text area
        JTextArea policyText = new JTextArea(generatePrivacyPolicy());
        policyText.setEditable(false);
        policyText.setLineWrap(true);
        policyText.setWrapStyleWord(true);
        policyText.setBackground(new Color(250, 250, 250));
        policyText.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(policyText);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Acceptance section
        JPanel acceptancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acceptancePanel.setBackground(Color.WHITE);
        
        acceptCheckbox = new JCheckBox("I have read and accept the privacy policy");
        acceptCheckbox.setBackground(Color.WHITE);
        
        acceptCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateContinueButton();
            }
        });
        
        continueButton = new JButton("Continue");
        continueButton.setEnabled(false);
        continueButton.setBackground(new Color(0, 150, 136));
        continueButton.setForeground(Color.WHITE);
        
        JButton declineButton = new JButton("Decline & Exit");
        declineButton.addActionListener(e -> System.exit(0));
        
        acceptancePanel.add(acceptCheckbox);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(continueButton);
        buttonPanel.add(declineButton);
        
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.add(acceptancePanel, BorderLayout.NORTH);
        bottomContainer.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }
    
    private String generatePrivacyPolicy() {
        return "RockPaperScissors Pro Privacy Policy\n\n" +
               "Last Updated: " + java.time.LocalDate.now() + "\n\n" +
               "1. Information We Collect\n" +
               "We collect game statistics including wins, losses, and game choices to improve your gaming experience.\n\n" +
               "2. How We Use Your Information\n" +
               "Game data is stored locally on your device and used solely for providing game statistics and improving gameplay.\n\n" +
               "3. Data Storage\n" +
               "All data is stored locally and is not transmitted to external servers without your explicit consent.\n\n" +
               "4. Your Rights\n" +
               "You can request deletion of your game data at any time through the app settings.\n\n" +
               "5. Consent\n" +
               "By checking the box below, you consent to our privacy policy and terms of service.";
    }
    
    private void updateContinueButton() {
        continueButton.setEnabled(acceptCheckbox.isSelected());
    }
    
    public void setContinueAction(ActionListener listener) {
        continueButton.addActionListener(listener);
    }
    
    public boolean isAccepted() {
        return acceptCheckbox.isSelected();
    }
}