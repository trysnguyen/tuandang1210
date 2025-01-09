import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BorrowHistory extends JFrame {
    private JTable History;
    private DefaultTableModel tableModel;
    private JButton returnB, SearchB, deleteB, addB, rdateB;
    private JLabel borrowid, studentid, stname, bname, bdate, rdate;
    private JTextField borrowF, stidF, stnameF, bnameF, bdateF, rdateF,SearchF;
    private int clickCount = 0;
    private static final int DOUBLE_CLICK_THRESHOLD = 500;
    private static Timer resetTimer;

    public BorrowHistory() {
        setTitle("Book borrowing history");
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);



        JPanel Lpanel = new JPanel(new GridBagLayout());


        JLabel SearchL = new JLabel("Search Bookname/StudentID");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        Lpanel.add(SearchL, gbc);

        SearchF = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridy = 0;
        Lpanel.add(SearchF, gbc);

        String[] column = {"BorrowID", "StudentID", "Studentname", "Bookname", "BorrowDate", "ReturnDate"};
        tableModel = new DefaultTableModel(column,0);
        History = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(History);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        Lpanel.add(scrollPane, gbc);

        SearchB = new JButton("Search StudentID/Bookname");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        Lpanel.add(SearchB, gbc);

        returnB = new JButton("Return");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        Lpanel.add(returnB,gbc);

        deleteB = new JButton("Delete");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        Lpanel.add(deleteB, gbc);

        gbc.insets = new Insets(0, 83, 0, 0);
        rdateB = new JButton("Returned Book");
        gbc.gridx = 0;
        gbc.gridy = 4;
        Lpanel.add(rdateB, gbc);

        JPanel Rpanel = new JPanel(new GridBagLayout());
        gbc.insets = new Insets(5, 5, 5, 5);

        studentid = new JLabel("StudentID");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        Rpanel.add(studentid,gbc);
        stidF = new JTextField(20);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        Rpanel.add(stidF,gbc);

        stname = new JLabel("Studentname");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        Rpanel.add(stname,gbc);
        stnameF = new JTextField(20);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        Rpanel.add(stnameF,gbc);

        bname = new JLabel("Bookname");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        Rpanel.add(bname,gbc);
        bnameF = new JTextField(20);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        Rpanel.add(bnameF,gbc);

        bdate = new JLabel("Borrowdate");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        Rpanel.add(bdate,gbc);
        bdateF = new JTextField(20);
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        Rpanel.add(bdateF,gbc);

        addB = new JButton("ADD");
        gbc.gridx = 0;
        gbc.gridy = 5;
        Rpanel.add(addB,gbc);


        add(Rpanel);
        add(Lpanel, BorderLayout.EAST);
        loadinghistory();

        SearchB.addActionListener(new ActionListener() {
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
                    tableModel.setRowCount(0);
                    loadinghistory();
                    clickCount = 0;
                    if (resetTimer != null) {
                        resetTimer.stop();
                    }
                }
            }
        });

        deleteB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Delete();
            }
        });

        addB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Add();
            }
        });

        returnB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Return();
            }
        });

        rdateB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Returned();
            }
        });
    }

    private void loadinghistory() {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "select * from BorrowHistory order by BorrowID asc";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int borrowid = rs.getInt("BorrowID");
                String studentid = rs.getString("StudentID");
                String studentname = rs.getString("Studentname");
                String bookname = rs.getString("Bookname");
                String bdate = rs.getString("BorrowDate");
                String rdate = rs.getString("ReturnDate");
                tableModel.addRow(new Object[]{borrowid,studentid,studentname,bookname,bdate,rdate});
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Searchbook() {
        String book = SearchF.getText();

        if (book.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter bookname you need find");
            return;
        }

        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "select * from BorrowHistory where Bookname = ? or StudentID = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, book);
            ps.setString(2, book);

            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                int borrowid = rs.getInt("BorrowID");
                String studentid = rs.getString("StudentID");
                String studentname = rs.getString("Studentname");
                String bookname = rs.getString("Bookname");
                String bdate = rs.getString("BorrowDate");
                String rdate = rs.getString("ReturnDate");
                tableModel.addRow(new Object[]{borrowid,studentid,studentname,bookname,bdate,rdate});

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void Delete() {
        int selectedRow = History.getSelectedRow();

        if (selectedRow != -1) {
            int borrowid = (int) History.getValueAt(selectedRow, 0);

            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete?");
            if (confirmation == JOptionPane.YES_OPTION) {
                try (Connection con = DatabaseConnection.getConnection()) {
                    String query = "DELETE FROM BorrowHistory WHERE BorrowID = ?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, borrowid);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        String updateQuery = "UPDATE BorrowHistory SET BorrowID = BorrowID - 1 WHERE BorrowID > ?";
                        PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                        updateStmt.setInt(1, borrowid);
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "deleted successfully");
                        tableModel.setRowCount(0);
                        loadinghistory();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error deleting");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select to delete");
        }
    }

    private void Add() {
        int borrowid = tableModel.getRowCount() + 1;
        String stid = stidF.getText();
        String name = stnameF.getText();
        String book = bnameF.getText();
        String bdate = bdateF.getText();

        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "INSERT into BorrowHistory (BorrowID, StudentID, Studentname, Bookname, BorrowDate) values (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, borrowid);
            ps.setString(2,stid);
            ps.setString(3,name);
            ps.setString(4,book);
            ps.setString(5,bdate);

            int rowaffected = ps.executeUpdate();
            if (rowaffected>0) {
                JOptionPane.showMessageDialog(this, "Added successfully");
                tableModel.setRowCount(0);
                loadinghistory();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding");
        }
    }

    private void Return() {
        new AdminFrame().setVisible(true);
        this.dispose();
    }

    private void Returned() {
        int row = History.getSelectedRow();
         if (row != -1) {
             int id = (int) History.getValueAt(row,0);

             try {
                 Connection con = DatabaseConnection.getConnection();
                 String sql = "update BorrowHistory set ReturnDate = GETDATE() where BorrowID = ?";
                 PreparedStatement ps = con.prepareStatement(sql);

                ps.setInt(1,id);

                int rowaffected = ps.executeUpdate();
                if (rowaffected > 0) {
                    JOptionPane.showMessageDialog(this, "Update successfully");
                    tableModel.setRowCount(0);
                    loadinghistory();
                }

             } catch (Exception e) {
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Returndate is not valid");
             }
         }
    }

    public static void main(String[] args) {
        new BorrowHistory().setVisible(true);
    }
}
