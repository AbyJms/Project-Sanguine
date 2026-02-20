package Hospital;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class RequestBloodPage extends JPanel {

    private String hospitalUsername;
    private JTextField txtQuantity;
    private JComboBox<String> comboBloodType;

    private String hospitalName;
    private String hospitalMobile;
    private Integer districtId; // can be null
    private Integer cityId;     // can be null

    public RequestBloodPage(String hospitalUsername) {
        this.hospitalUsername = hospitalUsername;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Request Blood", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // Fetch hospital details including district_id and city_id
        loadHospitalDetails();

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Blood Type
        JLabel lblBlood = new JLabel("Select Blood Type:");
        lblBlood.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblBlood, gbc);

        comboBloodType = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        gbc.gridx = 1;
        formPanel.add(comboBloodType, gbc);

        // Quantity
        JLabel lblQuantity = new JLabel("Quantity (units):");
        lblQuantity.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblQuantity, gbc);

        txtQuantity = new JTextField();
        gbc.gridx = 1;
        formPanel.add(txtQuantity, gbc);

        // Request button
        JButton btnRequest = new JButton("Submit Request");
        btnRequest.setBackground(new Color(180, 0, 0));
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(btnRequest, gbc);

        btnRequest.addActionListener(e -> submitRequest());

        add(formPanel, BorderLayout.CENTER);
    }

    private void loadHospitalDetails() {
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Name, Mobile_No, district_id, city_id FROM user_table WHERE Username=?")) {

            ps.setString(1, hospitalUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hospitalName = rs.getString("Name");
                hospitalMobile = rs.getString("Mobile_No");
                districtId = rs.getObject("district_id") != null ? rs.getInt("district_id") : null;
                cityId = rs.getObject("city_id") != null ? rs.getInt("city_id") : null;
            } else {
                JOptionPane.showMessageDialog(this, "Hospital details not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load hospital details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitRequest() {
        String bloodType = (String) comboBloodType.getSelectedItem();
        String quantityStr = txtQuantity.getText().trim();

        if (bloodType == null || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select blood type and enter quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert into request table
        String sql = "INSERT INTO request (acceptor, acceptor_name, mobile_no, blood, quantity, district_id, city_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hospitalUsername); // acceptor
            ps.setString(2, hospitalName);     // acceptor_name
            ps.setString(3, hospitalMobile);   // mobile_no
            ps.setString(4, bloodType);        // blood
            ps.setInt(5, quantity);            // quantity
            if (districtId != null) ps.setInt(6, districtId); else ps.setNull(6, java.sql.Types.INTEGER);
            if (cityId != null) ps.setInt(7, cityId); else ps.setNull(7, java.sql.Types.INTEGER);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Request submitted successfully!");

            // Reset fields
            txtQuantity.setText("");
            comboBloodType.setSelectedIndex(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to submit request: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
