package com.FVProject.filevault;

import com.FVProject.filevault.configcheck.ConfigChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = {
		"com.FVProject.filevault.CoreApp",
		"com.FVProject.filevault.configcheck"
})
public class FilevaultApplication {

	// Static block to load .env BEFORE Spring Boot starts
	static {
		loadEnvToSystemProperties();
	}

	public static void main(String[] args) {
		System.out.println("=== FILEVAULT APPLICATION STARTUP ===");

		loadEnvToSystemProperties(); // env vars load

		System.out.println("* Checking for existing configuration...");

		if (isWizardComplete()) {
			System.out.println("Configuration found! Starting main application on port 8080...");

			String resolvedUrl = buildJdbcUrl();
			System.out.println(" :) App now running on port 8080 with full cloud/database connectivity.");
			System.out.println("Resolved JDBC URL: " + resolvedUrl);

			startMainApplication(args);

		} else {
			System.out.println(" :/ Initial configuration not detected. Launching setup wizard on port 8081...");
			System.out.println(" Wizard active → guiding environment setup.");
			startWizardApplication(args);
		}
	}

	/**
	 * Load .env file and set system properties for Spring Boot to resolve
	 */
	private static void loadEnvToSystemProperties() {
		Path envFile = Paths.get(".env");

		if (!Files.exists(envFile)) {
			System.out.println("[ENV] No .env file found - skipping environment loading");
			return;
		}

		try {
			System.out.println("[ENV] Loading .env file: " + envFile.toAbsolutePath());

			Files.readAllLines(envFile).forEach(line -> {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
					String[] parts = line.split("=", 2);
					if (parts.length == 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();

						// Remove quotes if present
						if ((value.startsWith("\"") && value.endsWith("\"")) ||
								(value.startsWith("'") && value.endsWith("'"))) {
							value = value.substring(1, value.length() - 1);
						}

						// Set as system property
						System.setProperty(key, value);

						// Log (hide passwords)
						if (key.toLowerCase().contains("pass")) {
							System.out.println("[ENV] Set: " + key + " = [HIDDEN]");
						} else {
							System.out.println("[ENV] Set: " + key + " = " + value);
						}
					}
				}
			});

		} catch (IOException e) {
			System.err.println("[ENV] Error loading .env file: " + e.getMessage());
		}
	}

	/**
	 * Build JDBC URL using loaded environment variables
	 */
	private static String buildJdbcUrl() {
		String host = System.getProperty("MYSQL_HOST", "localhost");
		String port = System.getProperty("MYSQLPORT", "3306");
		String database = System.getProperty("DB", "unknown");

		return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", host, port, database);
	}

	private static boolean isWizardComplete() {
		// First check if .env file exists
		if (!ConfigChecker.hasEnvFile()) {
			return false;
		}

		// Check if all required variables are present
		String[] requiredVars = {"MYSQLPORT", "DB", "MYSQLUSER", "MYSQLPASS"};
		for (String var : requiredVars) {
			String value = System.getProperty(var);
			if (value == null || value.trim().isEmpty()) {
				System.out.println("[CONFIG] Missing required variable: " + var);
				return false;
			}
		}

		// Check WIZARD_COMPLETE flag
		Path envFile = Paths.get(".env");
		try {
			String content = Files.readString(envFile);
			boolean wizardComplete = content.contains("WIZARD_COMPLETE=true");
			System.out.println("[CONFIG] WIZARD_COMPLETE flag: " + wizardComplete);
			return wizardComplete;
		} catch (IOException e) {
			System.err.println(" Error reading .env file: " + e.getMessage());
			return false;
		}
	}

	private static void startMainApplication(String[] args) {
		System.setProperty("server.port", "8080");
		System.setProperty("spring.profiles.active", "production");

		SpringApplication.run(FilevaultApplication.class, args);
	}

	private static void startWizardApplication(String[] args) {
		WizardApplication.main(args);
	}
}



