package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_MANAGE, Permissions.ATTACHMENT_DELETE})
public class AttachmentDeleteController extends ApiController {
    private final AuthService authService;
    private final AttachmentService attachmentService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentDeleteV1", summary = "Delete attachment by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deletion result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/attachment/{attachmentId}/v1")
    public ResponseEntity<?> attachmentDeleteV1(
            @Parameter(example = DTOExamples.ATTACHMENT_ID) @PathVariable UUID attachmentId) {
        Response rs = new Response();
        try {
            ApiUser apiUser = authService.getApiUser();
            attachmentService.deleteById(apiUser, attachmentId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
