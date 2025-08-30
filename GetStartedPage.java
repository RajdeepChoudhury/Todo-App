package com.spark;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetStartedPage extends JFrame {
    private String username;
    private int userId;

    public GetStartedPage(String username) {
        this.username = username;
        this.userId = getUserId(username);  // Fetch user ID for TODO operations

        setTitle("Get Started");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTextArea description = new JTextArea("Welcome, " + username + "!\n\nThis is a simple TODO app. Manage your tasks easily.\nFeatures:\n- Create, Read, Update, Delete tasks\n- Set priorities and due dates\n- Mark as completed\n- Search tasks");
        description.setEditable(false);
        add(new JScrollPane(description), BorderLayout.CENTER);

        JButton getStartedButton = new JButton("Get Started");
        add(getStartedButton, BorderLayout.SOUTH);

        getStartedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new TodoPage(userId, username);
            }
        });

        setVisible(true);
    }

    private int getUserId(String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error fetching user ID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1;  // Error case
    }
}