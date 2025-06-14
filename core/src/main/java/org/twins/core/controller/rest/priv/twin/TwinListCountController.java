package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.twin.TwinSearchByAliasDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinSearchExtendedDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinSearchService;

import java.util.HashMap;
import java.util.Map;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinListCountController extends ApiController {

    private final TwinSearchService twinSearchService;
    private final TwinSearchExtendedDTOReverseMapper twinSearchRqDTOMapper;
    private final TwinSearchByAliasDTOReverseMapper twinSearchByAliasDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchCountV1", summary = "Count twins by frontendId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map { frontendId / count }", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchBatchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/count/v1")
    public ResponseEntity<?> twinSearchCountInBatchV1(@RequestBody TwinSearchBatchRqDTOv1 request) {
        TwinSearchBatchRsDTOv1 rs = new TwinSearchBatchRsDTOv1();
        try {
            Map<String, BasicSearch> searchMap = new HashMap<>();
            for (Map.Entry<String, TwinSearchRqDTOv1> entry : request.searchMap.entrySet())
                searchMap.put(entry.getKey(), twinSearchRqDTOMapper.convert(entry.getValue()));
            rs.response(twinSearchService.countTwinsInBatch(searchMap));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchByAliasCountV1", summary = "Count twins by search aliases")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map { alias / count }", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchBatchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search_by_alias/count/v1")
    public ResponseEntity<?> twinSearchByAliasCountInBatchV1(@RequestBody TwinSearchByAliasBatchRqDTOv1 request) {
        TwinSearchBatchRsDTOv1 rs = new TwinSearchBatchRsDTOv1();
        try {
            Map<String, SearchByAlias> searchMap = new HashMap<>();
            for (Map.Entry<String, TwinSearchByAliasRqDTOv1> entry : request.searchMap.entrySet()) {
                SearchByAlias searchByAlias = twinSearchByAliasDTOReverseMapper.convert(entry.getValue());
                searchByAlias.setAlias(entry.getKey());
                searchMap.put(entry.getKey(), searchByAlias);
            }
            rs.response(twinSearchService.countTwinsBySearchAliasInBatch(searchMap));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
