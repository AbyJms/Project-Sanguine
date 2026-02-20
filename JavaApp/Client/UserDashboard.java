package Client;

import Database.DbConnector;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserDashboard extends JFrame {

    private JPanel mainPanel;
    public User user; 
    private ArrayList<String[]> donationHistory = new ArrayList<>();
    
    // Global components for the Request Screen
    private JComboBox<String> cmbDistrict;
    private JComboBox<String> cmbCity;
    private JTextField txtQuantity;
    private JComboBox<String> cmbBloodType; 

    public UserDashboard(String loggedInUsername) { 
        user = new User(loggedInUsername); 
        
        if (!loadUserData()) {
             JOptionPane.showMessageDialog(this, "Failed to load user data.", "Login Error", JOptionPane.ERROR_MESSAGE);
             System.exit(0);
        }
        
        setTitle("Sanguine - Blood Donation App");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(120, 0, 0));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sanguine");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 10));
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(10));

        // Sidebar buttons (Notifications is back)
        String[] buttons = {"Home", "Profile", "History", "Notifications"};
        for (String name : buttons) {
            JButton btn = new JButton(name);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(180, 45));
            btn.setBackground(new Color(120, 0, 0));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                if (name.equals("Notifications")) {
                    showNotifications(); 
                } else if (name.equals("History")) {
                    showHistory();
                } else {
                    showView(name);
                }
            });

            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(10));
        }

        add(sidebar, BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        showView("Home");
        setVisible(true);
    }

    private boolean loadUserData() {
        String sql = "SELECT Name, Role, eligible, Mobile_No, Gmail, Blood_Type, district_id, city_id FROM User_Table WHERE Username = ?"; 
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUsername());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setName(rs.getString("Name"));
                    user.setCurrentRole(rs.getString("Role"));
                    user.setGmail(rs.getString("Gmail")); 
                    
                    String fetchedMobile = rs.getString("Mobile_No");
                    user.setMobileNo((fetchedMobile != null && !fetchedMobile.trim().isEmpty()) ? fetchedMobile : "N/A");
                    
                    user.setBloodType(rs.getString("Blood_Type"));
                    
                    user.setDistrictId(rs.getInt("district_id"));
                    user.setCityId(rs.getInt("city_id"));
                    
                    user.setDistrictName(getDistrictNameById(rs.getInt("district_id")));
                    user.setCityName(getCityNameById(rs.getInt("city_id")));

                    boolean isEligible = rs.getBoolean("eligible");
                    user.setEligibility(isEligible ? "Eligible" : "Not Eligible");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** Show selected view **/
    private void showView(String viewName) {
        mainPanel.removeAll();
        switch (viewName) {
            case "Home" -> mainPanel.add(createHomePanel(), BorderLayout.CENTER);
            case "Profile" -> mainPanel.add(createProfilePanel(), BorderLayout.CENTER);
            case "Status" -> mainPanel.add(createStatusPanel(), BorderLayout.CENTER); 
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /** HOME PANEL (MODIFIED: Unified Donor/Acceptor actions and removed Donate Card) **/
    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Welcome, " + user.getName(), SwingConstants.CENTER); 
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(40, 10, 20, 10));
        homePanel.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 30, 30));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(40, 80, 80, 80));

        JPanel requestCard = createCard("REQUEST BLOOD", "Submit a request for blood by specifying quantity and location.");
        requestCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showRequestScreen(); }
        });

        JPanel eligibilityCard = createCard("View Eligibility Status", "Check your donation cool-down status.");
        eligibilityCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(UserDashboard.this,
                        "Current Status: " + user.getEligibility(),
                        "Eligibility Status", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        grid.add(requestCard);
        grid.add(eligibilityCard);

        homePanel.add(grid, BorderLayout.CENTER);
        return homePanel;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(Color.WHITE);

        JLabel header = new JLabel("PROFILE", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(40, 10, 20, 10));
        profilePanel.add(header, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        
        // --- Display User Details ---
        JPanel detailPanel = createDetailDisplayPanel();
        contentPanel.add(detailPanel, BorderLayout.NORTH);

        // --- Edit Button ---
        JButton btnEdit = new JButton("Edit Profile");
        btnEdit.setBackground(new Color(180, 0, 0));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEdit.setPreferredSize(new Dimension(150, 40));
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        southPanel.add(btnEdit);
        
        contentPanel.add(southPanel, BorderLayout.CENTER);

        btnEdit.addActionListener(e -> showEditProfileDialog());
        
        profilePanel.add(contentPanel, BorderLayout.CENTER);
        return profilePanel;
    }
    
    // NEW: Panel to display user details
    private JPanel createDetailDisplayPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Helper to add label/value pairs
        class DetailRow {
            void add(String label, String value) {
                JLabel lbl = new JLabel(label);
                lbl.setFont(labelFont);
                JLabel val = new JLabel(value);
                val.setFont(valueFont);
                panel.add(lbl);
                panel.add(val);
            }
        }
        
        DetailRow row = new DetailRow();
        row.add("Username:", user.getUsername());
        row.add("Name:", user.getName());
        row.add("Blood Type:", user.getBloodType());
        row.add("Email (Gmail):", user.getGmail());
        row.add("Mobile No.:", user.getMobileNo());
        row.add("District:", user.getDistrictName());
        row.add("City:", user.getCityName());
        
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Current Status", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(40, 10, 30, 10));
        statusPanel.add(header, BorderLayout.NORTH);

        String status = user.getCurrentRole().equals("Client") ? 
                "Eligibility: " + user.getEligibility() :
                "Blood Request: Pending confirmation";

        JPanel grid = new JPanel(new GridLayout(1, 1));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(150, 150, 150, 150));
        JPanel statusCard = createCard("Status", status);
        grid.add(statusCard);
        statusPanel.add(grid, BorderLayout.CENTER);
        return statusPanel;
    }
    
    private JPanel createCard(String title, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(120, 0, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));

        JLabel descLabel = new JLabel("<html><span style='color:gray;'>" + desc + "</span></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 10));

        card.add(titleLabel);
        card.add(descLabel);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(255, 240, 240)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    private JPanel createCard(String title, String desc, String cardName) {
        JPanel card = createCard(title, desc);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(cardName.equals("EditProfile")) showEditProfileDialog();
                else if(cardName.equals("History")) showHistory();
            }
        });
        return card;
    }
    
    // NEW: Opens a dedicated dialog for editing profile
    private void showEditProfileDialog() {
        new EditProfileDialog(this, user).setVisible(true);
        loadUserData(); 
        showView("Profile");
    }

    public void showRequestScreen() {
        JPanel requestPanel = new JPanel(new GridBagLayout());
        requestPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font textFont = new Font("Segoe UI", Font.PLAIN, 16);
        
        int y = 0; // Start row counter

        // Display the user's blood type (No selection box)
        JLabel lblUserBlood = new JLabel("Your Blood Type:");
        lblUserBlood.setFont(labelFont);
        JLabel lblBloodValue = new JLabel(user.getBloodType());
        lblBloodValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.WEST;
        requestPanel.add(lblUserBlood, gbc);
        gbc.gridx = 1; requestPanel.add(lblBloodValue, gbc);
        y++; // Increment row

        // --- 2. District ---
        JLabel lblDistrict = new JLabel("Select District:");
        lblDistrict.setFont(labelFont);
        cmbDistrict = new JComboBox<>();
        cmbDistrict.setFont(textFont);
        
        gbc.gridx = 0; gbc.gridy = y; requestPanel.add(lblDistrict, gbc);
        gbc.gridx = 1; requestPanel.add(cmbDistrict, gbc);
        y++; // Increment row
        
        // --- 3. City (Cascading) ---
        JLabel lblCity = new JLabel("Select City:");
        lblCity.setFont(labelFont);
        cmbCity = new JComboBox<>();
        cmbCity.setFont(textFont);
        
        gbc.gridx = 0; gbc.gridy = y; requestPanel.add(lblCity, gbc);
        gbc.gridx = 1; requestPanel.add(cmbCity, gbc);
        y++; // Increment row
        
        // --- 4. Quantity ---
        JLabel lblQuantity = new JLabel("Quantity (units):");
        lblQuantity.setFont(labelFont);
        txtQuantity = new JTextField("1", 5);
        txtQuantity.setFont(textFont);
        
        gbc.gridx = 0; gbc.gridy = y; requestPanel.add(lblQuantity, gbc);
        gbc.gridx = 1; requestPanel.add(txtQuantity, gbc);
        y++; // Increment row

        // --- 5. Request Button ---
        JButton requestBtn = new JButton("Submit Request");
        requestBtn.setBackground(new Color(120, 0, 0));
        requestBtn.setForeground(Color.WHITE);
        requestBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        requestPanel.add(requestBtn, gbc);

        // --- Data Loading and Listeners ---
        loadDistricts(cmbDistrict);
        
        // Listener for cascading City ComboBox
        cmbDistrict.addActionListener(e -> loadCitiesForDistrict((String) cmbDistrict.getSelectedItem(), cmbCity));
        
        // Initial load for cities
        if (cmbDistrict.getItemCount() > 0) {
            loadCitiesForDistrict((String) cmbDistrict.getSelectedItem(), cmbCity);
        }

        // --- Finalize Panel ---
        mainPanel.removeAll();
        mainPanel.add(requestPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();

        // --- Action Listener ---
        requestBtn.addActionListener(e -> attemptBloodRequest());
    }

    public void updateUserProfile(String name, String mobile, String email, String blood, int districtId, int cityId, String districtName, String cityName) {
        // This method executes the database update
        String sql = "UPDATE User_Table SET Name = ?, Mobile_No = ?, Gmail = ?, Blood_Type = ?, district_id = ?, city_id = ?, district_name = ?, city_name = ? WHERE Username = ?";
        
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name.trim());
            ps.setString(2, mobile.trim());
            ps.setString(3, email.trim());
            ps.setString(4, blood);
            ps.setInt(5, districtId);
            ps.setInt(6, cityId);
            ps.setString(7, districtName);
            ps.setString(8, cityName);
            ps.setString(9, user.getUsername());
            
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Update local user object after successful DB update
                user.setName(name.trim());
                user.setMobileNo(mobile.trim());
                user.setGmail(email.trim());
                user.setBloodType(blood);
                user.setDistrictId(districtId);
                user.setCityId(cityId);
                user.setDistrictName(districtName);
                user.setCityName(cityName);
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update profile. User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
        // Force reload of profile view to show changes
        showView("Profile");
    }


    private void showHistory() {
        String loggedInUsername = user.getUsername(); 
        String[][] data;
        String[] cols = {"Acceptor Name", "Mobile", "Blood", "District", "City", "Quantity", "Status"}; 

        try (Connection conn = DbConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT acceptor_name, mobile_no, blood, D.district_name, C.city_name, quantity, status " +
                    "FROM Request R " +
                    "JOIN Districts D ON R.district_id = D.district_id " +
                    "JOIN Cities C ON R.city_id = C.city_id " +
                    "WHERE R.acceptor=?")) {

            ps.setString(1, loggedInUsername); 
            
            ResultSet rs = ps.executeQuery();
            ArrayList<String[]> rows = new ArrayList<>();
            
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("acceptor_name"),
                        rs.getString("mobile_no"), 
                        rs.getString("blood"),
                        rs.getString("district_name"), 
                        rs.getString("city_name"),     
                        rs.getString("quantity"),
                        rs.getString("status")
                });
            }
            data = rows.toArray(new String[0][0]);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch request history: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            data = new String[0][0]; 
        }


        JTable table = new JTable(new DefaultTableModel(data, cols));
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        mainPanel.removeAll();
        mainPanel.add(pane, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void switchRole() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Switch to " + (user.getCurrentRole().equals("Client") ? "Acceptor" : "Client") + "?",
                "Confirm Role Switch", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            user.toggleRole();
            JOptionPane.showMessageDialog(this, "Switched to: " + user.getCurrentRole());
            showView("Home");
        }
    }

    // ------------------- NOTIFICATIONS (Removed functionality) -------------------
    // UserDashboard.java

