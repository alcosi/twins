package org.twins.core.controller.rest.priv.statistic;

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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.TwinStatistic;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.statistic.StatisticCalcRqDTOv1;
import org.twins.core.dto.rest.statistic.StatisticCalcRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatisticService;

import java.util.Map;
import java.util.UUID;

@Tag(description = "", name = ApiTag.STATISTIC)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinStatisticCalcController extends ApiController {
    private final TwinStatisticService twinStatisticService;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatisticCalcV1", summary = "Returns statistic data by statistic id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistic data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = StatisticCalcRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/statistic/{statisticId}/v1")
    public ResponseEntity<?> twinStatisticCalcV1(
            @MapperContextBinding(roots = TwinRestDTOMapper.class, response = StatisticCalcRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.STATISTIC_ID) @PathVariable UUID statisticId,
            @RequestBody StatisticCalcRqDTOv1 request) {
        StatisticCalcRsDTOv1 rs = new StatisticCalcRsDTOv1();
        try {
            Map<UUID, TwinStatistic> map = twinStatisticService.calcStatistic(statisticId, request.getTwinIdSet());
            rs
                    .setStatistics(map);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
