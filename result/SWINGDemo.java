package com.mybank.gui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Sviatoslav Kalinuchuk
 */
public class SWINGDemo {
    
    private final JEditorPane log;
    private final JButton show;
    private final JButton report;
    private final JComboBox clients;
    private final JComboBox accounts;
    private final JButton deposit;
    private final JButton withdraw;
    private final JButton viewBalance;
    private final JFrame reportFrame;
    private final JEditorPane reportPane;
    
    public SWINGDemo() {
        log = new JEditorPane("text/html", "");
        log.setPreferredSize(new Dimension(550, 500));
        show = new JButton("Show");
        report = new JButton("Report");
        clients = new JComboBox();
        accounts = new JComboBox();
        deposit = new JButton("Deposit");
        withdraw = new JButton("Withdraw");
        viewBalance = new JButton("View Balance");
        
        // Initialize report window components
        reportFrame = new JFrame("Customer Report");
        reportPane = new JEditorPane("text/html", "");
        reportPane.setPreferredSize(new Dimension(600, 400));
        reportFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        reportFrame.add(reportPane, BorderLayout.CENTER);
        reportFrame.pack();
        reportFrame.setLocationRelativeTo(null);
        
        for (int i=0; i<Bank.getNumberOfCustomers();i++) {
            clients.addItem(Bank.getCustomer(i).getLastName()+", "+Bank.getCustomer(i).getFirstName());
        }
        
        // Add action listener for client selection
        clients.addActionListener((ActionEvent e) -> {
            updateAccountsList();
        });
        
        // Initial accounts list update
        updateAccountsList();
    }
    
