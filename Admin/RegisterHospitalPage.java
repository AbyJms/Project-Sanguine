package Admin;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class RegisterHospitalPage extends JPanel {

    private JLabel title;
    private JTextField txtUsername, txtName, txtContact, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbDistrict, cmbCity;
    private JButton btnSubmit;

    // Mapping name -> ID
    private Map<String, Integer> districtMap = new HashMap<>();
    private Map<String, Integer> cityMap = new HashMap<>();

    public RegisterHospitalPage() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        title = new JLabel("Register Hospital", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        add(new JLabel("Hospital Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20); add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Hospital Name:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(20); add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Contact No:"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(20); add(txtContact, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("District:"), gbc);
        gbc.gridx = 1;
        cmbDistrict = new JComboBox<>();
        add(cmbDistrict, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        cmbCity = new JComboBox<>();
        add(cmbCity, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20); add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20); add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        btnSubmit = new JButton("Register Hospital");
        add(btnSubmit, gbc);

        // Load districts initially
        loadDistricts();

        // When a district is selected, load its cities
        cmbDistrict.addActionListener(e -> loadCities());

        // Submit button
        btnSubmit.addActionListener(e -> registerHospital());
    }

    private void loadDistricts() {
        try (Connection conn = DbConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT district_id, district_name FROM districts ORDER BY district_name");
            ResultSet rs = ps.executeQuery()) {

            cmbDistrict.removeAllItems();
            districtMap.clear();

            while (rs.next()) {
                String name = rs.getString("district_name");
                int id = rs.getInt("district_id");
                cmbDistrict.addItem(name);
                districtMap.put(name, id);
            }

            if (cmbDistrict.getItemCount() > 0) {
                cmbDistrict.setSelectedIndex(0);
                loadCities();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load districts: " + e.getMessage());
        }
    }

    private void loadCities() {
        String selectedDistrict = (String) cmbDistrict.getSelectedItem();
        if (selectedDistrict == null) return;

        Integer districtId = districtMap.get(selectedDistrict);
        if (districtId == null) return;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT city_id, city_name FROM cities WHERE district_id=? ORDER BY proximity_order")) {

            ps.setInt(1, districtId);

            try (ResultSet rs = ps.executeQuery()) {
                cmbCity.removeAllItems();
                cityMap.clear();

                while (rs.next()) {
                    String cityName = rs.getString("city_name");
                    int cityId = rs.getInt("city_id");
                    cmbCity.addItem(cityName);
                    cityMap.put(cityName, cityId);
                }

                if (cmbCity.getItemCount() > 0) cmbCity.setSelectedIndex(0);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load cities: " + e.getMessage());
        }
    }

    private void registerHospital() {
        String username = txtUsername.getText().trim();
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        String selectedDistrict = (String) cmbDistrict.getSelectedItem();
        String selectedCity = (String) cmbCity.getSelectedItem();

        if(username.isEmpty() || name.isEmpty() || password.isEmpty() || selectedDistrict == null || selectedCity == null) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        int districtId = districtMap.get(selectedDistrict);
        int cityId = cityMap.get(selectedCity);

        String sql = "INSERT INTO user_table (Name, Username, Password, Mobile_No, Gmail, district_name, city_name, district_id, city_id, Role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Hospital')";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, username);
            pst.setString(3, password);
            pst.setString(4, contact);
            pst.setString(5, email);
            pst.setString(6, selectedDistrict);
            pst.setString(7, selectedCity);
            pst.setInt(8, districtId);
            pst.setInt(9, cityId);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Hospital registered successfully.");

            // Reset fields
            txtUsername.setText(""); txtName.setText(""); txtContact.setText("");
            txtEmail.setText(""); txtPassword.setText(""); cmbDistrict.setSelectedIndex(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
