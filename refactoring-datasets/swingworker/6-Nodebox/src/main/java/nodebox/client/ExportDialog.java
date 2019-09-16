package nodebox.client;

import nodebox.ui.ImageFormat;
import nodebox.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog presented when exporting a single image.
 */
public class ExportDialog extends JDialog {

    private boolean dialogSuccessful = false;
    private JComboBox formatBox;

    private JButton nextButton;

    public ExportDialog(Frame frame) {
        super(frame, "Export");
        setModal(true);
        setResizable(false);

        // Main
        setLayout(new BorderLayout(5, 5));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(new Theme.InsetsBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        // Format
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        formatPanel.add(new JLabel("Format:"));
        formatBox = new JComboBox();
        formatBox.addItem("SVG");
        formatBox.addItem("PNG");
        formatBox.addItem("PDF");
        formatBox.setSelectedItem("SVG");
        formatPanel.add(formatBox);
        mainPanel.add(formatPanel);

        mainPanel.add(Box.createVerticalGlue());

        // Buttons
        mainPanel.add(Box.createVerticalStrut(10));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 0));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                doCancel();
            }
        });
        buttonPanel.add(cancelButton);
        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                doNext();
            }
        });
        buttonPanel.add(nextButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        pack();
        getRootPane().setDefaultButton(nextButton);
    }

    private void doCancel() {
        setVisible(false);
    }

    private void doNext() {
        dialogSuccessful = true;
        setVisible(false);
    }

    public boolean isDialogSuccessful() {
        return dialogSuccessful;
    }

    public ImageFormat getFormat() {
        return ImageFormat.of(formatBox.getSelectedItem().toString());
    }

    public static void main(String[] args) {
        ExportDialog d = new ExportDialog(null);
        d.setVisible(true);
    }
}
