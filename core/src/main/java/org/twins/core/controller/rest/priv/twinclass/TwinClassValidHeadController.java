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
import org.cambium.common.pagination.SimplePagination;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.cambium.common.pagination.PaginationResult;
import org.twins.core.service.twin.TwinHeadService;

import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassValidHeadController extends ApiController {
    private final TwinHeadService twinHeadService;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassValidHeadV1", summary = "Get valid heads of given class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class/{twinClassId}/valid_heads/v1")
    public ResponseEntity<?> twinClassValidHeadsV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @SimplePaginationParams SimplePagination pagination) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            PaginationResult<TwinEntity> validHeads = twinHeadService.findValidHeads(twinClassId, pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(validHeads.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(validHeads));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