public void showNotifications() {
    // 1. Get filter criteria from the logged-in user object
    String userBloodType = user.getBloodType();
    int userDistrictId = user.getDistrictId();
    String loggedInUsername = user.getUsername();
    
    String[][] data;
    // MODIFIED COLUMNS: Removed "Status"
    String[] cols = {"Acceptor Name", "Mobile", "Blood", "City", "Quantity"}; 

    try (Connection conn = DbConnector.getConnection();
         PreparedStatement ps = conn.prepareStatement(
                 // MODIFIED SQL: Removed 'status' from SELECT list
                 "SELECT acceptor_name, mobile_no, blood, C.city_name, quantity " +
                 "FROM Request R " +
                 "JOIN Cities C ON R.city_id = C.city_id " +
                 "WHERE R.blood = ? " +
                 "  AND R.district_id = ? " + 
                 "  AND R.status = 'Pending' " + // Still filters by Pending status
                 "  AND R.acceptor != ?")) { // Exclude own requests

        ps.setString(1, userBloodType);
        ps.setInt(2, userDistrictId);
        ps.setString(3, loggedInUsername);
        
        ResultSet rs = ps.executeQuery();
        ArrayList<String[]> rows = new ArrayList<>();
        
        while (rs.next()) {
            rows.add(new String[]{
                    rs.getString("acceptor_name"),
                    rs.getString("mobile_no"), 
                    rs.getString("blood"),
                    rs.getString("city_name"),     
                    rs.getString("quantity") // Only 5 fields now
            });
        }
        data = rows.toArray(new String[0][0]);
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to fetch notifications: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // --- FIX: Add JTable and JScrollPane to mainPanel ---
    JTable table = new JTable(new DefaultTableModel(data, cols));
    JScrollPane pane = new  JScrollPane(table);
    pane.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

    mainPanel.removeAll();
    mainPanel.add(pane, BorderLayout.CENTER); // Add scroll pane to the main panel
    mainPanel.revalidate();
    mainPanel.repaint();
}

    // --- Helper Method to Load Districts --- (Required by EditProfileDialog)
    public void loadDistricts(JComboBox<String> comboBox) { // MADE PUBLIC
        ArrayList<String> districtList = new ArrayList<>();
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT district_name FROM districts ORDER BY district_name")) {

            while (rs.next()) {
                districtList.add(rs.getString("district_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        comboBox.setModel(new DefaultComboBoxModel<>(districtList.toArray(new String[0])));
    }

    // --- Helper Method to Load Cities (Cascading) --- (Required by EditProfileDialog)
    public void loadCitiesForDistrict(String districtName, JComboBox<String> comboBox) { // MADE PUBLIC
        if (districtName == null) return;
        
        ArrayList<String> cityList = new ArrayList<>();
        String sql = "SELECT C.city_name FROM Cities C JOIN Districts D ON C.district_id = D.district_id WHERE D.district_name = ? ORDER BY C.proximity_order ASC";
                     
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, districtName);
            try (ResultSet rs = ps.executeQuery()) {
                comboBox.removeAllItems();
                while (rs.next()) {
                    cityList.add(rs.getString("city_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Error handling here is silent to keep the app responsive
        }
        comboBox.setModel(new DefaultComboBoxModel<>(cityList.toArray(new String[0])));
    }
    
    // --- Helper Method to Get IDs from Names (Required by EditProfileDialog) ---
    public int getDistrictId(Connection conn, String districtName) throws SQLException { // MADE PUBLIC
        String sql = "SELECT district_id FROM Districts WHERE district_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, districtName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("district_id");
                throw new SQLException("District ID not found for name: " + districtName);
            }
        }
    }
    
    public int getCityId(Connection conn, String cityName) throws SQLException { // MADE PUBLIC
        String sql = "SELECT city_id FROM Cities WHERE city_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cityName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("city_id");
                throw new SQLException("City ID not found for name: " + cityName);
            }
        }
    }
    
    // --- New Helper to get Name from ID (used in loadUserData) ---
    private String getDistrictNameById(int id) throws SQLException {
        String sql = "SELECT district_name FROM Districts WHERE district_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("district_name") : "N/A";
            }
        }
    }
    private String getCityNameById(int id) throws SQLException {
        String sql = "SELECT city_name FROM Cities WHERE city_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("city_name") : "N/A";
            }
        }
    }

    // FIX 1: Changed method signature to public
    public void attemptBloodRequest() {
        // Validation and Data Retrieval
        String bloodType = user.getBloodType(); 
        String districtName = (String) cmbDistrict.getSelectedItem();
        String cityName = (String) cmbCity.getSelectedItem();
        int quantity;
        
        // Get user data from the object (Name and Mobile are now dynamically fetched)
        String acceptorUsername = user.getUsername();
        String acceptorName = user.getName();
        String acceptorMobileNo = user.getMobileNo();

        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity (1 or more).", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (districtName == null || cityName == null) {
            JOptionPane.showMessageDialog(this, "Please select a District and City.", "Missing Location", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DbConnector.getConnection()) {
            
            // --- STEP A: Get Foreign Key IDs ---
            int districtId = getDistrictId(conn, districtName);
            int cityId = getCityId(conn, cityName);

            // --- STEP B: Execute SQL INSERT with Username, Name, and Mobile ---
            String sql = "INSERT INTO Request (acceptor, acceptor_name, mobile_no, blood, quantity, district_id, city_id, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, acceptorUsername);      // Foreign Key (Username)
                ps.setString(2, acceptorName);          // Display Name
                ps.setString(3, acceptorMobileNo);      // Mobile Number (N/A if missing)
                ps.setString(4, bloodType);             // Dynamic Blood Type from User object
                ps.setInt(5, quantity);
                ps.setInt(6, districtId);
                ps.setInt(7, cityId);
                
                int rows = ps.executeUpdate();
                
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Blood request submitted successfully for " + acceptorName + "!", 
                        "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                    showView("Home");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to submit request.", "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during request submission: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static class User { // MADE PUBLIC
        private String currentRole;
        private String eligibility;
        private String username; 
        private String name; 
        private String mobileNo; 
        private String bloodType; 
        private String gmail; 
        private int districtId;
        private int cityId;
        private String districtName;
        private String cityName;


        // Modified constructor
        public User(String username) { 
            this.username = username;
            this.currentRole = "Client";
            this.eligibility = "Unknown";
            this.name = username; 
            this.mobileNo = ""; 
            this.bloodType = "N/A";
            this.gmail = "";
            this.districtId = -1;
            this.cityId = -1;
            this.districtName = "Loading...";
            this.cityName = "Loading...";
        }
        
        // Getters
        public String getCurrentRole() { return currentRole; }
        public String getEligibility() { return eligibility; }
        public String getUsername() { return username; }
        public String getName() { return name; } 
        public String getMobileNo() { return mobileNo; }
        public String getBloodType() { return bloodType; } 
        public String getGmail() { return gmail; }
        public int getDistrictId() { return districtId; }
        public int getCityId() { return cityId; }
        public String getDistrictName() { return districtName; } 
        public String getCityName() { return cityName; } 

        
        // Setters to be used by loadUserData()
        public void setName(String name) { this.name = name; } 
        public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
        public void setGmail(String gmail) { this.gmail = gmail; } 
        public void setCurrentRole(String role) { this.currentRole = role; }
        public void setEligibility(String e) { eligibility = e; }
        public void setBloodType(String bloodType) { this.bloodType = bloodType; } 
        public void setDistrictId(int districtId) { this.districtId = districtId; }
        public void setCityId(int cityId) { this.cityId = cityId; }
        public void setDistrictName(String districtName) { this.districtName = districtName; }
        public void setCityName(String cityName) { this.cityName = cityName; }
        
        public void toggleRole() { 
            currentRole = currentRole.equals("Client") ? "Acceptor" : "Client"; 
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserDashboard("abyjms").setVisible(true));
    }
}