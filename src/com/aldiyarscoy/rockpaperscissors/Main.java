package com.aldiyarscoy.rockpaperscissors;

import com.aldiyarscoy.rockpaperscissors.controller.AppController;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            controller.initialize();
        });
    }
}
