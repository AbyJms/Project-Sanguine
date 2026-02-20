package Client;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;

public class ClientRegistrationDialog extends JDialog {

    private Connection conn;
    private JTextField txtName, txtUsername, txtGmail, txtMobileNo;
    private JPasswordField txtPassword;
    private JComboBox<String> cbBloodType, cbDistrict, cbCity;
    
    // Array of valid blood types for the ComboBox
    private static final String[] BLOOD_TYPES = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};

    public ClientRegistrationDialog(Connection connection) {
        this.conn = connection;
        setTitle("Client Registration");
        setSize(400, 450);
        setModal(true);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        JButton btnSubmit = new JButton("Submit Registration");
        btnSubmit.addActionListener(e -> attemptRegistration());
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.add(btnSubmit);
        add(southPanel, BorderLayout.SOUTH);
        
        // Load districts and set up listeners
        loadDistricts();
        if (cbDistrict.getItemCount() > 0) {
            loadCitiesForSelectedDistrict();
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        
        // 1. Name
        txtName = new JTextField(20);
        y = addField(panel, gbc, "Full Name:", txtName, y);
        
        // 2. Username
        txtUsername = new JTextField(20);
        y = addField(panel, gbc, "Username:", txtUsername, y);

        // 3. Password
        txtPassword = new JPasswordField(20);
        y = addField(panel, gbc, "Password:", txtPassword, y);

        // 4. Blood Type
        cbBloodType = new JComboBox<>(BLOOD_TYPES);
        y = addField(panel, gbc, "Blood Type:", cbBloodType, y);

        // 5. Gmail
        txtGmail = new JTextField(20);
        y = addField(panel, gbc, "Gmail:", txtGmail, y);

        // 6. Mobile No
        txtMobileNo = new JTextField(20);
        y = addField(panel, gbc, "Mobile No.:", txtMobileNo, y);

        // 7. District Selection (Cascading Combo Box 1)
        cbDistrict = new JComboBox<>();
        y = addField(panel, gbc, "District:", cbDistrict, y);

        // 8. City Selection (Cascading Combo Box 2)
        cbCity = new JComboBox<>();
        y = addField(panel, gbc, "City:", cbCity, y);
        
        // Add listener for cascading effect (Must be done after component declaration)
        cbDistrict.addActionListener(e -> loadCitiesForSelectedDistrict());

        return panel;
    }
    
    private int addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        gbc.gridx = 0; gbc.gridy = y; panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridy = y; panel.add(field, gbc);
        return y + 1;
    }

    // --- DATABASE LOOKUP METHODS ---

    private void loadDistricts() {
        String sql = "SELECT district_name FROM Districts ORDER BY district_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            cbDistrict.removeAllItems();
            while (rs.next()) {
                cbDistrict.addItem(rs.getString("district_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load districts: " + ex.getMessage(), 
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCitiesForSelectedDistrict() {
        String selectedDistrict = (String) cbDistrict.getSelectedItem();
        if (selectedDistrict == null) return;

        // SQL uses a JOIN to find the correct cities based on the selected district name
        String sql = "SELECT C.city_name FROM Cities C JOIN Districts D " +
                     "ON C.district_id = D.district_id " +
                     "WHERE D.district_name = ? ORDER BY C.proximity_order ASC";
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selectedDistrict);
            try (ResultSet rs = ps.executeQuery()) {
                
                cbCity.removeAllItems();
                while (rs.next()) {
                    cbCity.addItem(rs.getString("city_name"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Silence this error during development if the Cities table is incomplete
        }
    }

    private int getDistrictIdByName(String districtName) throws SQLException {
        String sql = "SELECT district_id FROM Districts WHERE district_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, districtName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("district_id");
                }
            }
        }
        throw new SQLException("District ID not found for name: " + districtName);
    }
    
    private int getCityIdByName(String cityName) throws SQLException {
        String sql = "SELECT city_id FROM Cities WHERE city_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cityName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("city_id");
                }
            }
        }
        throw new SQLException("City ID not found for name: " + cityName);
    }
    
    // --- REGISTRATION LOGIC ---

    private void attemptRegistration() {
        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String bloodType = (String) cbBloodType.getSelectedItem();
        String gmail = txtGmail.getText().trim();
        String mobileNo = txtMobileNo.getText().trim();
        
        String districtName = (String) cbDistrict.getSelectedItem();
        String cityName = (String) cbCity.getSelectedItem();

        // 1. Basic validation for mandatory fields
        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || districtName == null || cityName == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields (Name, Username, Password, District, City).", 
                "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the numeric IDs from the database names
            int districtId = getDistrictIdByName(districtName);
            int cityId = getCityIdByName(cityName);
            
            // 2. Check for unique constraints (Username, Gmail, MobileNo)
            String checkSql = "SELECT Username FROM user_table WHERE Username=? OR Gmail=? OR Mobile_No=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            checkStmt.setString(2, gmail);
            checkStmt.setString(3, mobileNo);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username, Gmail, or Mobile No. already exists.", 
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Final Insertion into User_Table (Triggers handle Client sub-table)
            // Note: The INSERT now uses the numeric IDs and the text names.
            String insertSql = "INSERT INTO user_table(" +
                               "Username, Password, Name, Blood_Type, Gmail, Mobile_No, Role, " + 
                               "district_id, city_id, district_name, city_name) " +
                               "VALUES (?, ?, ?, ?, ?, ?, 'Client', ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(insertSql);
            ps.setString(1, username);
            ps.setString(2, password); // Implement HASHING in a real application!
            ps.setString(3, name);
            ps.setString(4, bloodType);
            ps.setString(5, gmail);
            ps.setString(6, mobileNo);
            ps.setInt(7, districtId);
            ps.setInt(8, cityId);
            ps.setString(9, districtName);
            ps.setString(10, cityName);
            
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.");
            this.dispose(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during registration: " + ex.getMessage(), 
                "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}