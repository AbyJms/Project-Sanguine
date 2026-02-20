package Hospital;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SanguineDashboard extends JFrame implements ActionListener {

    private JPanel mainContent;
    private CardLayout cardLayout;
    private DonorList donorListPanel;
    private UploadResultPage uploadResultPage;
    private UpdateStatusPage updateStatusPage;
    private RequestBloodPage requestBloodPage;

    private String hospitalUsername = "centralH"; // Example username

    public SanguineDashboard(String username) {
        this.hospitalUsername = username;

        setTitle("Sanguine - Blood Donation App (Hospital)");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

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

        String[] buttons = {"Home", "Donor List", "Upload Result", "Update Status", "Request"};
        for (String name : buttons) {
            JButton btn = new JButton(name);
            btn.setActionCommand(name);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(180, 45));
            btn.setBackground(new Color(120, 0, 0));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(this);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(10));
        }
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Color.WHITE);

        donorListPanel = new DonorList();
        uploadResultPage = new UploadResultPage();
        updateStatusPage = new UpdateStatusPage();
        requestBloodPage = new RequestBloodPage(hospitalUsername); // PASS username here

        mainContent.add(createHomePanel(), "Home");
        mainContent.add(donorListPanel, "Donor List");
        mainContent.add(uploadResultPage, "Upload Result");
        mainContent.add(updateStatusPage, "Update Status");
        mainContent.add(requestBloodPage, "Request");

        add(mainContent, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel header = new JLabel("HOSPITAL", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(40, 10, 20, 10));

        JPanel grid = new JPanel(new GridLayout(2, 2, 30, 30));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(40, 80, 80, 80));

        String[][] cards = {
                {"View Donor List", "See available donors and details.", "Donor List"},
                {"Upload Result", "Upload medical/test results and update eligibility.", "Upload Result"},
                {"Update Status", "Update request status for acceptors.", "Update Status"},
                {"Request Blood", "Create a blood request.", "Request"}
        };

        for (String[] c : cards) {
            grid.add(createCard(c[0], c[1], c[2]));
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCard(String title, String desc, String cardName) {
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

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                switch (cardName) {
                    case "Donor List" -> donorListPanel.loadDonors();
                    case "Upload Result" -> uploadResultPage.loadUsers();
                    case "Update Status" -> updateStatusPage.loadRequests();
                }
                cardLayout.show(mainContent, cardName);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) { card.setBackground(new Color(255, 235, 235)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Donor List" -> donorListPanel.loadDonors();
            case "Upload Result" -> uploadResultPage.loadUsers();
            case "Update Status" -> updateStatusPage.loadRequests();
        }
        cardLayout.show(mainContent, command);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SanguineDashboard("centralH"));
    }
}
