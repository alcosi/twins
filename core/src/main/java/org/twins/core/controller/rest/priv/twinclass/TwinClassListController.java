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
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRsDTOv1;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;

@Tag(description = "Get twin class list", name = "twinClass")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassListController extends ApiController {
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Operation(operationId = "twinClassListV1", summary = "Returns twin class list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassListV1(
            @RequestHeader("UserId") String userId,
            @RequestHeader("DomainId") String domainId,
            @RequestHeader("BusinessAccountId") String businessAccountId,
            @RequestHeader("Channel") String channel,
            @RequestBody TwinClassListRqDTOv1 request) {
        TwinClassListRsDTOv1 rs = new TwinClassListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinClassEntity> twinClassList = twinClassService.findTwinClasses(apiUser, request.twinClassIdList());
            rs.twinClassList(twinClassRestDTOMapper.convertList(twinClassList));
            if (request.showFields()) {
                for (TwinClassDTOv1 twinClass : rs.twinClassList()) {
                    twinClass.fields(
                            twinClassFieldRestDTOMapper.convertList(
                                    twinClassFieldService.findTwinClassFields(apiUser, twinClass.id())));
                }
            }
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
