package com.spark;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class TodoPage extends JFrame {
    private int userId;
    private String username;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField taskField, descField, dueDateField, priorityField, searchField;
    private JButton addButton, updateButton, deleteButton, completeButton, searchButton, logoutButton;

    public TodoPage(int userId, String username) {
        this.userId = userId;
        this.username = username;

        setTitle("TODO List for " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Task", "Description", "Due Date", "Priority", "Completed"}, 0);
        todoTable = new JTable(tableModel);
        add(new JScrollPane(todoTable), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Task:"));
        taskField = new JTextField();
        inputPanel.add(taskField);

        inputPanel.add(new JLabel("Description:"));
        descField = new JTextField();
        inputPanel.add(descField);

        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel("Priority (0-2):"));
        priorityField = new JTextField();
        inputPanel.add(priorityField);

        add(inputPanel, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        completeButton = new JButton("Mark Complete");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        logoutButton = new JButton("Logout");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial todos
        loadTodos("");

        // Action listeners
        addButton.addActionListener(e -> addTodo());
        updateButton.addActionListener(e -> updateTodo());
        deleteButton.addActionListener(e -> deleteTodo());
        completeButton.addActionListener(e -> markComplete());
        searchButton.addActionListener(e -> loadTodos(searchField.getText()));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        // Table row selection for update/delete
        todoTable.getSelectionModel().addListSelectionListener(event -> {
            if (todoTable.getSelectedRow() > -1) {
                taskField.setText(tableModel.getValueAt(todoTable.getSelectedRow(), 1).toString());
                descField.setText(tableModel.getValueAt(todoTable.getSelectedRow(), 2).toString());
                dueDateField.setText(tableModel.getValueAt(todoTable.getSelectedRow(), 3).toString());
                priorityField.setText(tableModel.getValueAt(todoTable.getSelectedRow(), 4).toString());
            }
        });

        setVisible(true);
    }

    private void loadTodos(String searchQuery) {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM todos WHERE user_id = ? AND task ILIKE ? ORDER BY priority DESC, due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("task"));
                row.add(rs.getString("description"));
                row.add(rs.getDate("due_date"));
                row.add(rs.getInt("priority"));
                row.add(rs.getBoolean("completed"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading todos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTodo() {
        if (taskField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Task cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO todos (user_id, task, description, due_date, priority) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, taskField.getText());
            pstmt.setString(3, descField.getText());
            pstmt.setDate(4, Date.valueOf(dueDateField.getText()));  // Assumes valid date
            pstmt.setInt(5, Integer.parseInt(priorityField.getText()));
            pstmt.executeUpdate();
            loadTodos("");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error adding todo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a todo to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int todoId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE todos SET task = ?, description = ?, due_date = ?, priority = ? WHERE id = ? AND user_id = ?")) {
            pstmt.setString(1, taskField.getText());
            pstmt.setString(2, descField.getText());
            pstmt.setDate(3, Date.valueOf(dueDateField.getText()));
            pstmt.setInt(4, Integer.parseInt(priorityField.getText()));
            pstmt.setInt(5, todoId);
            pstmt.setInt(6, userId);
            pstmt.executeUpdate();
            loadTodos("");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error updating todo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a todo to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int todoId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM todos WHERE id = ? AND user_id = ?")) {
            pstmt.setInt(1, todoId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            loadTodos("");
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error deleting todo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markComplete() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a todo to mark complete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int todoId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE todos SET completed = TRUE WHERE id = ? AND user_id = ?")) {
            pstmt.setInt(1, todoId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            loadTodos("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error marking complete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        taskField.setText("");
        descField.setText("");
        dueDateField.setText("");
        priorityField.setText("");
    }
}