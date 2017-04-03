import javax.swing.*;
import java.awt.*;

/**
 * @author Adam
 */
public class LoginFrame extends JFrame {

    public LoginFrame(int width, int height) {
        setTitle("Login");
        final JButton btnLogin = new JButton("Login");

        btnLogin.addActionListener(
                e -> {
                    UsernameFrame loginDlg = new UsernameFrame(width, height);
                    loginDlg.setVisible(true);
                    dispose();
                });

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLayout(new FlowLayout());
        getContentPane().add(btnLogin);
        setVisible(true);
        setLocation(560, 340);
    }
}
