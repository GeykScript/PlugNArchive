/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 *
 * @author kune
 */
public class FileUtils {
    // Format file size to human-readable format
    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    // Calculate total size of files
    public static long calculateTotalSize(List<File> files) {
        long total = 0;
        for (File file : files) {
            total += file.length();
        }
        return total;
    }
    
    // Collect files recursively based on selected types
    public static void collectFilesRecursively(File directory, List<String> types, List<File> files) {
        File[] fileList = directory.listFiles();
        if (fileList == null) return;

        for (File file : fileList) {
            if (file.isDirectory()) {
                collectFilesRecursively(file, types, files);
            } else {
                String name = file.getName().toLowerCase();
                boolean matches = false;
                
                for (String type : types) {
                    if (type.equals("video") && (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv"))) {
                        matches = true;
                    } else if (type.equals("image") && (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".gif"))) {
                        matches = true;
                    } else if (type.equals("pdf") && name.endsWith(".pdf")) {
                        matches = true;
                    } else if (type.equals("docx") && (name.endsWith(".docx") || name.endsWith(".doc"))) {
                        matches = true;
                    } else if (type.equals("xlsx") && (name.endsWith(".xlsx") || name.endsWith(".xls"))) {
                        matches = true;
                    } else if (type.equals("ppt") && (name.endsWith(".pptx") || name.endsWith(".ppt"))) {
                        matches = true;
                    } else if (type.equals("music") && (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac"))) {
                        matches = true;
                    }
                }
                
                if (matches) {
                    files.add(file);
                }
            }
        }
    }
    
    // Add file to ZIP archive
    public static void addFileToZip(File file, File rootDir, ZipOutputStream zos) throws IOException {
        String relativePath = rootDir.toPath().relativize(file.toPath()).toString();
        ZipEntry entry = new ZipEntry(relativePath);
        zos.putNextEntry(entry);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        
        zos.closeEntry();
    }
}
