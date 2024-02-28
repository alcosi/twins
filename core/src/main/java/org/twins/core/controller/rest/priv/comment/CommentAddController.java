package org.twins.core.controller.rest.priv.comment;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentCreateRqDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOReversedMapper;
import org.twins.core.service.comment.CommentService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentAddController extends ApiController {
    final CommentService commentService;
    final CommentRestDTOReversedMapper commentRestDTOReverseMapper;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentAddV1", summary = "Add comment to twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin comment", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentCreateRqDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/comment/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinCommentAddV1(
            @Parameter(example = DTOExamples.TWIN_COMMENT) @PathVariable UUID twinId,
            @RequestBody CommentCreateRqDTOv1 request) {
        TwinLinkAddRsDTOv1 rs = new TwinLinkAddRsDTOv1();
        try {
            TwinCommentEntity comment = new TwinCommentEntity();
            comment.setTwinId(twinId);
            commentRestDTOReverseMapper.map(request, comment, null);
            commentService.createComment(comment, attachmentAddRestDTOReverseMapper.convertList(request.attachments));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
