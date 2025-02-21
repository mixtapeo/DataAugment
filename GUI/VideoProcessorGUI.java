import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.*;

public class VideoProcessorGUI extends JFrame {
    private JTextArea outputArea;
    private JButton processButton;
    private JTextField inputPathField;
    private JTextField outputPathField;
    private JCheckBox folderProcessingCheck;
    private JSpinner angleSpinner;
    private JSpinner brightnessSpinner;
    private JComboBox<String> flipCombo;
    private JSpinner noiseSpinner;
    private JSpinner[] colorShiftSpinners;

    public VideoProcessorGUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Video Processing Tool");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Input Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        
        // Processing Mode
        folderProcessingCheck = new JCheckBox("Process Folder");
        folderProcessingCheck.addActionListener(e -> updatePathFields());
        addComponent(mainPanel, gbc, new JLabel("Processing Mode:"), 0, row);
        addComponent(mainPanel, gbc, folderProcessingCheck, 1, row++);

        // Input Path
        addComponent(mainPanel, gbc, new JLabel("Input Path:"), 0, row);
        inputPathField = new JTextField(30);
        JButton inputBrowse = new JButton("Browse");
        inputBrowse.addActionListener(e -> browseInputPath());
        addComponent(mainPanel, gbc, createPathPanel(inputPathField, inputBrowse), 1, row++);

        // Output Path
        addComponent(mainPanel, gbc, new JLabel("Output Path:"), 0, row);
        outputPathField = new JTextField(30);
        JButton outputBrowse = new JButton("Browse");
        outputBrowse.addActionListener(e -> browseOutputPath());
        addComponent(mainPanel, gbc, createPathPanel(outputPathField, outputBrowse), 1, row++);

        // Transformation Parameters
        addComponent(mainPanel, gbc, createSeparator("Transformations"), 0, row++, 2);

        // Rotation Angle
        angleSpinner = new JSpinner(new SpinnerNumberModel(45.0, -360.0, 360.0, 1.0));
        addParameter(mainPanel, gbc, "Rotation Angle (degrees):", angleSpinner, row++);

        // Brightness
        brightnessSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 5.0, 0.1));
        addParameter(mainPanel, gbc, "Brightness Factor:", brightnessSpinner, row++);

        // Flip
        flipCombo = new JComboBox<>(new String[]{"No Flip", "Horizontal", "Vertical", "Both"});
        addParameter(mainPanel, gbc, "Flip:", flipCombo, row++);

        // Gaussian Noise
        noiseSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        addParameter(mainPanel, gbc, "Noise (σ):", noiseSpinner, row++);

        // Color Shifts
        colorShiftSpinners = new JSpinner[3];
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        colorPanel.add(new JLabel("B:"));
        colorShiftSpinners[0] = new JSpinner(new SpinnerNumberModel(0, -255, 255, 1));
        colorPanel.add(colorShiftSpinners[0]);
        colorPanel.add(new JLabel("G:"));
        colorShiftSpinners[1] = new JSpinner(new SpinnerNumberModel(0, -255, 255, 1));
        colorPanel.add(colorShiftSpinners[1]);
        colorPanel.add(new JLabel("R:"));
        colorShiftSpinners[2] = new JSpinner(new SpinnerNumberModel(0, -255, 255, 1));
        colorPanel.add(colorShiftSpinners[2]);
        addParameter(mainPanel, gbc, "Color Shifts:", colorPanel, row++);

        // Process Button
        processButton = new JButton("Process Videos");
        processButton.addActionListener(this::processVideos);
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        addComponent(mainPanel, gbc, processButton, 0, row++);

        // Output Area
        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(mainPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void addComponent(JPanel panel, GridBagConstraints gbc, Component comp, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private void addComponent(JPanel panel, GridBagConstraints gbc, Component comp, int x, int y, int width) {
        gbc.gridwidth = width;
        addComponent(panel, gbc, comp, x, y);
        gbc.gridwidth = 1;
    }

    private void addParameter(JPanel panel, GridBagConstraints gbc, String label, Component field, int row) {
        addComponent(panel, gbc, new JLabel(label), 0, row);
        addComponent(panel, gbc, field, 1, row);
    }

    private JPanel createPathPanel(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private JComponent createSeparator(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        JLabel label = new JLabel(" " + text + " ");
        panel.add(label);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        return panel;
    }

    private void updatePathFields() {
        boolean folderMode = folderProcessingCheck.isSelected();
        inputPathField.setText("");
        outputPathField.setText(folderMode ? "" : "output.avi");
    }

    private void browseInputPath() {
        JFileChooser chooser = new JFileChooser();
        if (folderProcessingCheck.isSelected()) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("AVI Files", "avi"));
        }
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            inputPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseOutputPath() {
        JFileChooser chooser = new JFileChooser();
        if (folderProcessingCheck.isSelected()) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            chooser.setSelectedFile(new java.io.File("output.avi"));
        }
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void processVideos(ActionEvent e) {
        if (!validateInputs()) return;
        
        processButton.setEnabled(false);
        outputArea.setText("");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ProcessBuilder pb = new ProcessBuilder(buildCommand());
                pb.redirectErrorStream(true);

                try {
                    Process process = pb.start();
                    try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                        
                        String line;
                        while ((line = reader.readLine()) != null) {
                            publish(line + "\n");
                        }
                    }
                    
                    int exitCode = process.waitFor();
                    publish("\nProcess exited with code: " + exitCode);
                } catch (IOException | InterruptedException ex) {
                    publish("Error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(outputArea::append);
            }

            @Override
            protected void done() {
                processButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    private boolean validateInputs() {
        if (inputPathField.getText().isEmpty()) {
            showError("Input path cannot be empty!");
            return false;
        }
        
        if (outputPathField.getText().isEmpty()) {
            showError("Output path cannot be empty!");
            return false;
        }
        
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private String[] buildCommand() {
        boolean folderMode = folderProcessingCheck.isSelected();
        String flipCode = getFlipCode();
        
        return new String[] {
            "python",
            "main.py",
            folderMode ? "--folder" : "--input",
            inputPathField.getText(),
            outputPathField.getText(),
            "--angle", String.valueOf(angleSpinner.getValue()),
            "--brightness", String.valueOf(brightnessSpinner.getValue()),
            flipCode != null ? "--flip" : "",
            flipCode != null ? flipCode : "",
            "--gaussian", String.valueOf(noiseSpinner.getValue()),
            "--color_shift",
            String.valueOf(colorShiftSpinners[0].getValue()),
            String.valueOf(colorShiftSpinners[1].getValue()),
            String.valueOf(colorShiftSpinners[2].getValue())
        };
    }

    private String getFlipCode() {
        int selected = flipCombo.getSelectedIndex();
        return switch (selected) {
            case 1 -> "1";
            case 2 -> "0";
            case 3 -> "-1";
            default -> null;
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VideoProcessorGUI().setVisible(true);
        });
    }
}