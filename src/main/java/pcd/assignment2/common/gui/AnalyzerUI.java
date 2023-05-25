/*
 * Created by JFormDesigner on Tue Apr 11 21:23:00 CEST 2023
 */

package pcd.assignment2.common.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import pcd.assignment2.common.AnalysisStatsSnapshot;
import pcd.assignment2.common.LocEntry;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorenzo
 */
public class AnalyzerUI implements ActionListener {

//    private Controller controller;
    private int defaultProcCoresMultiplier;
    private boolean isProcCoresMultiplierInputAllowed;
    private String chosenDirPath;

    private DefaultTableModel topResultsTableModel;
    private DefaultTableModel distributionTableModel;

    private List<InputListener> listeners;

    public AnalyzerUI(String defRootDir, int defMaxLoC, int defNBands, int defNTopFiles, int defaultProcCoresMultiplier, boolean isProcCoresMultiplierInputAllowed) {
        this.listeners = new ArrayList<>();
//        this.controller = controller;
        this.defaultProcCoresMultiplier = defaultProcCoresMultiplier;
        this.isProcCoresMultiplierInputAllowed = isProcCoresMultiplierInputAllowed;
        this.topResultsTableModel = new DefaultTableModel();
        this.distributionTableModel = new DefaultTableModel();
        initComponents();
        initTableModels();
        initTableLayout();
        initProcCoresMultiplierInput(defaultProcCoresMultiplier, isProcCoresMultiplierInputAllowed);
        // initializing default values
        chosenDirPath = defRootDir;
        selectedDir.setText(defRootDir);
        maxLines.setText(String.valueOf(defMaxLoC));
        numIntervals.setText(String.valueOf(defNBands));
        snapshotSize.setText(String.valueOf(defNTopFiles));

        chooseDir.addActionListener(this);
        start.addActionListener(this);
        stop.addActionListener(this);
//        AnalyzerUI.setVisible(true);
    }

