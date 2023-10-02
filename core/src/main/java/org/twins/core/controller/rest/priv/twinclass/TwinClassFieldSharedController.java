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
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassFieldSharedController extends ApiController {
    final AuthService authService;
    final DataListService dataListService;
    final DataListRestDTOMapper dataListRestDTOMapper;
    final TwinClassFieldService twinClassFieldService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldDataListSharedInHeadV1", summary = "Returns twin class field options shared in head (free for select)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class_field/{twinClassFieldId}/data_list_shared_in_head/{headTwinId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassFieldDataListSharedInHeadV1(
            @Parameter(name = "twinClassFieldId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_CLASS_FIELD_SHARED_IN_HEAD_ID) @PathVariable UUID twinClassFieldId,
            @Parameter(name = "headTwinId", in = ParameterIn.PATH, required = true, example = DTOExamples.HEAD_TWIN_ID) @PathVariable UUID headTwinId,
            @Parameter(name = "showDatalistMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDatalistMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findDataListOptionsSharedInHead(twinClassFieldId, headTwinId), new MapperProperties().setMode(showDatalistMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}