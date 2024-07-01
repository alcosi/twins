package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassCreateController extends ApiController {
    final AuthService authService;
    final UserService userService;
    final TwinClassService twinClassService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinClassCreateRestDTOReverseMapper twinClassCreateRestDTOReverseMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCreateV1", summary = "Create new twin class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/v1")
    public ResponseEntity<?> twinClassCreateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, lazySupport = true) MapperContext mapperContext,
            @RequestBody TwinClassCreateRqDTOv1 request) {
        TwinClassCreateRsDTOv1 rs = new TwinClassCreateRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassCreateRestDTOReverseMapper.convert(request);
            I18nEntity nameI18n = i18nRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionsI18n = i18nRestDTOReverseMapper.convert(request.getDescriptionI18n());
            twinClassEntity = twinClassService.createInDomainClass(twinClassEntity, nameI18n, descriptionsI18n);
            rs
                    .setTwinClass(twinClassRestDTOMapper.convert(twinClassEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
