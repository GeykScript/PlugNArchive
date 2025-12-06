/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;

// A simple class to hold archived file data and generate a unique key.
public class ArchivedFileRecord {
    public final String fileName;
    public final long fileSize;
    public final long lastModified;
    public final String fileType;
    public final String originalPath;
    public final String archiveName;
    public final String timestamp;

    public ArchivedFileRecord(String fileName, long fileSize, long lastModified, String fileType, String originalPath, String archiveName, String timestamp) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.fileType = fileType;
        this.originalPath = originalPath;
        this.archiveName = archiveName;
        this.timestamp = timestamp;
    }
    
    /**
     * Creates a unique key based on file properties for fast duplicate checking.
     * The combination of Name, Size, and Last Modified time is used.
     */
    public String getUniqueKey() {
        return this.fileName + "_" + this.fileSize + "_" + this.lastModified;
    }
}
