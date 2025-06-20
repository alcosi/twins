package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.dto.rest.twin.TwinSearchSimpleDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinSearchSimpleDTOReverseMapper;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassValidLinkedTwinController extends ApiController {
    private final TwinLinkService twinLinkService;
    private final TwinSearchSimpleDTOReverseMapper twinSearchSimpleDTOReverseMapper;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "validLinkedTwinV1", summary = "Get valid twins list for link of new (not created) twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/{twinClassId}/link/{linkId}/valid_twins/v1")
    public ResponseEntity<?> validLinkedTwinV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @Parameter(example = DTOExamples.LINK_ID) @PathVariable UUID linkId,
            @RequestParam(required = false) UUID headTwinId,
            @RequestBody TwinSearchSimpleDTOv1 search,
            @SimplePaginationParams SimplePagination pagination) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            BasicSearch basicSearch = twinSearchSimpleDTOReverseMapper.convert(search);
            PaginationResult<TwinEntity> validHeads = twinLinkService.findValidDstTwins(twinClassId, linkId, headTwinId, basicSearch, pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(validHeads.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(validHeads));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
