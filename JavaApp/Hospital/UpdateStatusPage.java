package Hospital;

import Database.DbConnector;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class UpdateStatusPage extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private final String[] statusOptions = {"Pending", "Fulfilled", "Cancelled"};

    public UpdateStatusPage() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Update Request Status", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Acceptor", "Blood", "Quantity", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Status column editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);

        // Make Status column a JComboBox
        TableColumn statusColumn = table.getColumnModel().getColumn(4);
        JComboBox<String> comboBox = new JComboBox<>(statusOptions);
        statusColumn.setCellEditor(new DefaultCellEditor(comboBox));

        JButton saveBtn = new JButton("Update Status");
        saveBtn.addActionListener(e -> saveStatus());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);
    }

    public void loadRequests() {
        model.setRowCount(0);
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT request_pk, acceptor_name, blood, quantity, status FROM request")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_pk"),
                        rs.getString("acceptor_name"),
                        rs.getString("blood"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load requests: " + e.getMessage());
        }
    }

    private void saveStatus() {
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            for (int i = 0; i < model.getRowCount(); i++) {
                int pk = (Integer) model.getValueAt(i, 0);
                String status = (String) model.getValueAt(i, 4);

                stmt.executeUpdate(
                        "UPDATE request SET status='" + status + "' WHERE request_pk=" + pk
                );
            }

            JOptionPane.showMessageDialog(this, "Requests updated successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update requests: " + e.getMessage());
        }
    }
}
