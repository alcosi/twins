package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.LocaleRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentViewRestDTOMapper;

import java.util.UUID;

@Tag(description = "Get data lists", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserLocaleController {

    @Operation(operationId = "domainUserLocaleUpdateV1", summary = "Update user locale in domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/domain/{domainId}/user/{userId}/locale/{localeName}/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> domainUserLocaleUpdateV1(
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @Parameter(example = DTOExamples.LOCALE) @PathVariable String localeName,
            @RequestParam(name = RestRequestParam.showCommentMode, defaultValue = CommentViewRestDTOMapper.Mode._DETAILED) CommentViewRestDTOMapper.Mode showCommentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._SHORT) AttachmentViewRestDTOMapper.Mode showAttachmentMode) {
        Response rs = new Response();
//        try {
//
//        } catch (ServiceException se) {
//            return createErrorRs(se, rs);
//        } catch (Exception e) {
//            return createErrorRs(e, rs);
//        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @Operation(operationId = "domainUserLocaleViewV1", summary = "View user locale in domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LocaleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/domain/{domainId}/user/{userId}/locale/v1", method = RequestMethod.GET)
    public ResponseEntity<?> domainUserLocaleViewV1(
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId) {
        LocaleRsDTOv1 rs = new LocaleRsDTOv1();
        rs.setLocale("en");
//        try {
//        } catch (ServiceException se) {
//            return createErrorRs(se, rs);
//        } catch (Exception e) {
//            return createErrorRs(e, rs);
//        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
