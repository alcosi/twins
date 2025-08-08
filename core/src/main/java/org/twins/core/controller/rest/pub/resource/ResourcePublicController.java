package org.twins.core.controller.rest.pub.resource;

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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.resource.ResourceService;

import java.time.Duration;
import java.util.UUID;

@Tag(description = "Get resource", name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class ResourcePublicController extends ApiController {
    private final AuthService authService;
    private final ResourceService resourceService;
    @Value("${resource.controller.http-cache-lifetime:7d}")
    Duration httpCacheLifetime;

    @Operation(operationId = "getResourceFile", summary = "File in attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File", content = {@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Access is denied"),
            @ApiResponse(responseCode = "404", description = "File is not exist"),
    })
    @GetMapping(value = "/public/resource/{id}/v1")
    @Loggable(value = false)
    public ResponseEntity<?> getPublicResourceFile(
            @Schema(implementation = UUID.class, description = "Resource Id", example = DTOExamples.RESOURCE_ID)
            @PathVariable("id") UUID resourceId,
            @Schema(hidden = true) HttpServletRequest serverRq, @Schema(hidden = true) HttpServletResponse serverRs
    ) {
        Long time = System.currentTimeMillis();
        log.info("Started resource " + resourceId + " download");
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            var file = resourceService.getResourceFile(resourceId);
            serverRs.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(httpCacheLifetime).cachePublic().immutable().getHeaderValue());
            return ResponseEntity.ok(file);
        } catch (ServiceException se) {
            log.error("Error downloading resource {}", resourceId, se);
            return createErrorRs(se, new Response());
        } catch (Exception e) {
            log.error("Error downloading resource {}", resourceId, e);
            return createErrorRs(e, new Response());
        } finally {
            log.info("Ended resource " + resourceId + " download. Took " + (System.currentTimeMillis() - time) + " ms");
        }
    }
}
