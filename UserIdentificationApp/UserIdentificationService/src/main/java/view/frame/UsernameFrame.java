package view.frame;

import api.recognition.FingerPrintsRecognizer;
import api.recognition.VoiceRecognizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class UsernameFrame extends CreateUserFrame implements ActionListener {

    private JTextField tfUsername;
    private JLabel lbUsername;
    private JButton btnLogin;
    private JButton btnCancel;

    public UsernameFrame() {
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
            boolean userIdentifiedSuccessfully = false;
            FingerPrintsRecognizer fingerPrintsRecognizer = new FingerPrintsRecognizer();

            //TODO: do zmiany na plik dźwiękowy wczytany z dysku
            File voiceRecordingFile = null;

            if(fingerPrintImage != null) {
                if(fingerPrintsRecognizer.recognize(getUsername(), fingerPrintImage)) {
                    VoiceRecognizer voiceRecognizer = new VoiceRecognizer();
                    if(voiceRecognizer.recognize(getUsername(), voiceRecordingFile)) {
                        userIdentifiedSuccessfully = true;
                    }
                }
            }

            if(userIdentifiedSuccessfully) {
                JOptionPane.showMessageDialog(UsernameFrame.this, "Hi " + getUsername() + "! You have successfully logged in.", "Login", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(UsernameFrame.this, "Identification failed!", "Login", JOptionPane.ERROR_MESSAGE);
            }
            dispose();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);

        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        JButton sendButton = new JButton("Choose finger print image:");
        sendButton.addActionListener(this);

        panel.add(sendButton);
        panel.add(logScrollPane);

        add(panel, BorderLayout.CENTER);
        add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }
}