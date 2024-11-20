package org.twins.core.controller.rest.priv.twinstatus;

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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusSearchRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusSearchRestDTOReverseMapper;
import org.twins.core.service.twin.TwinStatusService;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinStatusSearchController extends ApiController {
    private final TwinStatusSearchRestDTOReverseMapper twinStatusSearchRestDTOReverseMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinStatusService twinStatusService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusSearchV1", summary = "Returns twin status search result in current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/search/v1")
    public ResponseEntity<?> twinStatusSearchV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinStatusSearchRqDTOv1 request) {
        TwinStatusSearchRsDTOv1 rs = new TwinStatusSearchRsDTOv1();
        try {
            PaginationResult<TwinStatusEntity> twinStatusList = twinStatusService
                    .findTwinStatusesForDomain(twinStatusSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setStatuses(twinStatusRestDTOMapper.convertCollection(twinStatusList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinStatusList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
