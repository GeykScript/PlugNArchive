/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author kune
 */
public class BackupHistoryManager {
    private static final String HISTORY_FILE_PATH = System.getProperty("user.home") + 
                                                     File.separator + "Documents" + 
                                                     File.separator + "PlugNArchive" + 
                                                     File.separator + "backup_history.csv";
    
    // Save backup entry to history file
    public static void saveBackupToHistory(String archiveName, int numberOfFiles, 
                                          long sizeInBytes) {
        try {
            // Create parent directory if it doesn't exist
            File historyFile = new File(HISTORY_FILE_PATH);
            File parentDir = historyFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Append to history file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String size = FileUtils.formatSize(sizeInBytes);
                
                BackupEntry entry = new BackupEntry(archiveName, numberOfFiles, size, 
                                                     timestamp);
                writer.write(entry.toCSV());
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error saving backup history: " + e.getMessage());
        }
    }
    
    // Load all backup entries from history file
    public static List<BackupEntry> loadBackupHistory() {
        List<BackupEntry> entries = new ArrayList<>();
        File historyFile = new File(HISTORY_FILE_PATH);
        
        if (!historyFile.exists()) {
            return entries;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                BackupEntry entry = BackupEntry.fromCSV(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading backup history: " + e.getMessage());
        }
        
        return entries;
    }

    // Get history file path
    public static String getHistoryFilePath() {
        return HISTORY_FILE_PATH;
    }
}
