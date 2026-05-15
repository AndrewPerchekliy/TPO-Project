/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad3;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private JFrame frame;
    private DefaultListModel<TaskWrapper> listModel;
    private JList<TaskWrapper> taskList;
    private JButton addTaskButton;
    private JButton cancelTaskButton;
    private JButton checkStateButton;
    private JButton showResultButton;
    private JTextArea resultArea;
    private ExecutorService executor;
    
    public Main() {
        executor = Executors.newFixedThreadPool(5);
        initializeGUI();
    }
    
    private void initializeGUI() {
        frame = new JFrame("Zarządzanie zadaniami");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Panel główny
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Lista zadań
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskListCellRenderer());
        
        JScrollPane listScrollPane = new JScrollPane(taskList);
        listScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        addTaskButton = new JButton("Dodaj zadanie");
        addTaskButton.addActionListener(new AddTaskListener());
        
        cancelTaskButton = new JButton("Anuluj zadanie");
        cancelTaskButton.addActionListener(new CancelTaskListener());
        cancelTaskButton.setEnabled(false);
        
        checkStateButton = new JButton("Sprawdź stan");
        checkStateButton.addActionListener(new CheckStateListener());
        checkStateButton.setEnabled(false);
        
        showResultButton = new JButton("Pokaż wynik");
        showResultButton.addActionListener(new ShowResultListener());
        showResultButton.setEnabled(false);
        
        buttonPanel.add(addTaskButton);
        buttonPanel.add(cancelTaskButton);
        buttonPanel.add(checkStateButton);
        buttonPanel.add(showResultButton);
        
        // Obszar wyników
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        
        // Listener dla wyboru z listy
        taskList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean hasSelection = taskList.getSelectedValue() != null;
                cancelTaskButton.setEnabled(hasSelection);
                checkStateButton.setEnabled(hasSelection);
                showResultButton.setEnabled(hasSelection);
            }
        });
        
        // Układ komponentów
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Lista zadań:"), BorderLayout.NORTH);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Wyniki i informacje:"), BorderLayout.NORTH);
        rightPanel.add(resultScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    private void addTask(String text, int count) {
        StringTask task = new StringTask(text, count);
        String taskName = "Zadanie " + (listModel.getSize() + 1);
        String description = "Tekst: \"" + text + "\", Powtórzenia: " + count;
        
        Future<String> future = executor.submit(task);
        TaskWrapper wrapper = new TaskWrapper(future, taskName, description);
        
        listModel.addElement(wrapper);
        resultArea.append("Dodano: " + taskName + " - " + description + "\n");
        
        // Aktualizuj listę co sekundę, aby pokazywać aktualne stany
        SwingUtilities.invokeLater(() -> {
            taskList.repaint();
        });
    }
    
    private class AddTaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JDialog dialog = new JDialog(frame, "Dodaj nowe zadanie", true);
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            JTextField textField = new JTextField(20);
            JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10));
            
            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Tekst:"), gbc);
            gbc.gridx = 1;
            dialog.add(textField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("Liczba powtórzeń:"), gbc);
            gbc.gridx = 1;
            dialog.add(countSpinner, gbc);
            
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Anuluj");
            
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            dialog.add(buttonPanel, gbc);
            
            okButton.addActionListener(ev -> {
                String text = textField.getText();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tekst nie może być pusty!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int count = (Integer) countSpinner.getValue();
                addTask(text, count);
                dialog.dispose();
            });
            
            cancelButton.addActionListener(ev -> dialog.dispose());
            
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }
    
    private class CancelTaskListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TaskWrapper selected = taskList.getSelectedValue();
            if (selected != null) {
                boolean cancelled = selected.getFuture().cancel(true);
                if (cancelled) {
                    resultArea.append("Anulowano: " + selected.getTaskName() + "\n");
                } else {
                    resultArea.append("Nie można anulować: " + selected.getTaskName() + 
                        " (zadanie już zakończone)\n");
                }
                taskList.repaint();
            }
        }
    }
    
    private class CheckStateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TaskWrapper selected = taskList.getSelectedValue();
            if (selected != null) {
                Future<?> future = selected.getFuture();
                String status = selected.getStatus();
                String stateInfo = "Zadanie: " + selected.getTaskName() + "\n";
                stateInfo += "Stan: " + status + "\n";
                stateInfo += "Anulowane: " + (future.isCancelled() ? "Tak" : "Nie") + "\n";
                stateInfo += "Zakończone: " + (future.isDone() ? "Tak" : "Nie") + "\n";
                stateInfo += "Opis: " + selected.getDescription() + "\n";
                stateInfo += "---\n";
                
                resultArea.append(stateInfo);
            }
        }
    }
    
    private class ShowResultListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TaskWrapper selected = taskList.getSelectedValue();
            if (selected != null) {
                Future<?> future = selected.getFuture();
                if (future.isDone() && !future.isCancelled()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Future<String> stringFuture = (Future<String>) future;
                        String result = stringFuture.get();
                        String resultInfo = "Zadanie: " + selected.getTaskName() + "\n";
                        resultInfo += "Wynik (" + result.length() + " znaków):\n";
                        if (result.length() > 200) {
                            resultInfo += result.substring(0, 200) + "...\n";
                        } else {
                            resultInfo += result + "\n";
                        }
                        resultInfo += "---\n";
                        resultArea.append(resultInfo);
                    } catch (Exception ex) {
                        resultArea.append("Error podczas pobierania wyniku: " + 
                            ex.getMessage() + "\n");
                    }
                } else if (future.isCancelled()) {
                    resultArea.append("Zadanie " + selected.getTaskName() + 
                        " zostało anulowane - brak wyniku\n");
                } else {
                    resultArea.append("Zadanie " + selected.getTaskName() + 
                        " jeszcze się wykonuje - wynik niedostępny\n");
                }
            }
        }
    }
    
    // Renderer dla listy zadań - koloruje zadania według stanu
    private class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof TaskWrapper) {
                TaskWrapper wrapper = (TaskWrapper) value;
                String status = wrapper.getStatus();
                
                if (!isSelected) {
                    switch (status) {
                        case "GOTOWE":
                            setBackground(new Color(200, 255, 200)); // Zielony
                            break;
                        case "ANULOWANE":
                            setBackground(new Color(255, 200, 200)); // Czerwony
                            break;
                        case "W TRAKCIE":
                            setBackground(new Color(255, 255, 200)); // Żółty
                            break;
                    }
                }
            }
            
            return this;
        }
    }
    
    public void show() {
        frame.setVisible(true);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.show();
            
            // Zamknij executor przy zamykaniu aplikacji
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                app.shutdown();
            }));
        });
    }
}
