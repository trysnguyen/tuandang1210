
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentFrame extends JFrame {
    private JTable Booktable, Ordertable;
    private DefaultTableModel bookmodel, ordermodel;
    private JButton Order, Search;
    private JTextField searchfield;
    private int clickCount = 0;
    private static final int DOUBLE_CLICK_THRESHOLD = 500;
    private static Timer resetTimer;


    public StudentFrame(String studentid, String studentname, String username)  {
        setTitle("Welcome "+ studentid);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel mainpanel = new JPanel(new GridLayout(1,2));

        JPanel rightpanel = new JPanel(new BorderLayout());
        JPanel leftpanel = new JPanel(new BorderLayout());
        JPanel buttonpanel = new JPanel(new GridBagLayout());

        JPanel searchpanel = new JPanel(new GridBagLayout());
        JLabel searchlabel = new JLabel("Search bookname");
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchpanel.add(searchlabel, gbc);
        searchfield = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        searchpanel.add(searchfield, gbc);

        Order = new JButton("Order");
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonpanel.add(Order, gbc);
        Search = new JButton("Search");
        gbc.gridx = 1;
        gbc.gridy = 0;
            buttonpanel.add(Search,gbc);


        String[] column = {"Bookname", "Author", "Category"};
        bookmodel = new DefaultTableModel(column,0);
        Booktable = new JTable(bookmodel);
        JScrollPane bookscrollPane = new JScrollPane(Booktable);
        leftpanel.add(searchpanel, BorderLayout.NORTH);
        leftpanel.add(bookscrollPane, BorderLayout.CENTER);
        leftpanel.add(buttonpanel, BorderLayout.SOUTH);


        String[] column1 = {"username","Studentname", "StudentID", "Bookname", "Status"};
        ordermodel = new DefaultTableModel(column1,0);
        Ordertable = new JTable(ordermodel);
        JScrollPane orderScrollPane = new JScrollPane(Ordertable);
        rightpanel.add(orderScrollPane, BorderLayout.CENTER);


        mainpanel.add(rightpanel);
        mainpanel.add(leftpanel);
        add(mainpanel, BorderLayout.CENTER);

        loadbook();
        loadorder(studentid);

        Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickCount++;

                if (clickCount == 1) {
                    if (resetTimer != null) {
                        resetTimer.stop();
                    }
                    resetTimer = new Timer(DOUBLE_CLICK_THRESHOLD, evt -> {
                        clickCount = 0;
                    });
                    resetTimer.setRepeats(false);
                    resetTimer.start();
                }

                if (clickCount == 1) {
                    Searchbook();
                } else if (clickCount == 2) {
                    bookmodel.setRowCount(0);
                    loadbook();
                    clickCount = 0;
                    if (resetTimer != null) {
                        resetTimer.stop();
                    }
                }
            }
        });

        Order.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Order(studentid,studentname,username);
            }
        });

    }

    private void loadbook() {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "select * from book";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String bookname = rs.getString("Bookname");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                bookmodel.addRow(new Object[]{bookname, author, category});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred during loading book");
        }
    }

    private void loadorder(String studentid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "select * from OrderBook where StudentID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,studentid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return;
            }
            do {
                String user = rs.getString("username");
                String name = rs.getString("Studentname");
                String id = rs.getString("StudentID");
                String book = rs.getString("Bookname");
                String status = rs.getString("Status");
                ordermodel.addRow(new Object[]{user, name, id, book, status});
            } while (rs.next());



        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred during loading order");
        }
    }

    private void Searchbook() {
        String book = searchfield.getText();

        if (book.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter bookname you need find");
            return;
        }

        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "select * from book where Bookname = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, book);

            ResultSet rs = ps.executeQuery();

            bookmodel.setRowCount(0);

            while (rs.next()) {
                String bookname = rs.getString("Bookname");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                bookmodel.addRow(new Object[]{bookname, author, category});
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Order(String studentid, String studentname, String username) {
        int row = Booktable.getSelectedRow();
        if (row != -1) {
            String book = bookmodel.getValueAt(row, 0).toString();
            String status = "Pending";

            java.sql.Date orderedDate = new java.sql.Date(System.currentTimeMillis());

            java.sql.Date returnedDate = new java.sql.Date(orderedDate.getTime() + (3 * 24 * 60 * 60 * 1000));

            try {
                Connection con = DatabaseConnection.getConnection();
                String check = "SELECT * FROM OrderBook WHERE StudentID = ? AND Bookname = ? AND Status = ?";
                PreparedStatement psCheck = con.prepareStatement(check);

                psCheck.setString(1, studentid);
                psCheck.setString(2, book);
                psCheck.setString(3, status);

                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "This book has already been ordered.");
                    return;
                }

                String sql = "INSERT INTO OrderBook (username, Studentname, StudentID, Bookname, Status, OrderedDate, ReturnedDate) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, username);
                ps.setString(2, studentname);
                ps.setString(3, studentid);
                ps.setString(4, book);
                ps.setString(5, status);
                ps.setDate(6, orderedDate);
                ps.setDate(7, returnedDate);

                ordermodel.addRow(new Object[]{username, studentname, studentid, book, status, orderedDate, returnedDate});

                int rowAffected = ps.executeUpdate();
                if (rowAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Ordered successfully");
                    ordermodel.setRowCount(0);
                    loadorder(studentid);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void main(String[] args) {
    }
}
