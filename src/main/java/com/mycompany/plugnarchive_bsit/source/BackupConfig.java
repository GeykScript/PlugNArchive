/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;

import java.io.File;

public class BackupConfig {
    
    public static final String PLUGNARCHIVE_HOME = 
        System.getProperty("user.home") + File.separator + "Documents" + File.separator + "PlugNArchive";
    
    // Path for the CSV record that tracks archived files (for duplicate detection)
    public static final String DUPLICATE_CSV_PATH = 
        PLUGNARCHIVE_HOME + File.separator + "archived_files_record.csv";
    
    // Path for the backup history CSV record
    public static final String BACKUP_HISTORY_PATH = 
        PLUGNARCHIVE_HOME + File.separator + "backup_history.csv";

    // Path for the actual zip files
    public static final String BACKUP_FOLDER_PATH = 
        PLUGNARCHIVE_HOME + File.separator + "Backups";

    
    public static void initializeDirectories() {
        File homeDir = new File(PLUGNARCHIVE_HOME);
        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }
        File backupDir = new File(BACKUP_FOLDER_PATH);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }
}