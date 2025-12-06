/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.filechooser.FileSystemView;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.swing.table.DefaultTableModel;



/**
 *
 * @author Mark
 */
public class Dashboard extends javax.swing.JFrame {
    private javax.swing.Timer driveTimer;
    private final Map<String, File> driveMap = new HashMap<>();
    private BackupWorker currentBackupWorker = null;
    private static final String BACKUP_FOLDER_PATH = BackupConfig.BACKUP_FOLDER_PATH;
    
    /**
     * Creates new form Dashboard
     * 
     *
     * @param current
     * @param total
     */
    
    public void updateProgress(int current, int total) {
        jProgressBar1.setValue(current);
        jProgressBar1.setString(current + " / " + total + " files");
    }

    public void setProgressStatus(String status) {
        jProgressBar1.setString(status);
    }

    public void resetBackupButton() {
        jButton4.setEnabled(true);
        jButton5.setEnabled(false);
        jButton5.setText("CANCEL");
    }

    public void refreshBackupHistory() {
        loadBackupHistory();
    }
    
    public void resetProgressBar() {
        jProgressBar1.setValue(0);      
        jProgressBar1.setString("");
    }

    
    private void updateDrives() {
        String previousSelection = (String) detection.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        model.addElement("Select a device");

        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] drives = File.listRoots();
        driveMap.clear();

        for (File drive : drives) {
            String type = fsv.getSystemTypeDescription(drive);
            String name = fsv.getSystemDisplayName(drive);

            if (name == null || name.trim().isEmpty()) {
                name = drive.getAbsolutePath();
            }

            // Only add non-local drives (e.g., exclude "Local Disk" but include "Removable Disk", "CD-ROM", etc.)
            if (type == null || !type.toLowerCase().contains("local")) {
                String displayName = name;
                if (type != null && type.toLowerCase().contains("removable")) {
                    displayName += "(Flash Drive)";  
                } else if (type != null) {
                    displayName += " (" + type + ")";
                }

                model.addElement(displayName);
                driveMap.put(displayName, drive);
            }
        }

        detection.setModel(model);

