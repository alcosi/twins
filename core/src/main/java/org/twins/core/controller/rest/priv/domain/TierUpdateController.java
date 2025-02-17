package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.domain.tier.TierUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv3;
import org.twins.core.dto.rest.domain.TierRsDTOv1;
import org.twins.core.dto.rest.domain.TierUpdateRqDTOv1;
import org.twins.core.mappers.rest.domain.TierRestDTOMapper;
import org.twins.core.mappers.rest.domain.TierRestDTOMapperV2;
import org.twins.core.mappers.rest.domain.TierUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.domain.TierService;

import java.util.UUID;

@Tag(name = ApiTag.TIER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TierUpdateController extends ApiController {
    private final TierUpdateDTOReverseMapper tierUpdateDTOReverseMapper;
    private final TierService tierService;
    private final TierRestDTOMapperV2 tierRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "tierUpdateV1", summary = "tier for update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated tier data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv3.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/tier/{tierId}/v1")
    public ResponseEntity<?> tierUpdateV1(
            @MapperContextBinding(roots = TierRestDTOMapper.class, response = TierRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TIER_ID) @PathVariable UUID tierId,
            @RequestBody TierUpdateRqDTOv1 request) {
        TierRsDTOv1 rs = new TierRsDTOv1();
        try {
            TierUpdate tierUpdate = tierUpdateDTOReverseMapper.convert(request);
            TierEntity tierEntity = tierService.updateTier(tierUpdate.setId(tierId));
            rs.setTier(tierRestDTOMapperV2.convert(tierEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}