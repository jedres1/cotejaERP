package com.coteja.erp.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaFallbackController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // For API routes that 404, return JSON error
        String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (uri != null && (uri.startsWith("/api/") || uri.startsWith("/admin/"))) {
            return new ResponseEntity<>("{\"success\":false,\"message\":\"Not found\"}", HttpStatus.NOT_FOUND);
        }

        // For all other 404s (React Router client-side routes), serve the SPA
        return "forward:/index.html";
    }
}
