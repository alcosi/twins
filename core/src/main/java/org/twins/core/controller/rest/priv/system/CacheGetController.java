package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.system.CacheInfo;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.system.CacheInfoRsDTOv1;
import org.twins.core.dto.rest.system.CacheRsDTOv1;
import org.twins.core.mappers.rest.system.CacheRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.system.CacheService;

import java.util.List;

@ProtectedBy(Permissions.SYSTEM_APP_INFO_VIEW)
@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class CacheGetController extends ApiController {
    private final CacheService cacheService;
    private final CacheRestDTOMapper cacheRestDTOMapper;


    @ParametersApiUserHeaders
    @Operation(operationId = "cacheInfoV1", summary = "Returns cache info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache info shared successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CacheRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/cache/{cacheKey}")
    public ResponseEntity<?> cacheInfoV1(
            @Parameter(example = DTOExamples.CACHE_KEY) @PathVariable String cacheKey) {
        CacheRsDTOv1 rs = new CacheRsDTOv1();
        try {
            rs.setCacheInfo(cacheRestDTOMapper.convert(cacheService.getCacheInfo(cacheKey)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "allCachesInfoV1", summary = "Get information about all caches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All caches info retrieved successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CacheInfoRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/cache/all/info")
    public ResponseEntity<?> allCachesInfoV1() {
        CacheInfoRsDTOv1 rs = new CacheInfoRsDTOv1();
        try {
            List<CacheInfo> allCachesInfo = cacheService.getAllCachesInfo();
            rs.setCaches(cacheRestDTOMapper.convertCollection(allCachesInfo));
            return new ResponseEntity<>(rs, HttpStatus.OK);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
