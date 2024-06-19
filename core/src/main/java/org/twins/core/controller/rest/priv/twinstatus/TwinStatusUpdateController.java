package org.twins.core.controller.rest.priv.twinstatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.domain.I18nTranslation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twinstatus.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOReverseMapper;
import org.twins.core.service.twin.TwinStatusService;

@Tag(description = "", name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinStatusUpdateController extends ApiController {
    final TwinStatusService twinStatusService;
    final TwinStatusRestDTOReverseMapper twinStatusRestDTOReverseMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusUpdateV1", summary = "Update twin status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_status/v1")
    public ResponseEntity<?> twinStatusUpdateV1(
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestBody TwinStatusUpdateRqDTOv1 request) {
        TwinStatusUpdateRsDTOv1 rs = new TwinStatusUpdateRsDTOv1();
        try {
            I18nTranslation i18nTranslationName = i18nRestDTOReverseMapper.convert(request.getNameI18n());
            I18nTranslation i18nTranslationDescription = i18nRestDTOReverseMapper.convert(request.getDescriptionI18n());
            TwinStatusEntity twinStatusEntity = twinStatusService
                    .updateStatus(twinStatusRestDTOReverseMapper.convert(request), i18nTranslationName, i18nTranslationDescription);
            MapperContext mapperContext = new MapperContext()
                    .setMode(showStatusMode);
            rs
                    .setTwinStatus(twinStatusRestDTOMapper.convert(twinStatusEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
