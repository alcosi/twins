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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.TwinStarredListRsDTOv1;
import org.twins.core.dto.rest.twin.TwinStarredRsDTOv1;
import org.twins.core.mappers.rest.twin.TwinStarredRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinStarredService;

import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinStarredController extends ApiController {
    final AuthService authService;
    final TwinStarredService twinStarredService;
    final TwinStarredRestDTOMapper twinStarredRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStarredListV1", summary = "Return list of starred twins of given class ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStarredListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class/{twinClassId}/starred/v1")
    public ResponseEntity<?> twinStarredListV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId) {
        TwinStarredListRsDTOv1 rs = new TwinStarredListRsDTOv1();
        try {
            List<TwinStarredEntity> twinStarredList = twinStarredService.findStarred(twinClassId);
            rs
                    .setStarredTwins(twinStarredRestDTOMapper.convertList(twinStarredList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "markTwinAsStarredV1", summary = "Mark given twin as starred for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStarredRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/start/v1")
    public ResponseEntity<?> markTwinAsStarredV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        TwinStarredRsDTOv1 rs = new TwinStarredRsDTOv1();
        try {
            TwinStarredEntity twinStarredEntity = twinStarredService.addStarred(twinId, authService.getApiUser());
            rs
                    .twinStarred(twinStarredRestDTOMapper.convert(twinStarredEntity));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "markTwinAsUnstarredV1", summary = "Unmark given twin as starred for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/unstart/v1")
    public ResponseEntity<?> markTwinAsUnstarredV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        Response rs = new Response();
        try {
            twinStarredService.deleteStarred(twinId, authService.getApiUser());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
