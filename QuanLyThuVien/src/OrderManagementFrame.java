package ThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OrderManagementFrame extends JFrame {
    private JTable Ordertable;
    private DefaultTableModel ordermodel;
    private JButton Search, openAdminFrameB;
    private JTextField searchfield;

    public OrderManagementFrame() {
        setTitle("Order Management");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainpanel = new JPanel(new BorderLayout());
        JPanel searchpanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel searchlabel = new JLabel("Search by Book Name");
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchpanel.add(searchlabel, gbc);

        searchfield = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        searchpanel.add(searchfield, gbc);

        // Search button
        Search = new JButton("Search");
        gbc.gridx = 2;
        gbc.gridy = 0;
        searchpanel.add(Search, gbc);

        // Go to Admin button
        openAdminFrameB = new JButton("Go to Admin");
        gbc.gridx = 3;
        gbc.gridy = 0;
        searchpanel.add(openAdminFrameB, gbc);

        String[] columns = {"Username", "Student Name", "Student ID", "Book Name", "Status", "Ordered Date", "Returned Date"};
        ordermodel = new DefaultTableModel(columns, 0);
        Ordertable = new JTable(ordermodel);
        JScrollPane orderScrollPane = new JScrollPane(Ordertable);

        mainpanel.add(searchpanel, BorderLayout.NORTH);
        mainpanel.add(orderScrollPane, BorderLayout.CENTER);

        add(mainpanel, BorderLayout.CENTER);

        loadAcceptedOrders();

        openAdminFrameB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AdminFrame().setVisible(true);
                setVisible(false); // Hide the OrderManagementFrame
            }
        });

        Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAcceptedOrders();
            }
        });
    }

    private void loadAcceptedOrders() {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM OrderBook WHERE Status = 'Accept'";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            ordermodel.setRowCount(0);

            while (rs.next()) {
                String username = rs.getString("username");
                String studentName = rs.getString("Studentname");
                String studentId = rs.getString("StudentID");
                String bookName = rs.getString("Bookname");
                String status = rs.getString("Status");
                Date orderedDate = rs.getDate("OrderedDate");
                Date returnedDate = rs.getDate("ReturnedDate");

                ordermodel.addRow(new Object[]{username, studentName, studentId, bookName, status, orderedDate, returnedDate});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while loading the orders.");
        }
    }

    private void searchAcceptedOrders() {
        String bookName = searchfield.getText().trim();

        if (bookName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a book name to search.");
            return;
        }

        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM OrderBook WHERE Bookname LIKE ? AND Status = 'Accept'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + bookName + "%");

            ResultSet rs = ps.executeQuery();

            ordermodel.setRowCount(0);

            while (rs.next()) {
                String username = rs.getString("username");
                String studentName = rs.getString("Studentname");
                String studentId = rs.getString("StudentID");
                String orderBookName = rs.getString("Bookname");
                String status = rs.getString("Status");
                Date orderedDate = rs.getDate("OrderedDate");
                Date returnedDate = rs.getDate("ReturnedDate");

                ordermodel.addRow(new Object[]{username, studentName, studentId, orderBookName, status, orderedDate, returnedDate});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while searching for orders.");
        }
    }

    public static void main(String[] args) {
        new OrderManagementFrame().setVisible(true);
    }
}
