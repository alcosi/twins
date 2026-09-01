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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionViewRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_RESTRICTION_MANAGE, Permissions.ATTACHMENT_RESTRICTION_VIEW})
public class AttachmentRestrictionViewController extends ApiController {
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentRestrictionViewV1", summary = "Attachment restriction view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment restriction data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentRestrictionViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/attachment_restriction/{attachmentRestrictionId}/v1")
    public ResponseEntity<?> attachmentRestrictionViewV1(
            @MapperContextBinding(roots = AttachmentRestrictionRestDTOMapper.class, response = AttachmentRestrictionViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.UUID_ID) @PathVariable UUID attachmentRestrictionId) {
        AttachmentRestrictionViewRsDTOv1 rs = new AttachmentRestrictionViewRsDTOv1();
        try {
            TwinAttachmentRestrictionEntity entity = attachmentRestrictionService.findEntitySafe(attachmentRestrictionId);
            rs
                    .setAttachmentRestriction(attachmentRestrictionRestDTOMapper.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
