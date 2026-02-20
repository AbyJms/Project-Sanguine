package Client;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class EditProfileDialog extends JDialog {
    private UserDashboard parentDashboard;
    private UserDashboard.User user;
    private Connection conn;
    
    private JTextField txtName, txtMobile, txtEmail;
    private JComboBox<String> cbBlood, cbDistrict, cbCity;

    private static final String[] BLOOD_TYPES = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};

    public EditProfileDialog(UserDashboard parent, UserDashboard.User currentUser) {
        super(parent, "Edit Profile Details", true);
        this.parentDashboard = parent;
        this.user = currentUser;
        
        // Assuming DbConnector.getConnection() is accessible and static
        try {
            this.conn = Database.DbConnector.getConnection(); 
        } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Database Error: Cannot open connection.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        setSize(450, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --- Components ---
        txtName = new JTextField(user.getName(), 25);
        txtMobile = new JTextField(user.getMobileNo(), 25);
        txtEmail = new JTextField(user.getGmail(), 25);
        cbBlood = new JComboBox<>(BLOOD_TYPES);
        cbDistrict = new JComboBox<>();
        cbCity = new JComboBox<>();

        // Set initial blood type selection
        cbBlood.setSelectedItem(user.getBloodType());

        // --- Layout ---
        int y = 0;
        y = addField(formPanel, gbc, "Full Name:", txtName, y);
        y = addField(formPanel, gbc, "Mobile No.:", txtMobile, y);
        y = addField(formPanel, gbc, "Email (Gmail):", txtEmail, y);
        y = addField(formPanel, gbc, "Blood Type:", cbBlood, y);
        y = addField(formPanel, gbc, "District:", cbDistrict, y);
        y = addField(formPanel, gbc, "City:", cbCity, y);

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(180, 0, 0));
        btnSave.setForeground(Color.WHITE);
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(btnSave, gbc);

        parentDashboard.loadDistricts(cbDistrict);
        
        if (user.getDistrictName() != null) {
            cbDistrict.setSelectedItem(user.getDistrictName());
            parentDashboard.loadCitiesForDistrict((String) cbDistrict.getSelectedItem(), cbCity);
            // Select current City
            cbCity.setSelectedItem(user.getCityName());
        }
        
        cbDistrict.addActionListener(e -> parentDashboard.loadCitiesForDistrict((String) cbDistrict.getSelectedItem(), cbCity));
        btnSave.addActionListener(e -> saveProfileChanges());

        add(formPanel, BorderLayout.CENTER);
    }

    private int addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        gbc.gridx = 0; gbc.gridy = y; panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridy = y; panel.add(field, gbc);
        return y + 1;
    }

    private void saveProfileChanges() {
        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();
        String blood = (String) cbBlood.getSelectedItem();
        String districtName = (String) cbDistrict.getSelectedItem();
        String cityName = (String) cbCity.getSelectedItem();
        
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || districtName == null || cityName == null) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {

            int districtId = parentDashboard.getDistrictId(Database.DbConnector.getConnection(), districtName);
            int cityId = parentDashboard.getCityId(Database.DbConnector.getConnection(), cityName);

            // Pass all updated data back to the UserDashboard's update method
            parentDashboard.updateUserProfile(name, mobile, email, blood, districtId, cityId, districtName, cityName);
            this.dispose(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "SQL Error: Failed to find location ID or database connection failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}