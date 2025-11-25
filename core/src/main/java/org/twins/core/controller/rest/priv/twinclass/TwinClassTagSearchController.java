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
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TagSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TagSearchRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twinclass.TagSearchDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionSearchService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Set;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassTagSearchController extends ApiController {

    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final TagSearchDTOReverseMapper tagSearchDTOReverseMapper;
    private final DataListOptionSearchService dataListOptionSearchService;
    private final PaginationMapper paginationMapper;
    private final TwinClassService twinClassService;
    private final AuthService authService;

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
            @MapperContextBinding(roots = DataListRestDTOMapper.class, response = TagSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TagSearchRqDTOv1 request) {
        TagSearchRsDTOv1 rs = new TagSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);

            if (twinClassEntity.getTagDataListId() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_TAGS_NOT_ALLOWED, "Twin class is not suitable for search");
            }

            DataListOptionSearch dataListOptionSearch = tagSearchDTOReverseMapper.convert(request);
            dataListOptionSearch
                    .setDataListIdList(Set.of(twinClassEntity.getTagDataListId()));
            if (apiUser.isBusinessAccountSpecified())
                dataListOptionSearch
                        .setBusinessAccountIdList(Set.of(apiUser.getBusinessAccountId()));

            PaginationResult<DataListOptionEntity> tags = dataListOptionSearchService
                    .findDataListOptionForDomain(dataListOptionSearch, pagination);

            rs.setOptions(dataListOptionRestDTOMapper.convertCollection(tags.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(tags));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}

