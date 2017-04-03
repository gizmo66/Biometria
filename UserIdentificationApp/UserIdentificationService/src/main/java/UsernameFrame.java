import javax.swing.*;
import java.awt.*;

public class UsernameFrame extends JFrame {

    private JTextField tfUsername;
    private JLabel lbUsername;
    private JButton btnLogin;
    private JButton btnCancel;

    public UsernameFrame(int width, int height) {
        setTitle("Login");
        setLocation(560, 340);

        final JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        btnLogin = new JButton("Login");

        btnLogin.addActionListener(e -> {
            dispose();
            FingerPrintsRecognizer fingerPrintsRecognizer = new FingerPrintsRecognizer();
            boolean fingerPrintsMatched = fingerPrintsRecognizer.recognize(getUsername());
            if (fingerPrintsMatched) {
                VoiceRecognizer voiceRecognizer = new VoiceRecognizer();
                if (voiceRecognizer.recognize(getUsername())) {
                    JOptionPane.showMessageDialog(UsernameFrame.this, "Hi " + getUsername() + "! You have successfully logged in.", "Login", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(UsernameFrame.this, "Identification failed!", "Login", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }
}