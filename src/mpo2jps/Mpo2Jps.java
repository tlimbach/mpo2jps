package mpo2jps;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Mpo2Jps implements ActionListener {

    private static final String MPO2JPS_CONVERTER_PROPERTIES = "MPO2JPS.properties";
    private JButton btnSelectDir;
    private JButton btnConvert;
    private JFrame frame;
    private JTextField text;
    private JLabel lblState;

    public Mpo2Jps() throws FileNotFoundException, IOException {
        initGui();
    }

    private void initGui() throws FileNotFoundException, IOException {
        frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setTitle("Mpo2Jps Converter");
        text = new JTextField(30);
        text.setToolTipText("Der ausgewählte Pfad");
        text.setEditable(false);
        File lastUsed = getLastUsed();

        if (lastUsed != null) {
            text.setText(lastUsed.getAbsolutePath());
        }

        frame.add(text);
        btnSelectDir = new JButton("...");
        btnSelectDir.setToolTipText("Verzeichnis auswählen mit MPO Dateien.");
        btnSelectDir.addActionListener(this);
        frame.add(btnSelectDir);

        btnConvert = new JButton("SideBySide (Jps) erstellen");
        btnConvert.addActionListener(this);
        frame.add(btnConvert);
        frame.add(lblState = new JLabel("Status          "));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnConvert) {
            try {
                setLastUsed();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            new Thread(() -> {
                convert();
            }).start();
        }

        if (source == btnSelectDir) {
            try {
                chooseDir();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private File getLastUsed() throws FileNotFoundException, IOException {
        Properties prop = new Properties();

        if (new File(MPO2JPS_CONVERTER_PROPERTIES).exists()) {

            File file;
            try (FileInputStream inStream = new FileInputStream(
                    MPO2JPS_CONVERTER_PROPERTIES)) {
                prop.load(inStream);
                file = new File(prop.getProperty("dir"));
            }

            if (file.exists()) {
                return file;
            } else {
                return null;
            }
        }

        return null;
    }

    private void setLastUsed() throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty("dir", text.getText());
        try (FileOutputStream out = new FileOutputStream(
                MPO2JPS_CONVERTER_PROPERTIES)) {
            prop.store(out, null);
            out.flush();
        }
    }

    private void chooseDir() throws FileNotFoundException, IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("MPO Verzeichnis öffnen");
        chooser.setDialogType(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(getLastUsed());
        chooser.showOpenDialog(frame);
        final File selectedFile = chooser.getSelectedFile();

        if (selectedFile != null) {
            text.setText(selectedFile.getAbsolutePath());
        }

    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void convert() {
        setCursor(Cursor.WAIT_CURSOR);

        File[] mpoFiles = new File(text.getText())
                .listFiles((File dir, String name) -> name.toLowerCase().endsWith("mpo"));

        for (int t = 0; t < mpoFiles.length; t++) {
            setProgress(t + " /" + mpoFiles.length);

            new SideBySideConverter(mpoFiles[t]);

            setProgress((t + 1) + " /" + mpoFiles.length);
        }

        JOptionPane.showMessageDialog(frame, "Fertig.");

        setCursor(Cursor.DEFAULT_CURSOR);
    }

    private void setProgress(final String state) {
        SwingUtilities.invokeLater(() -> {
            lblState.setText(state);
        });

    }

    public void setCursor(final int cursor) {
        SwingUtilities.invokeLater(() -> {
            frame.setCursor(Cursor.getPredefinedCursor(cursor));
        });

    }

    @SuppressWarnings({"UseSpecificCatch", "ResultOfObjectAllocationIgnored"})
    public static void main(String[] args) throws FileNotFoundException,
            IOException {

        try {
            UIManager
                    .setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // dont care
        }

        new Mpo2Jps();
    }

}
