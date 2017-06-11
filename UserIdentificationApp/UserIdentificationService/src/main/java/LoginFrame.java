import javax.swing.*;
import java.awt.*;

/**
 * @author Adam
 */
public class LoginFrame extends JFrame {

    public LoginFrame(int width, int height, boolean atLeastOneUserInDatabase) {
        setTitle("Login");

        final JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        if (!atLeastOneUserInDatabase) {
            JLabel lbUsername = new JLabel("No users in database, please create new user. ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lbUsername, cs);
        }
        getContentPane().add(panel, BorderLayout.CENTER);

        if (atLeastOneUserInDatabase) {
            final JButton btnLogin = new JButton("Login");
            btnLogin.addActionListener(
                    e -> {
                        UsernameFrame loginDlg = new UsernameFrame(width, height);
                        loginDlg.setVisible(true);
                        dispose();
                    });
            getContentPane().add(btnLogin);
        }

        final JButton btnCreateUser = new JButton("Create user");
        btnCreateUser.addActionListener(
                e -> {
                    CreateUserFrame createUserFrame = new CreateUserFrame(width, height);
                    createUserFrame.setVisible(true);
                    dispose();
                });
        getContentPane().add(btnCreateUser);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLayout(new FlowLayout());
        setVisible(true);
        setLocation(560, 340);
    }
}
