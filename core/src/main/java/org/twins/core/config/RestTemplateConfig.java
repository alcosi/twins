package org.twins.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public
class RestTemplateConfig {

    @RequiredArgsConstructor
    @Service
    public static class LogRequestResponseFilter implements ClientHttpRequestInterceptor {

        private final static int MAX_SIZE = 2000;

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            UUID id = UUID.randomUUID();
            traceRequest(id, request, body);
            ClientHttpResponse response = execution.execute(request, body);
            traceResponse(id, response, request);
            return response;
        }

        private void traceRequest(UUID id, HttpRequest request, byte[] body) {
            StringBuilder sb = new StringBuilder();
            sb.append("===========================request begin================================================\n").
                    append("=ID         : {}\n").
                    append("=URI         : {}\n").
                    append("=Method      : {}\n").
                    append("=Headers     : {}\n").
                    append("=Request body: {}\n").
                    append("==========================request end================================================");

            String requestBody;
            String contentType = request.getHeaders().getContentType() != null
                    ? request.getHeaders().getContentType().toString()
                    : "";

            if (contentType.toLowerCase().contains("multipart/") && body.length > MAX_SIZE) {
                requestBody = new String(body, 0, MAX_SIZE, StandardCharsets.UTF_8) + "...\nMultipart body is too big to log";
            } else {
                requestBody = new String(body, StandardCharsets.UTF_8);
            }
            log.info(sb.toString(), id.toString(), request.getURI(), request.getMethod(), request.getHeaders(), requestBody);
        }

        private void traceResponse(UUID id, ClientHttpResponse response, HttpRequest request) throws IOException {
            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
            StringBuilder sb = new StringBuilder();

            sb.append("============================response begin==========================================\n").
                    append("=ID         : {}\n").
                    append("=URI         : {}\n").
                    append("=Method      : {}\n").
                    append("=Status code  : {}\n").
                    append("=Status text  : {}\n").
                    append("=Headers      : {}\n").
                    append("=Response body: {}\n").
                    append("=======================response end=================================================");
            log.info(sb.toString(), id.toString(), request.getURI(), request.getMethod(), response.getStatusCode(), response.getStatusText(), response.getHeaders(), inputStringBuilder);

        }
    }
}
