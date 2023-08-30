package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.twin.TwinListRqDTOv1;
import org.twins.core.dto.rest.twin.TwinListRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.List;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinRestDTOMapper twinRestDTOMapper;

    @Operation(operationId = "twinListV1", summary = "Returns twin list by tql")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinListV1(
            @RequestHeader("UserId") String userId,
            @RequestHeader("DomainId") String domainId,
            @RequestHeader("BusinessAccountId") String businessAccountId,
            @RequestHeader("Channel") String channel,
            @RequestBody TwinListRqDTOv1 request) {
        TwinListRsDTOv1 rs = new TwinListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinEntity> twinList = twinService.findTwins(apiUser, null);
            MapperProperties mapperProperties = MapperProperties.create();
            if (!request.showUserDetails())
                mapperProperties.setMode(UserDTOMapper.Mode.ID_ONLY);
            if (!request.showStatusDetails())
                mapperProperties.setMode(TwinStatusRestDTOMapper.Mode.ID_ONLY);
            if (!request.showClassDetails())
                mapperProperties.setMode(TwinClassRestDTOMapper.Mode.ID_ONLY);
            rs.twinList(twinRestDTOMapper.convertList(
                    twinList, mapperProperties));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
