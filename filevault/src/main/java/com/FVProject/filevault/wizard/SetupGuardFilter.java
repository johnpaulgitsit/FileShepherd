package com.FVProject.filevault.wizard;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.annotation.Order;
import java.nio.file.Paths;

@Component
@Order(1)
public class SetupGuardFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();

        // In wizard mode, simpler logic:
        // - Allow all wizard/API routes
        // - Allow static resources
        // - Block everything else until wizard is complete

        if (isWizardOrStaticResource(uri)) {
            chain.doFilter(req, res);
            return;
        }

        if (isWizardComplete()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"error\": \"Configuration complete. Please restart the application.\"}");
            return;
        }

        // Wizard not complete, redirect to wizard
        if (uri.equals("/") || uri.isEmpty()) {
            response.sendRedirect("/wizard");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Setup wizard must be completed first.");
        }
    }

    private boolean isWizardOrStaticResource(String uri) {
        return uri.startsWith("/wizard") ||
                uri.startsWith("/api/wizard") ||
                uri.startsWith("/static/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/images/") ||
                uri.startsWith("/favicon.ico") ||
                uri.startsWith("/webjars/");
    }

    private boolean isWizardComplete() {
        try {
            if (!Files.exists(Paths.get(".env"))) {
                return false;
            }
            String content = Files.readString(Paths.get(".env"));
            return content.contains("WIZARD_COMPLETE=true");
        } catch (IOException e) {
            return false;
        }
    }
}

