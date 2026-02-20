package Admin;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ViewUsersPage extends JPanel {

    private JTable usersTable;
    private DefaultTableModel model;

    public ViewUsersPage() {
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("View Users", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Username", "Name", "Role", "Contact No", "Gmail", "Blood Group"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        usersTable = new JTable(model);
        usersTable.setRowHeight(28);

        add(new JScrollPane(usersTable), BorderLayout.CENTER);

        loadUsers();
    }

    public void loadUsers() {
        model.setRowCount(0);
        String sql = "SELECT Username, Name, Role, Mobile_No, Gmail, Blood_Type FROM user_table";
        try (Connection conn = DbConnector.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("Username"),
                        rs.getString("Name"),
                        rs.getString("Role"),
                        rs.getString("Mobile_No"),
                        rs.getString("Gmail"),
                        rs.getString("Blood_Type")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
    }
}
