package Hospital;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DonorList extends JPanel {

    private JTable donorTable;
    private DefaultTableModel model;

    public DonorList() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        String[] columns = {"Name", "Blood Group", "Contact", "Gmail", "Username"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        donorTable = new JTable(model);
        donorTable.setRowHeight(28);
        donorTable.setFont(new Font("Arial", Font.PLAIN, 14));
        donorTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(donorTable);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadDonors() {
        model.setRowCount(0);
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT Name, Blood_Type, Mobile_No, Gmail, Username " +
                             "FROM user_table WHERE Role='Client'")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("Name"),
                        rs.getString("Blood_Type"),
                        rs.getString("Mobile_No"),
                        rs.getString("Gmail"),
                        rs.getString("Username")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load donors: " + e.getMessage());
        }
    }
}
