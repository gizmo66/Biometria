import javax.swing.*;

/**
 * @author Adam
 */
public class FingerPrintRecognitionDialog extends JFrame {

    private static final String WINDOW_TITLE = "Input Image";

    public FingerPrintRecognitionDialog(ImageIcon icon, int x, int y) {
        super(WINDOW_TITLE);
        JLabel label = new JLabel(icon);
        this.add(label);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setLocation(x, y);
    }
}
