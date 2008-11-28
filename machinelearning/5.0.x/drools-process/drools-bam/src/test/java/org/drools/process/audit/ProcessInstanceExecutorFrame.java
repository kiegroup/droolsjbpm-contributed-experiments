package org.drools.process.audit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.drools.WorkingMemory;

public class ProcessInstanceExecutorFrame extends JFrame {

    private static final long serialVersionUID = 400L;
    
    private WorkingMemory workingMemory;
    private JTextField processIdTextField;
    private JButton startButton;

    public ProcessInstanceExecutorFrame(WorkingMemory workingMemory) {
        this.workingMemory = workingMemory;
        setSize(new Dimension(200, 150));
        setTitle("Start Process");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initializeComponent();
    }

    private void initializeComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(panel, BorderLayout.CENTER);

        processIdTextField = new JTextField("com.sample.ruleflow");
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(processIdTextField, c);

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                start();
            }
        });
        c = new GridBagConstraints();
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(startButton, c);
    }
    
    private void start() {
        workingMemory.startProcess(processIdTextField.getText());
        workingMemory.fireAllRules();
    }
    
}
