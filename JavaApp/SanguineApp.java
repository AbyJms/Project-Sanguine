// SanguineApp.java

// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import Admin.*;
import Client.*;
import Database.*;
import Hospital.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants; 
import javax.swing.SwingUtilities;

public class SanguineApp extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private Connection conn; 

    public SanguineApp() {
        this.conn = DbConnector.getConnection(); 
        
        if (this.conn == null) {
            JOptionPane.showMessageDialog(this, "Failed to connect to DB", "DB Error", 0);
            System.exit(0);
        }

        this.setTitle("Sanguine - Login / Register");
        this.setSize(450, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo((Component)null);
        this.setLayout(new BorderLayout());
        
        JLabel header = new JLabel("SANGUINE", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", 1, 32));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(new Color(180, 0, 0));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        this.add(header, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblUsername = new JLabel("Username:"); 
        lblUsername.setFont(new Font("Segoe UI", 1, 16));
        this.txtUsername = new JTextField(20);
        
        JLabel lblPassword = new JLabel("Password:"); 
        lblPassword.setFont(new Font("Segoe UI", 1, 16));
        this.txtPassword = new JPasswordField(20);
        
        JButton btnLogin = new JButton("Login"); 
        btnLogin.setBackground(new Color(180, 0, 0));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", 1, 16));
        btnLogin.setFocusPainted(false);
        
        JButton btnRegister = new JButton("Register"); 
        btnRegister.setBackground(new Color(255, 255, 255));
        btnRegister.setForeground(new Color(180, 0, 0));
        btnRegister.setFont(new Font("Segoe UI", 1, 16));
        btnRegister.setFocusPainted(false);
        btnRegister.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 0), 2));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        formPanel.add(this.txtUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        formPanel.add(this.txtPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(btnLogin, gbc);
        
        gbc.gridy = 3;
        formPanel.add(btnRegister, gbc);
        
        this.add(formPanel, BorderLayout.CENTER);
        
        btnLogin.addActionListener((e) -> {
            this.loginUser();
        });
        
        btnRegister.addActionListener((e) -> {
            this.registerUser();
        });
        
        this.setVisible(true);
    }

    private void loginUser() {
        String username = this.txtUsername.getText().trim();
        String password = (new String(this.txtPassword.getPassword())).trim();
        
        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                // Select Role AND Username
                String sql = "SELECT Role, Username FROM user_table WHERE Username=? AND Password=?";
                PreparedStatement ps = this.conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String role = rs.getString("Role");
                    String loggedInUsername = rs.getString("Username"); 
                    
                    // Pass the role AND the extracted username
                    this.openDashboard(role, loggedInUsername); 
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", 0);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error", "Error", 0);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Enter username and password", "Error", 2);
        }
    }

    private void registerUser() {
        // FIX: Call the dedicated registration dialog
        if (this.conn != null) {
            (new ClientRegistrationDialog(this.conn)).setVisible(true);
        } else {
             JOptionPane.showMessageDialog(this, "Database connection not available.", "DB Error", 0);
        }
    }

    // NEW SIGNATURE: Accepts username parameter
    private void openDashboard(String role, String username) { 
        switch (role) {
            case "Admin":
                // FIX: Reverted to no-arg call to compile against old constructor
                (new AdminDashboard()).setVisible(true); 
                break;
            case "Hospital":
                // FIX: Reverted to no-arg call to compile against old constructor
                (new SanguineDashboard(username)).setVisible(true); 
                break;
            case "Client":
                // Client Data Flow is correct: pass username
                (new UserDashboard(username)).setVisible(true); 
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SanguineApp::new);
    }
}