package org.twins.core.controller.rest.priv.twinstatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.dao.I18nEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.twin.TwinStatusService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinStatusCreateController extends ApiController {
    private final TwinStatusCreateRestDTOReverseMapper twinStatusCreateRestDTOReverseMapper;
    private final TwinStatusService twinStatusService;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final I18nRestDTOReverseMapper i18NRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusCreateV1", summary = "Create new twin status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/{twinClassId}/twin_status/v1")
    public ResponseEntity<?> twinStatusCreateV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusCreateRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TwinStatusCreateRqDTOv1 request) {
        TwinStatusCreateRsDTOv1 rs = new TwinStatusCreateRsDTOv1();
        try {
            request.setTwinClassId(twinClassId);
            TwinStatusEntity twinStatusEntity = twinStatusCreateRestDTOReverseMapper.convert(request);
            I18nEntity nameI18n = i18NRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionsI18n = i18NRestDTOReverseMapper.convert(request.getDescriptionI18n());
            twinStatusService.createStatus(twinStatusEntity, nameI18n, descriptionsI18n);
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
