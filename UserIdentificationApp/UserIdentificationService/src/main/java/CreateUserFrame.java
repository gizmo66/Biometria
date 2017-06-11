import api.user.UserServiceImpl;
import fileChooser.ImagePreview;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CreateUserFrame extends JFrame implements ActionListener {

    private JTextField tfUsername;
    private JLabel lbUsername;
    private JButton btnCreate;
    private JButton btnCancel;
    private String newline = "\n";
    protected JTextArea log;
    protected JFileChooser fc;
    protected File fingerPrintImage;

    public CreateUserFrame() throws HeadlessException {
    }

    public CreateUserFrame(int width, int height) {
        setTitle("Create user");
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

        btnCreate = new JButton("Create");

        btnCreate.addActionListener(e -> {
            UserServiceImpl userService = new UserServiceImpl();
            userService.createUser(tfUsername.getText(), fingerPrintImage);

            UsernameFrame loginDlg = new UsernameFrame(width, height);
            loginDlg.setVisible(true);
            dispose();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JPanel bp = new JPanel();
        bp.add(btnCreate);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fc == null) {
            fc = new JFileChooser("..\\UserIdentificationApp\\FingerPrintsRecognition\\src\\main\\resources");

            fc.setAcceptAllFileFilterUsed(false);
            fc.setAccessory(new ImagePreview(fc));
        }

        int returnVal = fc.showDialog(this,
                                      "Choose finger print image:");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            log.append("Attaching file: " + file.getName()
                       + "." + newline);
            fingerPrintImage = file;
        } else {
            log.append("Attachment cancelled by user." + newline);
        }
        log.setCaretPosition(log.getDocument().getLength());

        //Reset the file chooser for the next time it's shown.
        fc.setSelectedFile(null);
    }
}