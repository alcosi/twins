package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFreezeDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFreezeSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFreezeSearchService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassFreezeSearchController extends ApiController {
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;
    private final TwinClassFreezeSearchService twinClassFreezeSearchService;
    private final TwinClassFreezeSearchRestDTOReverseMapper twinClassFreezeSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFreezeSearchV1", summary = "Returns twin class freezes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class freeze list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFreezeSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_freeze/search/v1")
    public ResponseEntity<?> twinClassFreezeSearchV1(
            @MapperContextBinding(roots = TwinClassFreezeDTOMapper.class, response = TwinClassFreezeSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassFreezeSearchRqDTOv1 request) {
        TwinClassFreezeSearchRsDTOv1 rs = new TwinClassFreezeSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFreezeEntity> twinClassFreezeList = twinClassFreezeSearchService.findTwinClassFreezes(twinClassFreezeSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinClassFreezeList))
                    .setTwinClassFreezes(twinClassFreezeDTOMapper.convertCollection(twinClassFreezeList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
