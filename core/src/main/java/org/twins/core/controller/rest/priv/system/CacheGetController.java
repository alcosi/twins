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
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CacheUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.system.CacheRsDTOv1;


@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class CacheGetController extends ApiController {
    private final CacheManager cacheManager;

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
            Cache cache = cacheManager.getCache(cacheKey);
            if (cache == null) {
                throw new ServiceException(ErrorCodeCommon.CACHE_WRONG_KEY, "Cannot find cache by key [" + cacheKey + "]");
            }

            long itemCount = 0;
            double sizeInMb = 0;

            if (cache instanceof CaffeineCache) {
                Object nativeCache = cache.getNativeCache();
                itemCount = ((com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache).estimatedSize();
                sizeInMb = (double) CacheUtils.estimateSize(cache) / (1024 * 1024);
            }

            rs
                    .setItemsCount(itemCount)
                    .setSizeInMb(sizeInMb);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}