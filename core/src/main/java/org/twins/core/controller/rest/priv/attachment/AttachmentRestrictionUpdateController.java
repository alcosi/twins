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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionListRsDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionUpdateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.ATTACHMENT_RESTRICTION_UPDATE)
public class AttachmentRestrictionUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AttachmentRestrictionUpdateDTOReverseMapper attachmentRestrictionUpdateDTOReverseMapper;
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final AttachmentRestrictionService attachmentRestrictionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentRestrictionUpdateV1", summary = "Attachment restriction update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment restrictions updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentRestrictionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/attachment_restriction/v1")
    public ResponseEntity<?> attachmentRestrictionUpdateV1(
            @MapperContextBinding(roots = AttachmentRestrictionRestDTOMapper.class, response = AttachmentRestrictionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody AttachmentRestrictionUpdateRqDTOv1 request) {
        AttachmentRestrictionListRsDTOv1 rs = new AttachmentRestrictionListRsDTOv1();
        try {
            List<TwinAttachmentRestrictionEntity> entities = attachmentRestrictionService.updateAttachmentRestrictions(attachmentRestrictionUpdateDTOReverseMapper.convertCollection(request.getAttachmentRestrictions()));
            rs
                    .setAttachmentRestrictions(attachmentRestrictionRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
