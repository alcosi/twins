package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCreateValidateRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCreateValidateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentCreateValidateV1", summary = "Validate attachment create operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment validation result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentCreateValidateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/validate_create/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> attachmentValidateV1(
            @MapperContextBinding(roots = AttachmentCreateValidateRestDTOMapper.class, response = AttachmentCreateValidateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody AttachmentCreateValidateRqDTOv1 request) {
        return createAttachment(mapperContext, request, Map.of());
    }


    @SneakyThrows
    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentCreateValidateV1", summary = "Validate attachment create operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment validation result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentCreateValidateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/validate_create/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCreateFromMultipart(
            @MapperContextBinding(roots = AttachmentCreateValidateRestDTOMapper.class, response = AttachmentCreateValidateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = AttachmentCreateValidateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return createAttachment(mapperContext, mapRequest(requestBytes, AttachmentCreateValidateRqDTOv1.class), filesMap);
    }

    protected ResponseEntity createAttachment(MapperContext mapperContext, AttachmentCreateValidateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        AttachmentCreateValidateRsDTOv1 rs = new AttachmentCreateValidateRsDTOv1();
        try {
            attachmentCreateRestDTOReverseMapper.preProcessAttachments(request.getCreate(), filesMap);
            EntityCUD<TwinAttachmentEntity> convert = attachmentCreateValidateRestDTOReverseMapper.convert(request, mapperContext);
            AttachmentCUDValidateResult result = attachmentRestrictionService.validateAttachments(null, request.getTwinClassId(), convert);
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
