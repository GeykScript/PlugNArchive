/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;

/**
 *
 * @author kune
 */
public class BackupEntry {
    private String archiveName;
    private int numberOfFiles;
    private String size;
    private String time;
    
    public BackupEntry(String archiveName, int numberOfFiles, String size, String time) {
        this.archiveName = archiveName;
        this.numberOfFiles = numberOfFiles;
        this.size = size;
        this.time = time;
    }
    
    // Getters
    public String getArchiveName() { return archiveName; }
    public int getNumberOfFiles() { return numberOfFiles; }
    public String getSize() { return size; }
    public String getTime() { return time; }
    
    // Convert to CSV format
    public String toCSV() {
        return archiveName + "," + numberOfFiles + "," + size + "," + time;
    }
    
    // Create BackupEntry from CSV line
    public static BackupEntry fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 4) {
            return new BackupEntry(
                parts[0], 
                Integer.parseInt(parts[1]), 
                parts[2], 
                parts[3]
            );
        }
        return null;
    }
    
    // Convert to table row format
    public Object[] toTableRow() {
        return new Object[]{archiveName, numberOfFiles, size, time};
    }
}
