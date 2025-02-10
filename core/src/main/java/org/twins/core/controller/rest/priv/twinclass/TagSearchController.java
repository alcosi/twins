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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TagSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TagSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapperV3;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twinclass.TagSearchDTOReverseMapper;
import org.twins.core.service.datalist.DataListOptionSearchService;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Tag(name = ApiTag.TWIN_CLASS)
public class TagSearchController extends ApiController {

    private final DataListOptionRestDTOMapperV3 dataListOptionRestDTOMapperV3;
    private final TagSearchDTOReverseMapper tagSearchDTOReverseMapper;
    private final DataListOptionSearchService dataListOptionSearchService;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "tagSearchV1", summary = "Tag search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TagSearchRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/twin_class/{twinClassId}/tag/search/v1")
    public ResponseEntity<?> tagSearchV1(
            @MapperContextBinding(roots = DataListRestDTOMapperV2.class, response = TagSearchRsDTOv1.class)
            MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TagSearchRqDTOv1 request) {

        TagSearchRsDTOv1 rs = new TagSearchRsDTOv1();
        try {
            PaginationResult<DataListOptionEntity> tags = dataListOptionSearchService
                    .findDataListOptionForDomain(tagSearchDTOReverseMapper.convert(request.setTwinClassId(twinClassId)), pagination);
            rs.setOptions(dataListOptionRestDTOMapperV3.convertCollection(tags.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(tags));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}

