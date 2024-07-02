package org.twins.core.controller.rest.priv.twinstarred;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinStarredRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinStarredRestDTOMapper;
import org.twins.core.service.twin.TwinStarredService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinStarredCreateController extends ApiController {
    final TwinStarredService twinStarredService;
    final TwinStarredRestDTOMapper twinStarredRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "markTwinAsStarredV1", summary = "Mark given twin as starred for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStarredRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/star/v1")
    public ResponseEntity<?> markTwinAsStarredV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @MapperContextBinding(roots = TwinStarredRestDTOMapper.class, response = TwinStarredRsDTOv1.class) MapperContext mapperContext) {
        TwinStarredRsDTOv1 rs = new TwinStarredRsDTOv1();
        try {
            TwinStarredEntity twinStarredEntity = twinStarredService.addStarred(twinId);
            rs
                    .twinStarred(twinStarredRestDTOMapper.convert(twinStarredEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
