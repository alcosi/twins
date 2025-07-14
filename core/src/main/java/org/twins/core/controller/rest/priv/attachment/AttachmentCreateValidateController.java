package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.attachment.AttachmentCreateValidateRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCreateValidateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_MANAGE, Permissions.ATTACHMENT_VALIDATE})
public class AttachmentCreateValidateController extends ApiController {

    private final AttachmentCreateValidateRestDTOReverseMapper attachmentCreateValidateRestDTOReverseMapper;
    private final AttachmentCreateValidateRestDTOMapper attachmentCreateValidateRestDTOMapper;
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentCreateValidateV1", summary = "Validate attachment create operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment validation result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentCreateValidateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/validate_create/v1")
    public ResponseEntity<?> attachmentValidateV1(
            @MapperContextBinding(roots = AttachmentCreateValidateRestDTOMapper.class, response = AttachmentCreateValidateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody AttachmentCreateValidateRqDTOv1 request) {
        AttachmentCreateValidateRsDTOv1 rs = new AttachmentCreateValidateRsDTOv1();
        try {
            AttachmentCUDValidateResult result = attachmentRestrictionService.validateAttachments(null, request.getTwinClassId(), attachmentCreateValidateRestDTOReverseMapper.convert(request, mapperContext));
            rs = attachmentCreateValidateRestDTOMapper.convert(result, mapperContext);
            rs
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
            if (result.hasProblems())
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID);

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
