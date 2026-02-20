package Admin;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.*;

public class RegisterNewAdminPage extends JPanel {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnRegister;

    public RegisterNewAdminPage() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel title = new JLabel("Register New Admin", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20); add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20); add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        btnRegister = new JButton("Register Admin");
        add(btnRegister, gbc);

        btnRegister.addActionListener(e -> registerAdmin());
    }

    private void registerAdmin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if(username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password required.");
            return;
        }

        String sql = "INSERT INTO user_table (Name, Username, Password, district_id, city_id, Role) " +
            "VALUES (?, ?, ?, NULL, NULL, 'Admin')";

        try (Connection conn = DbConnector.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            
pst.setString(1, username); // Use username as Name
pst.setString(2, username);
pst.setString(3, password);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Admin registered successfully.");
            txtUsername.setText(""); txtPassword.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
