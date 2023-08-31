package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
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
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Operation(operationId = "twinClassListV1", summary = "Returns twin class list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassListV1(
            @Parameter(name = "UserId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.USER_ID) String userId,
            @Parameter(name = "DomainId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.DOMAIN_ID) String domainId,
            @Parameter(name = "BusinessAccountId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) String businessAccountId,
            @Parameter(name = "Channel", in = ParameterIn.HEADER,  required = true, example = DTOExamples.CHANNEL) String channel,
            @RequestBody TwinClassListRqDTOv1 request) {
        TwinClassListRsDTOv1 rs = new TwinClassListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            MapperProperties mapperProperties = MapperProperties.create();
            if (request.showFields())
                mapperProperties.setMode(TwinClassRestDTOMapper.Mode.SHOW_FIELDS);
            rs.twinClassList(
                    twinClassRestDTOMapper.convertList(
                            twinClassService.findTwinClasses(apiUser, request.twinClassIdList()), mapperProperties));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
