package org.twins.core.controller.rest.priv.link;

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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.TwinLinkCreateRqDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddRsDTOv1;
import org.twins.core.mappers.rest.link.TwinLinkCreateRestDTOReverseMapper;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.LINK_MANAGE, Permissions.LINK_CREATE})
public class TwinLinkAddController extends ApiController {
    private final TwinLinkService twinLinkService;
    private final TwinService twinService;
    private final TwinLinkCreateRestDTOReverseMapper twinLinkCreateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinLinkAddV1", summary = "Add link to twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinLinkAddRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/{twinId}/link/v1")
    public ResponseEntity<?> twinLinkAddV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody TwinLinkCreateRqDTOv1 request) {
        TwinLinkAddRsDTOv1 rs = new TwinLinkAddRsDTOv1();
        try {
            twinLinkService.addLinks(
                    twinService.findEntitySafe(twinId),
                    twinLinkCreateRestDTOReverseMapper.convertCollection(request.getLinks()));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