    private void updateAccountsList() {
        accounts.removeAllItems();
        Customer current = Bank.getCustomer(clients.getSelectedIndex());
        for (int i = 0; i < current.getNumberOfAccounts(); i++) {
            String accType = current.getAccount(i) instanceof CheckingAccount ? "Checking" : "Savings";
            accounts.addItem("Account " + (i + 1) + " (" + accType + ")");
        }
    }
    
    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());
        
        // Top panel with client selection and main buttons
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 3));
        topPanel.add(clients);
        topPanel.add(show);
        topPanel.add(report);
        
        // Transaction panel
        JPanel transactionPanel = new JPanel(new FlowLayout());
        transactionPanel.add(new JLabel("Select Account:"));
        transactionPanel.add(accounts);
        transactionPanel.add(deposit);
        transactionPanel.add(withdraw);
        transactionPanel.add(viewBalance);
        
        // Combine panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(transactionPanel, BorderLayout.CENTER);
        
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(log, BorderLayout.CENTER);
        
        // Show button action
        show.addActionListener((ActionEvent e) -> {
            log.setText(""); // Clear the log field before showing new content
            Customer current = Bank.getCustomer(clients.getSelectedIndex());
            StringBuilder custInfo = new StringBuilder();
            custInfo.append("<br>&nbsp;<b><span style=\"font-size:2em;\">")
                    .append(current.getLastName()).append(", ")
                    .append(current.getFirstName())
                    .append("</span><br><hr>");
            
            for (int i = 0; i < current.getNumberOfAccounts(); i++) {
                String accType = current.getAccount(i) instanceof CheckingAccount ? "Checking" : "Savings";
                custInfo.append("&nbsp;<b>Account ").append(i + 1).append(":</b><br>")
                        .append("&nbsp;&nbsp;Type: ").append(accType).append("<br>")
                        .append("&nbsp;&nbsp;Balance: <span style=\"color:red;\">$")
                        .append(String.format("%.2f", current.getAccount(i).getBalance()))
                        .append("</span><br>");
                
                switch (current.getAccount(i)) {
                    case CheckingAccount checkingAccount -> custInfo.append("&nbsp;&nbsp;Overdraft: $")
                            .append(String.format("%.2f", checkingAccount.getOverdraftAmount()))
                            .append("<br>");
                    case SavingsAccount savingsAccount -> custInfo.append("&nbsp;&nbsp;Interest Rate: ")
                            .append(String.format("%.1f", savingsAccount.getInterestRate() * 100))
                            .append("%<br>");
                    default -> {
                    }
                }
                custInfo.append("<br>");
            }
            log.setText(custInfo.toString());
        });
        
        // Report button action
        report.addActionListener((ActionEvent e) -> {
            StringBuilder reportHtml = new StringBuilder();
            reportHtml.append("<html><body>");
            reportHtml.append("<h2>Customer Report</h2><hr>");
            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                Customer customer = Bank.getCustomer(i);
                reportHtml.append("<b>")
                    .append(customer.getLastName()).append(", ")
                    .append(customer.getFirstName()).append("</b><br>");
                for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                    String accType = customer.getAccount(j) instanceof CheckingAccount ? "Checking" : "Savings";
                    reportHtml.append("&nbsp;&nbsp;Account ").append(j + 1).append(": ")
                        .append(accType)
                        .append(", Balance: $")
                        .append(String.format("%.2f", customer.getAccount(j).getBalance()));
                    if (customer.getAccount(j) instanceof CheckingAccount) {
                        reportHtml.append(", Overdraft: $")
                            .append(String.format("%.2f", ((CheckingAccount)customer.getAccount(j)).getOverdraftAmount()));
                    } else if (customer.getAccount(j) instanceof SavingsAccount) {
                        reportHtml.append(", Interest Rate: ")
                            .append(String.format("%.1f", ((SavingsAccount)customer.getAccount(j)).getInterestRate() * 100))
                            .append("%");
                    }
                    reportHtml.append("<br>");
                }
                reportHtml.append("<hr>");
            }
            reportHtml.append("</body></html>");
            reportPane.setText(reportHtml.toString());
            reportFrame.setVisible(true);
        });
        
        // Deposit button action
        deposit.addActionListener((ActionEvent e) -> {
            String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to deposit:", "Deposit", JOptionPane.QUESTION_MESSAGE);
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        Customer current = Bank.getCustomer(clients.getSelectedIndex());
                        int accountIndex = accounts.getSelectedIndex();
                        current.getAccount(accountIndex).deposit(amount);
                        show.doClick(); // Refresh the display
                        JOptionPane.showMessageDialog(frame, "Deposit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Withdraw button action
        withdraw.addActionListener((ActionEvent e) -> {
            String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to withdraw:", "Withdraw", JOptionPane.QUESTION_MESSAGE);
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        Customer current = Bank.getCustomer(clients.getSelectedIndex());
                        int accountIndex = accounts.getSelectedIndex();
                        try {
                            current.getAccount(accountIndex).withdraw(amount);
                            show.doClick(); // Refresh the display
                            JOptionPane.showMessageDialog(frame, "Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(frame, "Insufficient funds!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // View Balance button action
        viewBalance.addActionListener((ActionEvent e) -> {
            Customer current = Bank.getCustomer(clients.getSelectedIndex());
            int accountIndex = accounts.getSelectedIndex();
            double balance = current.getAccount(accountIndex).getBalance();
            JOptionPane.showMessageDialog(frame, 
                String.format("Current balance: $%.2f", balance),
                "Account Balance",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        frame.setResizable(false);
        frame.setVisible(true);        
    }
    
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("com/mybank/gui/test.dat"))) {
            int numCustomers = Integer.parseInt(reader.readLine().trim());
            
            for (int i = 0; i < numCustomers; i++) {
                String line;
                // Skip blank lines before customer info
                while ((line = reader.readLine()) != null && line.trim().isEmpty()) {}
                if (line == null) break;
                String[] customerInfo = line.trim().split("\t");
                String firstName = customerInfo[0];
                String lastName = customerInfo[1];
                int numAccounts = Integer.parseInt(customerInfo[2]);
                
                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(i);
                
                for (int j = 0; j < numAccounts; j++) {
                    // Skip blank lines before account info
                    while ((line = reader.readLine()) != null && line.trim().isEmpty()) {}
                    if (line == null) break;
                    String[] accountInfo = line.trim().split("\t");
                    String accountType = accountInfo[0];
                    double balance = Double.parseDouble(accountInfo[1]);
                    double rateOrOverdraft = Double.parseDouble(accountInfo[2]);
                    
                    if (accountType.equals("S")) {
                        customer.addAccount(new SavingsAccount(balance, rateOrOverdraft));
                    } else if (accountType.equals("C")) {
                        customer.addAccount(new CheckingAccount(balance, rateOrOverdraft));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading test.dat: " + e.getMessage());
            System.exit(1);
        }
        
        SWINGDemo demo = new SWINGDemo();        
        demo.launchFrame();
    }
}