package api;

import javax.swing.*;

/**
 * @author Adam
 */
public class FingerPrintRecognitionDialog extends JFrame {

    public FingerPrintRecognitionDialog(ImageIcon icon, int x, int y, String windowName) {
        super(windowName);
        JLabel label = new JLabel(icon);
        this.add(label);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setLocation(x, y);
    }
}
