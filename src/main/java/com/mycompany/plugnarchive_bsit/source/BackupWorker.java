/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;

public class BackupWorker extends SwingWorker<Void, Integer> {
    private final File selectedDrive;
    private final List<File> filesToBackup;
    private final File zipFile;
    private final String archiveName;
    private final Dashboard dashboard;
    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;
    private final Object pauseLock = new Object();
    private int totalFilesToArchive = 0; 
    
    public BackupWorker(Dashboard dashboard, File selectedDrive, List<File> filesToBackup, 
                        File zipFile, String archiveName) {
        this.dashboard = dashboard;
        this.selectedDrive = selectedDrive;
        this.filesToBackup = filesToBackup;
        this.zipFile = zipFile;
        this.archiveName = archiveName;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        ZipOutputStream zos = null;
        try {
            // Get selected types from the dashboard
            List<String> selectedTypes = dashboard.getSelectedFileTypes();

            // Filter out duplicates BEFORE creating the zip (type-aware)
            List<File> uniqueFiles = DuplicateDetectionManager.filterDuplicates(filesToBackup, selectedTypes);

            // If all files are duplicates, don't create backup
            if (uniqueFiles.isEmpty()) {
                throw new Exception("All files are duplicates. No new files to backup.");
            }
            
            totalFilesToArchive = uniqueFiles.size();

            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zos.setLevel(9);
            zos.setMethod(ZipOutputStream.DEFLATED);

            int processedFiles = 0;
            for (File file : uniqueFiles) {
                // Check if paused
                synchronized (pauseLock) {
                    while (isPaused && !isCancelled) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new InterruptedException("Backup interrupted");
                        }
                    }
                }

                // Check if cancelled
                if (isCancelled || isCancelled()) {
                    throw new InterruptedException("Backup cancelled by user");
                }

                FileUtils.addFileToZip(file, selectedDrive, zos);
                processedFiles++;
                publish(processedFiles);
            }

            // After successful backup, add ONLY unique files to archived files CSV
            String fileType = getFileTypesString(uniqueFiles);
            DuplicateDetectionManager.addArchivedFiles(uniqueFiles, selectedDrive, archiveName, fileType);
            
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception e) {
                    // Ignore close exception
                }
            }
        }
        return null;
    }

    // Helper method to get file types string
    private String getFileTypesString(List<File> files) {
        Set<String> types = new HashSet<>();
        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv")) {
                types.add("video");
            } else if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                types.add("image");
            } else if (name.endsWith(".pdf")) {
                types.add("pdf");
            } else if (name.endsWith(".docx") || name.endsWith(".doc")) {
                types.add("docx");
            } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                types.add("xlsx");
            } else if (name.endsWith(".pptx") || name.endsWith(".ppt")) {
                types.add("ppt");
            } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac")) {
                types.add("music");
            }
        }
        return String.join(", ", types);
    }
    
    @Override
    protected void process(List<Integer> chunks) {
        int latestProgress = chunks.get(chunks.size() - 1);
        dashboard.updateProgress(latestProgress, totalFilesToArchive); 
    }
    
    @Override
    protected void done() {
        try {
            get();

            // Get the actual number of unique files that were archived
            List<String> selectedTypes = dashboard.getSelectedFileTypes();
            List<File> uniqueFiles = DuplicateDetectionManager.filterDuplicates(filesToBackup, selectedTypes);

            // Backup completed successfully - save history with UNIQUE file count
            long zipFileSize = zipFile.length();
            BackupHistoryManager.saveBackupToHistory(archiveName, totalFilesToArchive, zipFileSize);
            dashboard.refreshBackupHistory();
            dashboard.resetBackupButton();
            dashboard.resetProgressBar();

            JOptionPane.showMessageDialog(dashboard,
                """
                Backup created successfully!
                
                Archive: """ + archiveName + "\n" +
                "Files: " + totalFilesToArchive + "\n" +
                "Size: " + FileUtils.formatSize(zipFileSize),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            

        } catch (java.util.concurrent.CancellationException e) {
            // Backup was cancelled
            dashboard.setProgressStatus("Cancelled");
            dashboard.resetBackupButton();

            // Delete incomplete zip file
            if (zipFile.exists()) {
                zipFile.delete();
            }

            JOptionPane.showMessageDialog(dashboard,
                "Backup cancelled by user.",
                "Cancelled",
                JOptionPane.WARNING_MESSAGE);

        } catch (InterruptedException e) {
            // Backup was interrupted
            dashboard.setProgressStatus("Cancelled");
            dashboard.resetBackupButton();

            // Delete incomplete zip file
            if (zipFile.exists()) {
                zipFile.delete();
            }

            JOptionPane.showMessageDialog(dashboard,
                "Backup cancelled by user.",
                "Cancelled",
                JOptionPane.WARNING_MESSAGE);

        } catch (Exception e) {
            // Backup failed - detailed error logging
            e.printStackTrace();

            dashboard.setProgressStatus("Failed");
            dashboard.resetBackupButton();

            // Delete incomplete zip file
            if (zipFile.exists()) {
                zipFile.delete();
            }

            // Don't save failed backups to history

            // Build detailed error message
            String errorMsg = "Unknown error";
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                if (cause.getMessage() != null && !cause.getMessage().isEmpty()) {
                    errorMsg = cause.getMessage();
                } else {
                    errorMsg = cause.getClass().getSimpleName();
                }
            } else if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMsg = e.getMessage();
            } else {
                errorMsg = e.getClass().getSimpleName();
            }

            JOptionPane.showMessageDialog(dashboard,
                "\nError creating backup:\n\n" + errorMsg + "\n\n",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            dashboard.resetProgressBar();
        }
    }

    public void pauseBackup() {
        synchronized (pauseLock) {
            isPaused = true;
        }
    }
    
    public void resumeBackup() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }
    
    public void cancelBackup() {
        isCancelled = true;
        synchronized (pauseLock) {
            pauseLock.notifyAll(); 
        }
        cancel(true);
    }
    
    public boolean isPaused() {
        return isPaused;
    }
}