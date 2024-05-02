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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchBatchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchBatchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinSearchRqDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Map;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListCountController extends ApiController {

    final AuthService authService;
    final TwinService twinService;
    final TwinSearchService twinSearchService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    final TwinRestDTOMapper twinRestDTOMapper;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final TwinSearchRqDTOMapper twinSearchRqDTOMapper;
    final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchCountV1", summary = "Count twins by frontendId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map { frontendId / count }", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchBatchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/count/v1", method = RequestMethod.POST)
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
}
