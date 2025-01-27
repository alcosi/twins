package org.twins.core.controller.rest.pub.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.core.io.InputStreamResource;
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
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.resource.ResourceService;

import java.time.Duration;
import java.util.UUID;

import static org.twins.core.config.filter.LoggingFilter.LOG_RS_BODY;

@Tag(description = "Get resource", name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ResourcePublicController extends ApiController {
    private final AuthService authService;
    private final ResourceService resourceService;

    @Operation(operationId = "getResourceFile", summary = "File in attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File", content = {@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Access is denied"),
            @ApiResponse(responseCode = "404", description = "File is not exist"),
    })
    @GetMapping(value = "/public/resource/{id}")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> localeListPublicViewV1(
            @Schema(implementation = UUID.class, description = "Resource Id", example = DTOExamples.RESOURCE_ID)
            @PathVariable("id") UUID resourceId,
            HttpServletRequest serverRq
            ) {
        try {
            serverRq.setAttribute(LOG_RS_BODY, false);
            var file = resourceService.getResourceFile(resourceId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(file.originalFileName()).build());
            headers.setContentLength(file.fileSize());
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic().immutable());
            var resource = new InputStreamResource(file.content());
            return new ResponseEntity<>(resource,headers, HttpStatus.OK);
        } catch (ServiceException se) {
            return createErrorRs(se, new Response());
        } catch (Exception e) {
            return createErrorRs(e, new Response());
        }
    }
}
