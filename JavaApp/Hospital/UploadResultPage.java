package Hospital;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UploadResultPage extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public UploadResultPage() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Upload Result / Update Eligibility", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Username", "Name", "Blood Group", "Eligible"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Boolean.class : String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);

        JButton saveBtn = new JButton("Save Eligibility");
        saveBtn.addActionListener(e -> updateEligibility());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);
    }

    public void loadUsers() {
        model.setRowCount(0);
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT Username, Name, Blood_Type, Role FROM user_table WHERE Role='Client'")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("Username"),
                        rs.getString("Name"),
                        rs.getString("Blood_Type"),
                        false
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
    }

    private void updateEligibility() {
        try (Connection conn = DbConnector.getConnection();
            Statement stmt = conn.createStatement()) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String username = (String) model.getValueAt(i, 0);
                boolean eligible = (Boolean) model.getValueAt(i, 3);
                stmt.executeUpdate(
                        "UPDATE user_table SET Eligible=" + (eligible ? 1 : 0) +
                                " WHERE Username='" + username + "'"
                );
            }
            JOptionPane.showMessageDialog(this, "Eligibility updated successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update: " + e.getMessage());
        }
    }
}
