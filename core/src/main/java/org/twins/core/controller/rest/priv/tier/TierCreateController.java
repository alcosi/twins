package org.twins.core.controller.rest.priv.tier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierCreateRqDTOv1;
import org.twins.core.dto.rest.tier.TierListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.tier.TierCreateDTOReverseMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.tier.TierService;

import java.util.List;

@Tag(description = "", name = ApiTag.TIER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TIER_CREATE)
public class TierCreateController extends ApiController {
    private final TierService tierService;
    private final TierCreateDTOReverseMapper tierCreateDTOReverseMapper;
    private final TierRestDTOMapper tierRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "tierCreateV1", summary = "Tier batch add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tier batch added successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TierListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/tier/v1")
    public ResponseEntity<?> tierCreateV1(
            @MapperContextBinding(roots = TierRestDTOMapper.class, response = TierListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid TierCreateRqDTOv1 request) {
        TierListRsDTOv1 rs = new TierListRsDTOv1();
        try {
            List<TierEntity> tierEntities = tierService.createTiers(tierCreateDTOReverseMapper.convertCollection(request.getTiers()));
            rs.setTiers(tierRestDTOMapper.convertCollection(tierEntities, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
