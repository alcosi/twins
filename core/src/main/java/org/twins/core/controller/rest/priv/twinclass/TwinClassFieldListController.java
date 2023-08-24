package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.twinclass.TwinClassFieldListRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldListRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRsDTOv1;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;

@Tag(description = "Get twin class field list", name = "twinClassField")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassFieldListController extends ApiController {
    private final AuthService authService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldListRestDTOMapper twinClassFieldListRestDTOMapper;

    @Operation(operationId = "twinClassFieldListV1", summary = "Returns twin class field list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field list prepared" , content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class_field/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassFieldListV1(
            @RequestHeader("UserId") String userId,
            @RequestHeader("DomainId") String domainId,
            @RequestHeader("BusinessAccountId") String businessAccountId,
            @RequestHeader("Channel") String channel,
            @RequestBody TwinClassFieldListRqDTOv1 request) {
        TwinClassFieldListRsDTOv1 rs = new TwinClassFieldListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinClassFieldEntity> twinClassFieldsList = twinClassFieldService.findTwinClassFields(apiUser, request.twinClassId);
            rs.twinClassFieldList(twinClassFieldListRestDTOMapper.convert(twinClassFieldsList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
