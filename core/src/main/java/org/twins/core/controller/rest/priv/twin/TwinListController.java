package org.twins.core.controller.rest.priv.twin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinListRqDTOv1;
import org.twins.core.dto.rest.twin.TwinListRsDTOv1;
import org.twins.core.domain.ApiUser;
import org.twins.core.mappers.rest.twin.TwinListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.List;

@Api(description = "Get twins by tql", tags = "transfers")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinListRestDTOMapper twinListRestDTOMapper;

    @ApiOperation(nickname = "twinListV1", value = "Returns twin list by tql", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Twin list prepared", response = TwinListRsDTOv1.class),
            @ApiResponse(code = 401, message = "Access is denied")})
    @RequestMapping(value = "/private/twins/v1", method = RequestMethod.POST)
    public ResponseEntity<?> transferListV4(
            @RequestBody TwinListRqDTOv1 request) {
        TwinListRsDTOv1 rs = new TwinListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinEntity> twinList = twinService.findTwins(apiUser, null);
            rs.setTwinList(twinListRestDTOMapper.convert(twinList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
