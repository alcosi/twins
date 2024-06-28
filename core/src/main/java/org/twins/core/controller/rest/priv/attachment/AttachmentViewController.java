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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapperV2;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AttachmentViewController extends ApiController {
    private final AuthService authService;
    private final AttachmentService attachmentService;
    private final AttachmentViewRestDTOMapperV2 attachmentRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentViewV1", summary = "View attachment by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/attachment/{attachmentId}/v1")
    public ResponseEntity<?> attachmentViewV1(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.ATTACHMENT_ID) @PathVariable UUID attachmentId,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = MapperMode.AttachmentMode.Fields.DETAILED) MapperMode.AttachmentMode showAttachmentMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode) {
        AttachmentViewRsDTOv1 rs = new AttachmentViewRsDTOv1();
        try {
            rs.setAttachment(
                    attachmentRestDTOMapperV2.convert(
                            attachmentService.findAttachment(attachmentId, EntitySmartService.FindMode.ifEmptyThrows), mapperContext
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
