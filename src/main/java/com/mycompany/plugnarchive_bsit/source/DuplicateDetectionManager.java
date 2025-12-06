/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.plugnarchive_bsit.source;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public class DuplicateDetectionManager {

    private static final String ARCHIVED_FILES_CSV = System.getProperty("user.home") +
                                                                        File.separator + "Documents" +
                                                                        File.separator + "PlugNArchive" +
                                                                        File.separator + "archived_files.csv";

    // Class to represent an archived file entry
    public static class ArchivedFile {
        private String fileName;
        private String filePath;
        private long fileSize;
        private String fileHash;
        private String fileType;
        private String archiveName;
        private String dateArchived;

        public ArchivedFile(String fileName, String filePath, long fileSize,
                            String fileHash, String fileType, String archiveName, String dateArchived) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.fileHash = fileHash;
            this.fileType = fileType;
            this.archiveName = archiveName;
            this.dateArchived = dateArchived;
        }

        public String toCSV() {
            return fileName + "," +
                    escapeCsv(filePath) + "," +
                    fileSize + "," +
                    fileHash + "," +
                    fileType + "," +
                    archiveName + "," +
                    dateArchived;
        }

        public static ArchivedFile fromCSV(String csvLine) {
            String[] parts = parseCsvLine(csvLine);
            if (parts.length == 7) {
                return new ArchivedFile(
                        parts[0],  // fileName
                        parts[1],  // filePath
                        Long.parseLong(parts[2]), // fileSize
                        parts[3],  // fileHash
                        parts[4],  // fileType
                        parts[5],  // archiveName
                        parts[6]   // dateArchived
                );
            }
            return null;
        }

        public String getFileHash() { return fileHash; }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getArchiveName() { return archiveName; }
    }

    // Calculate MD5 hash of a file, this makes the file UNIQUE!
    public static String calculateFileHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Error calculating hash for: " + file.getName());
            return "";
        }
    }

    // Load archived files
    public static Map<String, ArchivedFile> loadArchivedFiles() {
        Map<String, ArchivedFile> archivedFiles = new HashMap<>();
        File csvFile = new File(ARCHIVED_FILES_CSV);

        if (!csvFile.exists()) {
            return archivedFiles;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                ArchivedFile entry = ArchivedFile.fromCSV(line);
                if (entry != null) {
                    archivedFiles.put(entry.getFileHash(), entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading archived files: " + e.getMessage());
        }

        return archivedFiles;
    }

    // Save archived files to CSV
    private static void saveArchivedFiles(Map<String, ArchivedFile> archivedFiles) {
        try {
            File csvFile = new File(ARCHIVED_FILES_CSV);
            File parentDir = csvFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                writer.write("FileName,FilePath,FileSize,FileHash,FileType,ArchiveName,DateArchived");
                writer.newLine();

                for (ArchivedFile entry : archivedFiles.values()) {
                    writer.write(entry.toCSV());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving archived files: " + e.getMessage());
        }
    }

    // Add new archived files
    public static void addArchivedFiles(List<File> files, File rootDir, String archiveName, String fileType) {
        Map<String, ArchivedFile> existingFiles = loadArchivedFiles();
        String dateArchived = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        for (File file : files) {
            String hash = calculateFileHash(file);
            if (!hash.isEmpty() && !existingFiles.containsKey(hash)) {
                String relativePath = rootDir.toPath().relativize(file.toPath()).toString();

                ArchivedFile entry = new ArchivedFile(
                        file.getName(),
                        relativePath,
                        file.length(),
                        hash,
                        fileType,
                        archiveName,
                        dateArchived
                );

                existingFiles.put(hash, entry);
            }
        }

        saveArchivedFiles(existingFiles);
    }

    // Filter duplicate files
    public static List<File> filterDuplicates(List<File> files, List<String> selectedTypes) {
        Map<String, ArchivedFile> archivedFiles = loadArchivedFiles();
        List<File> uniqueFiles = new ArrayList<>();
        List<File> duplicateFiles = new ArrayList<>();

        for (File file : files) {
            String fileType = getFileType(file);
            String hash = calculateFileHash(file);

            boolean singleTypeBackup = selectedTypes.size() == 1 && selectedTypes.contains(fileType);

            if (hash.isEmpty()) {
                uniqueFiles.add(file);
            } else if (archivedFiles.containsKey(hash)) {
                if (singleTypeBackup) {
                    duplicateFiles.add(file);
                    System.out.println("Duplicate found: " + file.getName() +
                            " (previously archived in: " +
                            archivedFiles.get(hash).getArchiveName() + ")");
                } else {
                    uniqueFiles.add(file);
                }
            } else {
                uniqueFiles.add(file);
            }
        }

        return uniqueFiles;
    }

    // Determine file type
    private static String getFileType(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv")) return "video";
        if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".gif")) return "image";
        if (name.endsWith(".pdf")) return "pdf";
        if (name.endsWith(".docx") || name.endsWith(".doc")) return "docx";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return "xlsx";
        if (name.endsWith(".pptx") || name.endsWith(".ppt")) return "ppt";
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac")) return "music";

        return "other";
    }

    // Analyze duplicates
    public static DuplicateStats analyzeDuplicates(List<File> files, List<String> selectedTypes) {
        Map<String, ArchivedFile> archivedFiles = loadArchivedFiles();
        int totalFiles = files.size();
        int duplicateCount = 0;
        long duplicateSize = 0;
        List<String> duplicateNames = new ArrayList<>();

        for (File file : files) {
            String fileType = getFileType(file);
            String hash = calculateFileHash(file);

            boolean singleTypeBackup = selectedTypes.size() == 1 && selectedTypes.contains(fileType);

            if (!hash.isEmpty() && archivedFiles.containsKey(hash) && singleTypeBackup) {
                duplicateCount++;
                duplicateSize += file.length();
                duplicateNames.add(file.getName());
            }
        }

        return new DuplicateStats(totalFiles, duplicateCount, duplicateSize, duplicateNames);
    }

    // Statistics class
    public static class DuplicateStats {
        public int totalFiles;
        public int duplicateCount;
        public int uniqueCount;
        public long duplicateSize;
        public List<String> duplicateNames;

        public DuplicateStats(int totalFiles, int duplicateCount, long duplicateSize, List<String> duplicateNames) {
            this.totalFiles = totalFiles;
            this.duplicateCount = duplicateCount;
            this.uniqueCount = totalFiles - duplicateCount;
            this.duplicateSize = duplicateSize;
            this.duplicateNames = duplicateNames;
        }
    }

    // Escape CSV values
    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Parse CSV lines safely
    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
