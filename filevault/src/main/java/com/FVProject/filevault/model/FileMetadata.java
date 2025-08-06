package com.FVProject.filevault.model;

import jakarta.persistence.*;

@Entity
@Table(name = "-")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Owner of the file

    @Column(nullable = false)
    private String filename; // Original filename

    @Column(nullable = false, unique = true)
    private String filePath; // Path in Google Cloud Storage

    public FileMetadata() {}

    public FileMetadata(Long userId, String filename, String filePath) {
        this.userId = userId;
        this.filename = filename;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

