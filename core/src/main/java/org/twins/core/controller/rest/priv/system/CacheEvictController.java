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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.system.CacheService;


@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class CacheEvictController extends ApiController {
    private final CacheService cacheService;

    @ParametersApiUserHeaders
    @Operation(operationId = "cacheEvictV1", summary = "Evict cache or specific record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache info shared successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/cache/{cacheKey}/evict")
    public ResponseEntity<?> cacheEvictV1(
            @Parameter(example = DTOExamples.CACHE_KEY) @PathVariable String cacheKey,
            @RequestParam(required = false) String recordKey) {
        Response rs = new Response();
        try {
            cacheService.evictCacheRecord(cacheKey, recordKey);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "allCachesEvictV1", summary = "Evict all caches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All caches info retrieved successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/cache/all/evict")
    public ResponseEntity<?> allCachesEvictV1() {
        Response rs = new Response();
        try {
            cacheService.evictAllCacheRecords();
            return new ResponseEntity<>(rs, HttpStatus.OK);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
