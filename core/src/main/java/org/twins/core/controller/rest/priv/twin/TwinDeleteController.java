package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.draft.DraftRsDTOv1;
import org.twins.core.dto.rest.twin.TwinDeleteRqDTOv1;
import org.twins.core.mappers.rest.draft.DraftRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinEraserService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_DELETE})
public class TwinDeleteController extends ApiController {
    private final TwinEraserService twinEraserService;
    private final DraftRestDTOMapper draftRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinDeleteV1", summary = "Delete twin by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/twin/{twinId}/v1")
    public ResponseEntity<?> twinDeleteV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = DraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        DraftRsDTOv1 rs = new DraftRsDTOv1();
        try {
            DraftEntity draftEntity = twinEraserService.eraseTwin(twinId);
            rs
                    .setDraft(draftRestDTOMapper.convert(draftEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinDeleteBatchV1", summary = "Delete twins by self id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/delete/v1")
    public ResponseEntity<?> twinDeleteBatchV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = DraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinDeleteRqDTOv1 twinDeleteRqDTOv1) {
        DraftRsDTOv1 rs = new DraftRsDTOv1();
        try {
            DraftEntity draftEntity = twinEraserService.eraseTwins(twinDeleteRqDTOv1.twinIds);
            rs
                    .setDraft(draftRestDTOMapper.convert(draftEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinDeleteDraftedV1", summary = "Delete twin by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/twin/{twinId}/delete_drafted/v1")
    public ResponseEntity<?> twinDeleteDraftedV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = DraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        DraftRsDTOv1 rs = new DraftRsDTOv1();
        try {
            DraftEntity draftEntity = twinEraserService.eraseTwinDrafted(twinId);
            rs
                    .setDraft(draftRestDTOMapper.convert(draftEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
