package com.FVProject.filevault.CoreApp.repositories;

import com.FVProject.filevault.CoreApp.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUserId(Long userId);
}

