package org.twins.core.controller.rest.pub.attachement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.UUID;

@Tag(description = "Get attachment", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class AttachementPublicController extends ApiController {
    private final AuthService authService;
    private final AttachmentService attachmentService;
    @Value("${attachment.controller.http-cache-lifetime:7d}")
    Duration httpCacheLifetime;

    @Operation(operationId = "getAttachmentFile", summary = "File in attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File", content = {@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Access is denied"),
            @ApiResponse(responseCode = "404", description = "File is not exist"),
    })
    @GetMapping(value = "/public/attachment/{id}/v1")
    @Loggable(value = false)
    public ResponseEntity<?> getPublicResourceFile(
            @Schema(implementation = UUID.class, description = "Attachment Id", example = DTOExamples.ATTACHMENT_ID)
            @PathVariable("id") UUID attachmentId,
            HttpServletRequest serverRq, HttpServletResponse serverRs
    ) {
        Long time = System.currentTimeMillis();
        log.info("Started attachment " + attachmentId + " download");
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            var file = attachmentService.getAttachmentFile(attachmentId);
            serverRs.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            serverRs.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.originalFileName()).build().toString());
            if (file.fileSize() > 0) {
                serverRs.setHeader(HttpHeaders.CONTENT_LENGTH, file.fileSize() + "");
            }
            serverRs.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(httpCacheLifetime).cachePublic().immutable().getHeaderValue());
            serverRs.setStatus(HttpServletResponse.SC_OK);
            try (InputStream content = file.content()) {
                try (OutputStream out = serverRs.getOutputStream()) {
                    content.transferTo(out);
                    out.flush();
                    serverRs.flushBuffer();
                }
            }
            return null;
        } catch (ServiceException se) {
            log.error("Error downloading attachment {}", attachmentId, se);
            return createErrorRs(se, new Response());
        } catch (Exception e) {
            log.error("Error downloading attachment {}", attachmentId, e);
            return createErrorRs(e, new Response());
        } finally {
            log.info("Ended attachment " + attachmentId + " download. Took " + (System.currentTimeMillis() - time) + " ms");
        }
    }
}