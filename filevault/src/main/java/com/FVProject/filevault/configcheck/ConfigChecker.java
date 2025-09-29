package com.FVProject.filevault.configcheck;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class ConfigChecker {

    // Required variables
    public static final List<String> REQUIRED_VARS = Arrays.asList(
            "DB",
            "GCPKEYPATH",
            "MYBUCKET",
            "MYSQL_PORT",
            "MYSQLPASS",
            "MYSQLUSER"
    );

    private final Environment environment;

    public ConfigChecker(Environment environment) {
        this.environment = environment;
    }

    public boolean allVarsPresent() {
        for (String var : REQUIRED_VARS) {
            String value = environment.getProperty(var);
            if (value == null || value.isBlank()) {
                System.out.println("Missing config: " + var);
                return false;
            }
        }
        return true;
    }

    public List<String> getMissingVars() {
        ArrayList<String> missing = new ArrayList<>();
        for (String var : REQUIRED_VARS) {
            String value = environment.getProperty(var);
            if (value == null || value.isBlank()) {
                missing.add(var);
            }
        }
        return missing;
    }

    public Map<String, Boolean> getVarStatuses() {
        Map<String, Boolean> statuses = new HashMap<>();
        for (String var : REQUIRED_VARS) {
            String value = environment.getProperty(var);
            statuses.put(var, value != null && !value.isBlank());
        }
        return statuses;
    }

    public boolean isWizardComplete() {
        String wizardComplete = environment.getProperty("WIZARD_COMPLETE");
        return "true".equals(wizardComplete);
    }

    // Static method for checking before Spring context is available
    public static boolean hasEnvFile() {
        return java.nio.file.Files.exists(java.nio.file.Paths.get(".env"));
    }
}