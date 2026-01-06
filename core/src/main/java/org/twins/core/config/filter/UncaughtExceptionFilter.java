package org.twins.core.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.dto.rest.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;


@RequiredArgsConstructor
public class UncaughtExceptionFilter extends OncePerRequestFilter {

    protected final LoggingController loggingController;
    protected final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ResponseEntity rs = resolveResponse(resolveRootException(e));
            writeResponseIfNotNull(response, rs);
        }
    }

    protected ResponseEntity resolveResponse(Exception e) {
        ResponseEntity rs;
        if (e instanceof ServiceException) {
            rs = loggingController.createErrorRs((ServiceException) e, new Response());
        } else {
            rs = loggingController.createErrorRs(e, new Response());
        }
        return rs;
    }

    protected Exception resolveRootException(Throwable e) {
        if (e instanceof ServletException && e.getCause() != null && e.getCause() instanceof Exception) {
            e = e.getCause();
        }
        if (e instanceof UndeclaredThrowableException && e.getCause() != null && e.getCause() instanceof Exception) {
            e = e.getCause();
        }
        if (e instanceof Exception) {
            return (Exception) e;
        } else {
            return new RuntimeException(e);
        }
    }

    protected void writeResponseIfNotNull(HttpServletResponse response, ResponseEntity rs) throws IOException {
        if (rs == null) {
            return;
        }
        response.setStatus(rs.getStatusCode().value());
        if (rs.hasBody()) {
            String rsJson = objectMapper.writeValueAsString(rs.getBody());
            try (OutputStream os = response.getOutputStream()) {
                os.write(rsJson.getBytes());
                os.flush();
            }
        }
    }

    @Component
    public static class LoggingController extends ApiController {
    }

}
