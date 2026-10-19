package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDeleteRqDTOv1;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.ATTACHMENT_RESTRICTION_DELETE)
public class AttachmentRestrictionDeleteController extends ApiController {
    private final AttachmentRestrictionService attachmentRestrictionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentRestrictionDeleteV1", summary = "Attachment restriction delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment restrictions deleted"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment_restriction/delete/v1")
    public ResponseEntity<?> attachmentRestrictionDeleteV1(
            @RequestBody AttachmentRestrictionDeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            //todo add usage check
            attachmentRestrictionService.deleteSafe(request.getAttachmentRestrictionIdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