        if (previousSelection != null) {
            detection.setSelectedItem(previousSelection);
        }
    }
    
    private void updateAllCheckboxes() {
        boolean selectAll = SelectAllCheckBox.isSelected();
        VideosCheckBox.setSelected(selectAll);
        ImagesCheckBox.setSelected(selectAll);
        PdfCheckBox.setSelected(selectAll);
        DocxCheckBox.setSelected(selectAll);
        XlsCheckBox.setSelected(selectAll);
        PptCheckBox.setSelected(selectAll);
        MusicCheckBox.setSelected(selectAll);
    }

    
    private void updateFileCount(){
        String selected = (String) detection.getSelectedItem();
        if (selected == null || "Select a device".equals(selected)){
            return;
        }

        File selectedDrive = driveMap.get(selected);
        if (selectedDrive == null) {
            return;
        }

        int[] fileCounts = new int[7];
        long[] fileSizes = new long[7];

        countFilesRecursively(selectedDrive, fileCounts, fileSizes);

        // Update labels
        label2.setText(String.valueOf(fileCounts[0]));
        label10.setText(FileUtils.formatSize(fileSizes[0]));
        label4.setText(String.valueOf(fileCounts[1]));
        label11.setText(FileUtils.formatSize(fileSizes[1]));
        label5.setText(String.valueOf(fileCounts[2]));
        label12.setText(FileUtils.formatSize(fileSizes[2]));
        label6.setText(String.valueOf(fileCounts[3]));
        label13.setText(FileUtils.formatSize(fileSizes[3]));
        label7.setText(String.valueOf(fileCounts[4]));
        label14.setText(FileUtils.formatSize(fileSizes[4]));
        label8.setText(String.valueOf(fileCounts[5]));
        label15.setText(FileUtils.formatSize(fileSizes[5]));
        label9.setText(String.valueOf(fileCounts[6]));
        label16.setText(FileUtils.formatSize(fileSizes[6]));
    }

    private void countFilesRecursively(File directory, int[] counts, long[] sizes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
           if (file.isDirectory()) {
                countFilesRecursively(file, counts, sizes);
            } else {
                String name = file.getName().toLowerCase();
                
                //videos
                if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv")) {
                    counts[0]++;
                    sizes[0] += file.length();
                    }
                    
                //images
                else if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                    counts[1]++;
                    sizes[1] += file.length();
                }
                
                //pdf
                else if (name.endsWith(".pdf")) {
                    counts[2]++;
                    sizes[2] += file.length();
                }
                
                //docx
                else if (name.endsWith(".docx") || name.endsWith(".doc")) {
                    counts[3]++;
                    sizes[3] += file.length();
                }

                //xlxs
                else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                    counts[4]++;
                    sizes[4] += file.length();
                }
                
                //ppt
                else if (name.endsWith(".pptx") || name.endsWith(".ppt")) {
                    counts[5]++;
                    sizes[5] += file.length();
                }
                
                //music
                else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac")) {
                    counts[6]++;
                    sizes[6] += file.length();
                }
            }
        }
    }
    
    public List<String> getSelectedFileTypes() {
        List<String> selectedTypes = new ArrayList<>();
        if (VideosCheckBox.isSelected()) selectedTypes.add("video");
        if (ImagesCheckBox.isSelected()) selectedTypes.add("image");
        if (PdfCheckBox.isSelected()) selectedTypes.add("pdf");
        if (DocxCheckBox.isSelected()) selectedTypes.add("docx");
        if (XlsCheckBox.isSelected()) selectedTypes.add("xlsx");
        if (PptCheckBox.isSelected()) selectedTypes.add("ppt");
        if (MusicCheckBox.isSelected()) selectedTypes.add("music");
        return selectedTypes;
    }
      
    public Dashboard() {
        initComponents();
        
        BackupConfig.initializeDirectories();
       
        pathplaceHolder.setText(BackupConfig.BACKUP_FOLDER_PATH);
        pathplaceHolder.setEditable(false);
        
        loadBackupHistory();
        
        File backupDir = new File(BACKUP_FOLDER_PATH);
        if (!backupDir.exists()) {
            backupDir.mkdirs(); // Creates all necessary parent directories
        }

        
        pathplaceHolder.setText(BACKUP_FOLDER_PATH);
        pathplaceHolder.setEditable(false); // Make it read-only

        // Optionally disable the ellipsis button since path is fixed???
        EllipsisBtnActionPerformed.setEnabled(false);
        
        detection.addPopupMenuListener(new javax.swing.event.PopupMenuListener(){
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
               driveTimer.stop();
            }
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e){
                driveTimer.start();
            }
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e){
                driveTimer.start();
            }
        });
        
        driveTimer = new javax.swing.Timer(100, e -> updateDrives());
        driveTimer.start();
    }
    
    private void loadBackupHistory() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        
        List<BackupEntry> entries = BackupHistoryManager.loadBackupHistory();
        for (BackupEntry entry : entries) {
            model.addRow(entry.toTableRow());
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        SelectAllCheckBox = new javax.swing.JCheckBox();
        VideosCheckBox = new javax.swing.JCheckBox();
        ImagesCheckBox = new javax.swing.JCheckBox();
        PdfCheckBox = new javax.swing.JCheckBox();
        DocxCheckBox = new javax.swing.JCheckBox();
        XlsCheckBox = new javax.swing.JCheckBox();
        PptCheckBox = new javax.swing.JCheckBox();
        MusicCheckBox = new javax.swing.JCheckBox();
        label3 = new java.awt.Label();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label4 = new java.awt.Label();
        label5 = new java.awt.Label();
        label6 = new java.awt.Label();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        label9 = new java.awt.Label();
        label10 = new java.awt.Label();
        label11 = new java.awt.Label();
        label12 = new java.awt.Label();
        label13 = new java.awt.Label();
        label14 = new java.awt.Label();
        label15 = new java.awt.Label();
        label16 = new java.awt.Label();
        jButton4 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton5 = new javax.swing.JButton();
        detection = new javax.swing.JComboBox<>();
        pathplaceHolder = new javax.swing.JTextField();
        EllipsisBtnActionPerformed = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(995, 730));

        jPanel1.setBackground(new java.awt.Color(30, 37, 59));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 102, 204));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jLabel2.setText("Plug N' Archive");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Automated Backup Tool");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 37, Short.MAX_VALUE)
                        .addComponent(jLabel3))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1030, -1));

        jButton1.setBackground(new java.awt.Color(0, 102, 204));
        jButton1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Device Monitor");
        jButton1.setBorder(null);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(177, 100, 202, 47));

        jButton2.setBackground(new java.awt.Color(0, 102, 204));
        jButton2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("About Us");
        jButton2.setBorder(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(617, 100, 201, 47));

        jButton3.setBackground(new java.awt.Color(0, 102, 204));
        jButton3.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Archive History");
        jButton3.setBorder(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(397, 101, 202, 46));

        jPanel3.setBackground(new java.awt.Color(30, 37, 59));

        jPanel8.setBackground(new java.awt.Color(43, 78, 112));

        SelectAllCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        SelectAllCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        SelectAllCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        SelectAllCheckBox.setText("Select All");
        SelectAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectAllCheckBoxActionPerformed(evt);
            }
        });

        VideosCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        VideosCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        VideosCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        VideosCheckBox.setText("Videos");
        VideosCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VideosCheckBoxActionPerformed(evt);
            }
        });

        ImagesCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        ImagesCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ImagesCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        ImagesCheckBox.setText("Images");
        ImagesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImagesCheckBoxActionPerformed(evt);
            }
        });

        PdfCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        PdfCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        PdfCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        PdfCheckBox.setText("PDF");
        PdfCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PdfCheckBoxActionPerformed(evt);
            }
        });

        DocxCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        DocxCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        DocxCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        DocxCheckBox.setText("DOCX");
        DocxCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DocxCheckBoxActionPerformed(evt);
            }
        });

        XlsCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        XlsCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        XlsCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        XlsCheckBox.setText("XLXS");
        XlsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XlsCheckBoxActionPerformed(evt);
            }
        });

        PptCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        PptCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        PptCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        PptCheckBox.setText("PPT");
        PptCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PptCheckBoxActionPerformed(evt);
            }
        });

        MusicCheckBox.setBackground(new java.awt.Color(43, 78, 112));
        MusicCheckBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        MusicCheckBox.setForeground(new java.awt.Color(255, 255, 255));
        MusicCheckBox.setText("Music");
        MusicCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MusicCheckBoxActionPerformed(evt);
            }
        });

        label3.setForeground(new java.awt.Color(255, 255, 255));
        label3.setText("# of Files");

        label1.setForeground(new java.awt.Color(255, 255, 255));
        label1.setText("Total Size");

        label2.setForeground(new java.awt.Color(255, 255, 255));

        label4.setForeground(new java.awt.Color(255, 255, 255));

        label5.setForeground(new java.awt.Color(255, 255, 255));

        label6.setForeground(new java.awt.Color(255, 255, 255));

        label7.setForeground(new java.awt.Color(255, 255, 255));

        label8.setForeground(new java.awt.Color(255, 255, 255));

        label9.setForeground(new java.awt.Color(255, 255, 255));

        label10.setForeground(new java.awt.Color(255, 255, 255));

        label11.setForeground(new java.awt.Color(255, 255, 255));

        label12.setForeground(new java.awt.Color(255, 255, 255));

        label13.setForeground(new java.awt.Color(255, 255, 255));

        label14.setForeground(new java.awt.Color(255, 255, 255));

        label15.setForeground(new java.awt.Color(255, 255, 255));

        label16.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(SelectAllCheckBox)
                        .addGap(81, 81, 81)
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(VideosCheckBox)
                            .addComponent(ImagesCheckBox)
                            .addComponent(PdfCheckBox)
                            .addComponent(DocxCheckBox)
                            .addComponent(XlsCheckBox)
                            .addComponent(PptCheckBox)
                            .addComponent(MusicCheckBox))
                        .addGap(99, 99, 99)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(64, 64, 64))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(SelectAllCheckBox)
                                                            .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(VideosCheckBox)
                                                            .addComponent(label10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(ImagesCheckBox)
                                                    .addComponent(label11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(PdfCheckBox)
                                            .addComponent(label5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(DocxCheckBox)
                                            .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(XlsCheckBox)
                                    .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(PptCheckBox)
                            .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(MusicCheckBox)
                    .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton4.setBackground(new java.awt.Color(0, 102, 153));
        jButton4.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("CREATE BACKUP");
        jButton4.setBorder(null);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(43, 78, 112));

        jButton5.setBackground(new java.awt.Color(255, 0, 0));
        jButton5.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("CANCEL");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 669, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        detection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectionActionPerformed(evt);
            }
        });

        pathplaceHolder.setActionCommand("<Not Set>");
        pathplaceHolder.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        pathplaceHolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathplaceHolderActionPerformed(evt);
            }
        });

        EllipsisBtnActionPerformed.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        EllipsisBtnActionPerformed.setText("...");
        EllipsisBtnActionPerformed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EllipsisBtnActionPerformedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(detection, 0, 348, Short.MAX_VALUE)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(pathplaceHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(EllipsisBtnActionPerformed, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(130, 130, 130)
                                .addComponent(jLabel4)
                                .addGap(17, 17, 17)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(57, 57, 57))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(192, 192, 192)
                        .addComponent(detection, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pathplaceHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                            .addComponent(EllipsisBtnActionPerformed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(204, 204, 204)
                        .addComponent(jLabel4))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(193, 193, 193)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel3);

        jPanel4.setBackground(new java.awt.Color(30, 37, 59));

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jTable1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ARCHIVE NAME", "NO. OF FILES", "SIZE", "TIME"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel10.setBackground(new java.awt.Color(43, 78, 112));
        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));

        jLabel7.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("RECENT ARCHIVES");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(194, 194, 194)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(135, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel4);

        jPanel5.setBackground(new java.awt.Color(30, 37, 59));

        jPanel11.setBackground(new java.awt.Color(43, 78, 112));
        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel11.setForeground(new java.awt.Color(43, 78, 112));

        jTextArea1.setBackground(new java.awt.Color(43, 78, 112));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("PlugNArchive is a file backup and \narchiving solution designed to keep your \nfiles safe, organized, and duplicate-free.\nIt automatically detects duplicate \nfiles, avoids repeated type combinations, \nand maintains a detailed backup history.");
        jTextArea1.setCaretColor(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(383, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(222, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(167, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(151, 151, 151))
        );

        jTabbedPane1.addTab("tab3", jPanel5);

        jPanel1.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-100, 20, 1130, 680));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void detectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectionActionPerformed
        // TODO add your handling code here:
        String selected = (String) detection.getSelectedItem();
        if (selected == null || "Select a device".equals(selected) || !driveMap.containsKey(selected)) {
            // Clear all file count and size labels when no valid device is selected
            label2.setText("");
            label10.setText("");
            label4.setText("");
            label11.setText("");
            label5.setText("");
            label12.setText("");
            label6.setText("");
            label13.setText("");
            label7.setText("");
            label14.setText("");
            label8.setText("");
            label15.setText("");
            label9.setText("");
            label16.setText("");
        } else {
            File selectedDrive = driveMap.get(selected);
            System.out.println("Selected" + selectedDrive.getAbsolutePath());
            updateFileCount();
        }
    }//GEN-LAST:event_detectionActionPerformed

    private void EllipsisBtnActionPerformedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EllipsisBtnActionPerformedActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(this,
        "The default backup location is:\n" + BACKUP_FOLDER_PATH + "\n\n" +
        "Do you want to change it?",
        "Change Backup Location",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    
        if (choice == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File(BACKUP_FOLDER_PATH));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                pathplaceHolder.setText(selectedFolder.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_EllipsisBtnActionPerformedActionPerformed

    private void pathplaceHolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathplaceHolderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pathplaceHolderActionPerformed

    private void SelectAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectAllCheckBoxActionPerformed
        // TODO add your handling code here:
        updateAllCheckboxes();
    }//GEN-LAST:event_SelectAllCheckBoxActionPerformed

    private void VideosCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VideosCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_VideosCheckBoxActionPerformed

    private void ImagesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImagesCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_ImagesCheckBoxActionPerformed

    private void PdfCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PdfCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_PdfCheckBoxActionPerformed

    private void DocxCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DocxCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_DocxCheckBoxActionPerformed

    private void XlsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XlsCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_XlsCheckBoxActionPerformed

    private void MusicCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MusicCheckBoxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_MusicCheckBoxActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        try {
            String selected = (String) detection.getSelectedItem();
            if (selected == null || "Select a device".equals(selected)) {
                JOptionPane.showMessageDialog(this, "Please select a device.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File selectedDrive = driveMap.get(selected);
            if (selectedDrive == null) {
                JOptionPane.showMessageDialog(this, "Invalid device selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if drive is accessible
            if (!selectedDrive.exists() || !selectedDrive.canRead()) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot access the selected device.\nPlease ensure it is connected and accessible.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Collect selected file types
            List<String> selectedTypes = new ArrayList<>();
            if (VideosCheckBox.isSelected()) selectedTypes.add("video");
            if (ImagesCheckBox.isSelected()) selectedTypes.add("image");
            if (PdfCheckBox.isSelected()) selectedTypes.add("pdf");
            if (DocxCheckBox.isSelected()) selectedTypes.add("docx");
            if (XlsCheckBox.isSelected()) selectedTypes.add("xlsx");
            if (PptCheckBox.isSelected()) selectedTypes.add("ppt");
            if (MusicCheckBox.isSelected()) selectedTypes.add("music");

            if (selectedTypes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one file type to backup.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Collect files
            List<File> filesToBackup = new ArrayList<>();
            FileUtils.collectFilesRecursively(selectedDrive, selectedTypes, filesToBackup);

            if (filesToBackup.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No files found for the selected types.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Ensure backup directory exists
            File backupDir = new File(BackupConfig.BACKUP_FOLDER_PATH);
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot create backup folder at:\n" + BackupConfig.BACKUP_FOLDER_PATH + "\n\n" +
                        "Please check permissions.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Check if backup directory is writable
            if (!backupDir.canWrite()) {
                JOptionPane.showMessageDialog(this, 
                    "Backup folder is not writable:\n" + BackupConfig.BACKUP_FOLDER_PATH + "\n\n" +
                    "Please check permissions.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create archive
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String archiveName = "Backup_" + timestamp + ".zip";
            File zipFile = new File(backupDir, archiveName);

            // Reset and configure progress bar
            jProgressBar1.setValue(0);
            jProgressBar1.setMaximum(filesToBackup.size());
            jProgressBar1.setStringPainted(true);
            jProgressBar1.setString("Starting...");

            // Disable CREATE BACKUP button and enable CANCEL button
            jButton4.setEnabled(false);
            jButton5.setEnabled(true);
            jButton5.setText("CANCEL");

            // Create and start the backup worker
            currentBackupWorker = new BackupWorker(this, selectedDrive, filesToBackup, zipFile, archiveName);
            currentBackupWorker.execute();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error starting backup:\n" + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);

            // Reset buttons
            jButton4.setEnabled(true);
            jButton5.setEnabled(false);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        if (currentBackupWorker != null && !currentBackupWorker.isDone()) {
        
        // If currently paused, just resume
            if (currentBackupWorker.isPaused()) {
                currentBackupWorker.resumeBackup();
                jButton5.setText("CANCEL");
                jProgressBar1.setString("Resuming...");
                return;
            }

            // Pause the backup and show confirmation dialog
            currentBackupWorker.pauseBackup();
            jProgressBar1.setString("Paused - Waiting for response...");
            jButton5.setText("RESUME");

            int choice = JOptionPane.showConfirmDialog(this, """
                                                             Backup is paused.
                                                             
                                                             Do you want to cancel the backup?
                                                             
                                                             YES - Cancel and delete incomplete backup
                                                             NO - Resume the backup""",
                "Backup Paused",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                // User wants to cancel
                jProgressBar1.setString("Cancelling...");
                jButton5.setEnabled(false);
                jButton5.setText("CANCEL");
                currentBackupWorker.cancelBackup();
            } else {
                // User wants to continue (NO or closed dialog)
                currentBackupWorker.resumeBackup();
                jButton5.setText("CANCEL");
                jProgressBar1.setString("Resuming...");
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void PptCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PptCheckBoxActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_PptCheckBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox DocxCheckBox;
    private javax.swing.JButton EllipsisBtnActionPerformed;
    private javax.swing.JCheckBox ImagesCheckBox;
    private javax.swing.JCheckBox MusicCheckBox;
    private javax.swing.JCheckBox PdfCheckBox;
    private javax.swing.JCheckBox PptCheckBox;
    private javax.swing.JCheckBox SelectAllCheckBox;
    private javax.swing.JCheckBox VideosCheckBox;
    private javax.swing.JCheckBox XlsCheckBox;
    private javax.swing.JComboBox<String> detection;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label11;
    private java.awt.Label label12;
    private java.awt.Label label13;
    private java.awt.Label label14;
    private java.awt.Label label15;
    private java.awt.Label label16;
    private java.awt.Label label2;
    private java.awt.Label label3;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    private javax.swing.JTextField pathplaceHolder;
    // End of variables declaration//GEN-END:variables
}
