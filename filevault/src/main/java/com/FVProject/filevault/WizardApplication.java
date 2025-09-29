package com.FVProject.filevault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(
        scanBasePackages = {
                "com.FVProject.filevault.wizard",
                "com.FVProject.filevault.configcheck"  // Add this line
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class
        }
)
public class WizardApplication {

    private static SpringApplication currentApp;

    public static void main(String[] args) {
        currentApp = new SpringApplication(WizardApplication.class);

        Map<String, Object> defaults = new HashMap<>();
        // Guarantee wizard runs on 8081 even if config file doesn't exist
        defaults.put("server.port", "8081");
        // Load properties if they exist, ignore if missing
        defaults.put("spring.config.location", "optional:file:./config/application-wizard.properties");

        currentApp.setDefaultProperties(defaults);
        currentApp.run(args);
    }

    // restart with main application after wizard completion
    public static void restartWithMainApplication() {
        System.out.println("✅ Configuration complete. Environment variables loaded into backend.");
        System.out.println("🔄 Reloading backend with new settings...");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Exit the JVM - full restart
        System.exit(0);
    }
}




