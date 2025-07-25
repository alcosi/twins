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
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.attachment.AttachmentCUDValidateRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDValidateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.permission.Permissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_MANAGE, Permissions.ATTACHMENT_VALIDATE})
public class AttachmentCUDValidateController extends ApiController {

    private final AttachmentCUDValidateRestDTOReverseMapper attachmentCUDValidateRestDTOReverseMapper;
    private final AttachmentCUDValidateRestDTOMapper attachmentCUDValidateRestDTOMapper;
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentValidateV1", summary = "Validate attachment CUD operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment validation result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentCUDValidateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/validate_cud/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> attachmentValidateV1(
            @MapperContextBinding(roots = AttachmentCUDValidateRestDTOMapper.class, response = AttachmentCUDValidateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody AttachmentCUDValidateRqDTOv1 request) {
        return processValidate(mapperContext, request, Collections.emptyMap());
    }

    @Operation(operationId = "attachmentValidateV1", summary = "Validate attachment CUD operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment validation result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentCUDValidateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/validate_cud/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> attachmentValidateV1Multipart(
            @MapperContextBinding(roots = AttachmentCUDValidateRestDTOMapper.class, response = AttachmentCUDValidateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = AttachmentCUDValidateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return processValidate(mapperContext, mapRequest(requestBytes, AttachmentCUDValidateRqDTOv1.class), filesMap);
    }

    protected ResponseEntity<? extends Response> processValidate(MapperContext mapperContext, AttachmentCUDValidateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        AttachmentCUDValidateRsDTOv1 rs = new AttachmentCUDValidateRsDTOv1();
        try {
            attachmentCUDValidateRestDTOReverseMapper.preProcessAttachments(request.attachments, filesMap);
            AttachmentCUDValidateResult result = attachmentRestrictionService.validateAttachments(request.getTwinId(), null, attachmentCUDValidateRestDTOReverseMapper.convert(request, mapperContext));
            rs = attachmentCUDValidateRestDTOMapper.convert(result, mapperContext);
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
