package org.twins.core.controller.rest.priv.draft;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.draft.DraftRsDTOv1;
import org.twins.core.mappers.rest.draft.DraftRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.DRAFT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DRAFT_COMMIT)
public class DraftCommitController extends ApiController {
    private final DraftRestDTOMapper draftRestDTOMapper;
    private final DraftCommitService draftCommitService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "draftCommitV1", summary = "Commit draft by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/draft/{draftId}/commit/v1")
    public ResponseEntity<?> draftCommitV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = DraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DRAFT_ID) @PathVariable UUID draftId) {
        DraftRsDTOv1 rs = new DraftRsDTOv1();
        try {
            DraftEntity draftEntity = draftCommitService.commitNowOrInQueue(draftId);
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
