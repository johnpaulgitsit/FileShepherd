package com.FVProject.filevault.services;

import com.FVProject.filevault.model.User;
import com.FVProject.filevault.repositories.FileMetadataRepository;
import com.FVProject.filevault.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;

@Service
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;
    private final DataSource dataSource; // Injecting DataSource for MySQL operations
    private final UserRepository userRepository;

        public Long getUserIdByUsername(String username) {
            User user = userRepository.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException("User not found");
            return user.getId();
        }

    public FileService(
            FileMetadataRepository fileMetadataRepository,
            DataSource dataSource,
            UserRepository userRepository
    ) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    // Securely retrieve stored encryption key
    public SecretKey getStoredKey(Long userId) throws Exception {
        return KeyGenerator.getInstance("AES").generateKey();
    }

    // Encrypt file before saving
    public byte[] encryptFile(byte[] fileData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(fileData);
    }

    // Decrypt file before sending
    public byte[] decryptFile(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    // Store file metadata in MySQL using a stored procedure
    public void saveFileMetadata(Long userId, String filename, String filePath) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall("{CALL SaveFileMetadata(?, ?, ?)}")) {
            statement.setLong(1, userId);
            statement.setString(2, filename);
            statement.setString(3, filePath);
            statement.execute();
        }
    }

    // Save the encrypted file to the user's folder
    public void saveEncryptedFile(Long userId, String filename, byte[] encryptedData) throws Exception {
        String uploadDir = "uploads/" + userId + "/";
        Files.createDirectories(Paths.get(uploadDir));
        Path filePath = Paths.get(uploadDir + filename);
        Files.write(filePath, encryptedData);
    }
}
