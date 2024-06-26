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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
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
    @RequestMapping(value = "/private/attachment/{attachmentId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> attachmentViewV1(
            @Parameter(example = DTOExamples.ATTACHMENT_ID) @PathVariable UUID attachmentId,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._DETAILED) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode) {
        AttachmentViewRsDTOv1 rs = new AttachmentViewRsDTOv1();
        try {
            rs.setAttachment(
                    attachmentRestDTOMapperV2.convert(
                            attachmentService.findAttachment(attachmentId, EntitySmartService.FindMode.ifEmptyThrows), new MapperContext()
                                    .setMode(showUserMode)
                                    .setMode(showAttachmentMode)
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