    private void initTableLayout() {
        topResults.getTableHeader().setReorderingAllowed(false);
        distribution.getTableHeader().setReorderingAllowed(false);
        topResults.getParent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (topResults.getPreferredSize().width < topResults.getParent().getWidth()) {
                    topResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    topResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });
        distribution.getParent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (distribution.getPreferredSize().width < distribution.getParent().getWidth()) {
                    distribution.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    distribution.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == chooseDir) {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.setDialogTitle("Choose a directory...");
//            fileChooser.setFileFilter(new FolderFilter());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int choice = fileChooser.showOpenDialog(AnalyzerUI);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File chosenDir = fileChooser.getSelectedFile();
                chosenDirPath = chosenDir.getAbsolutePath();
                String displayedString = chosenDirPath.length() >= 70 ? "..." + chosenDirPath.substring(chosenDirPath.length() - 70):
                        chosenDirPath;
                selectedDir.setText(displayedString);
                start.setEnabled(true);
            }
        } else if (source == start) {
            String maxLinesParam = maxLines.getText();
            String numIntervalsParam = numIntervals.getText();
            String snapshotSizeParam = snapshotSize.getText();
            String procCoresMultiplierParam = procCoresMultiplier.getText();
            if (!maxLinesParam.isEmpty() && !numIntervalsParam.isEmpty() && !snapshotSizeParam.isEmpty() && !procCoresMultiplierParam.isEmpty()) { // FIXME: lacks proper input validation
                int maxLinesInt = Integer.parseInt(maxLinesParam);
                int numIntervalsInt =  Integer.parseInt(numIntervalsParam);
                int snapshotSizeInt = Integer.parseInt(snapshotSizeParam);
                int procCoresMultiplierInt = Integer.parseInt(procCoresMultiplierParam);
                if (procCoresMultiplierInt > 0) {
                    this.start.setEnabled(false);
                    this.stop.setEnabled(true);
                    this.chooseDir.setEnabled(false);
                    this.maxLines.setEnabled(false);
                    this.numIntervals.setEnabled(false);
                    this.snapshotSize.setEnabled(false);
                    this.procCoresMultiplier.setEnabled(false);
                    this.status.setText("");
                    this.notifyStarted(chosenDirPath,snapshotSizeInt, numIntervalsInt, maxLinesInt);
                } else {
                    this.procCoresMultiplier.setText(String.valueOf(this.defaultProcCoresMultiplier));
                }
            }
        } else if (source == stop) {
            this.notifyStopped();
            this.reset();
        }
    }

    public void addListener(InputListener listener) {
        listeners.add(listener);
    }
    public void updateStats(AnalysisStatsSnapshot statsSnapshot) {
        SwingUtilities.invokeLater(() -> {
            this.updateTopResultsTable(statsSnapshot.getRank());
            this.updateDistributionTable(statsSnapshot);
        });
    }

    public void updateStatus(String text) {
        SwingUtilities.invokeLater(() -> this.status.append(text + "\r\n"));
    }

    public void display() {
        SwingUtilities.invokeLater(() -> AnalyzerUI.setVisible(true));
    }

    public void reset() {
        SwingUtilities.invokeLater(() -> {
                    this.start.setEnabled(true);
                    this.stop.setEnabled(false);
                    this.chooseDir.setEnabled(true);
                    this.maxLines.setEnabled(true);
                    this.numIntervals.setEnabled(true);
                    this.snapshotSize.setEnabled(true);
                    this.procCoresMultiplier.setEnabled(this.isProcCoresMultiplierInputAllowed);
                });
//        this.status.setText("");
    }

    private void notifyStarted(String selectedDirPath, int maxFiles, int nBands, int maxLoc) {
        listeners.forEach(l -> l.started(selectedDirPath, maxFiles, nBands, maxLoc));
    }

    private void notifyStopped() {
        listeners.forEach(l -> l.stopped());
    }

    private void initTableModels() {
        this.topResultsTableModel.addColumn("File");
        this.topResultsTableModel.addColumn("# Lines");
        this.topResults.setModel(topResultsTableModel);

        this.distributionTableModel.addColumn("Range");
        this.distributionTableModel.addColumn("# Files");
        this.distribution.setModel(distributionTableModel);
    }

    private void initProcCoresMultiplierInput(int defaultProcCoresMultiplier, boolean isProcCoresMultiplierInputAllowed) {
        this.procCoresMultiplier.setText(String.valueOf(defaultProcCoresMultiplier));
        this.procCoresMultiplier.setEnabled(isProcCoresMultiplierInputAllowed);
    }


    private void updateTopResultsTable(LocEntry[] entries) {
        topResultsTableModel.setRowCount(0);
        for (LocEntry entry : entries) {
            this.topResultsTableModel.addRow(new Object[]{entry.getSrcPathRelativeToRoot(), entry.getNLoc()});
        }
    }

    private void updateDistributionTable(AnalysisStatsSnapshot snapshot) {
        distributionTableModel.setRowCount(0);
        int[] bands = snapshot.getBands();
        int nLocPerBand = snapshot.getMaxLoc() / (bands.length - 1);
        int a = 0;
        int b = 0;
        for (int i = 0; i < bands.length - 1; i++) {
            b = a + nLocPerBand - 1;
            this.distributionTableModel.addRow(new Object[]{"["+ a + " - " + b + "]", bands[i]});
            a = b + 1;
        }
        this.distributionTableModel.addRow(new Object[]{" >= " + snapshot.getMaxLoc(), bands[bands.length - 1]});
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        AnalyzerUI = new JFrame();
        label1 = new JLabel();
        maxLines = new JTextField();
        start = new JButton();
        scrollPane1 = new JScrollPane();
        status = new JTextArea();
        label3 = new JLabel();
        numIntervals = new JTextField();
        stop = new JButton();
        label2 = new JLabel();
        snapshotSize = new JTextField();
        label4 = new JLabel();
        procCoresMultiplier = new JTextField();
        chooseDir = new JButton();
        selectedDir = new JTextField();
        label5 = new JLabel();
        separator1 = new JSeparator();
        scrollPane3 = new JScrollPane();
        topResults = new JTable();
        scrollPane4 = new JScrollPane();
        distribution = new JTable();

        //======== AnalyzerUI ========
        {
            AnalyzerUI.setTitle("Source Code Analyzer - Executors Version");
            AnalyzerUI.setResizable(false);
            AnalyzerUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            var AnalyzerUIContentPane = AnalyzerUI.getContentPane();
            AnalyzerUIContentPane.setLayout(new FormLayout(
                "$rgap, $lcgap, default, $lcgap, 44dlu, $lcgap, 19dlu, $lcgap, 79dlu, $lcgap, 67dlu, 5*($lcgap, default), $lcgap, 260dlu, 2*($lcgap, default), $lcgap, $rgap",
                "$rgap, $lgap, 17dlu, 2*($lgap, default), $lgap, $rgap, $lgap, default, $lgap, $ugap, $lgap, default, $lgap, $ugap, 2*($lgap), default, $ugap, $lgap, 76dlu, $lgap, 121dlu, $lgap, $rgap"));

            //---- label1 ----
            label1.setText("Max number of lines");
            AnalyzerUIContentPane.add(label1, CC.xy(3, 3));
            AnalyzerUIContentPane.add(maxLines, CC.xy(5, 3));

            //---- start ----
            start.setText("Start");
            start.setEnabled(true);
            AnalyzerUIContentPane.add(start, CC.xy(9, 3));

            //======== scrollPane1 ========
            {

                //---- status ----
                status.setEditable(false);
                scrollPane1.setViewportView(status);
            }
            AnalyzerUIContentPane.add(scrollPane1, CC.xywh(21, 3, 8, 9));

            //---- label3 ----
            label3.setText("Number of intervals");
            AnalyzerUIContentPane.add(label3, CC.xy(3, 5));
            AnalyzerUIContentPane.add(numIntervals, CC.xy(5, 5));

            //---- stop ----
            stop.setText("Stop");
            stop.setEnabled(false);
            AnalyzerUIContentPane.add(stop, CC.xy(9, 5));

            //---- label2 ----
            label2.setText("Number of results to display");
            AnalyzerUIContentPane.add(label2, CC.xy(3, 7));
            AnalyzerUIContentPane.add(snapshotSize, CC.xy(5, 7));

            //---- label4 ----
            label4.setText("Available processor cores multiplier");
            AnalyzerUIContentPane.add(label4, CC.xy(3, 11));
            AnalyzerUIContentPane.add(procCoresMultiplier, CC.xy(5, 11));

            //---- chooseDir ----
            chooseDir.setText("Select directory");
            AnalyzerUIContentPane.add(chooseDir, CC.xy(3, 15));

            //---- selectedDir ----
            selectedDir.setEnabled(false);
            AnalyzerUIContentPane.add(selectedDir, CC.xywh(5, 15, 7, 1));

            //---- label5 ----
            label5.setText("NOTE: Displayed paths are relative to the selected folder.");
            AnalyzerUIContentPane.add(label5, CC.xy(23, 15));
            AnalyzerUIContentPane.add(separator1, CC.xywh(1, 20, 29, 1));

            //======== scrollPane3 ========
            {
                scrollPane3.setViewportView(topResults);
            }
            AnalyzerUIContentPane.add(scrollPane3, CC.xywh(3, 23, 9, 3));

            //======== scrollPane4 ========
            {
                scrollPane4.setViewportView(distribution);
            }
            AnalyzerUIContentPane.add(scrollPane4, CC.xywh(23, 23, 5, 3));
            AnalyzerUI.pack();
            AnalyzerUI.setLocationRelativeTo(AnalyzerUI.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JFrame AnalyzerUI;
    private JLabel label1;
    private JTextField maxLines;
    private JButton start;
    private JScrollPane scrollPane1;
    private JTextArea status;
    private JLabel label3;
    private JTextField numIntervals;
    private JButton stop;
    private JLabel label2;
    private JTextField snapshotSize;
    private JLabel label4;
    private JTextField procCoresMultiplier;
    private JButton chooseDir;
    private JTextField selectedDir;
    private JLabel label5;
    private JSeparator separator1;
    private JScrollPane scrollPane3;
    private JTable topResults;
    private JScrollPane scrollPane4;
    private JTable distribution;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private class FolderFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Directories";
        }
    }

}
