package org.twins.core.controller.rest.priv.datalist;

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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.datalist.DataListField;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListUpdateRqDTOv1;
import org.twins.core.mappers.rest.datalist.DataListAttributeRestDTOReverseMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapperV2;
import org.twins.core.mappers.rest.datalist.DataListUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapperV2;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;


@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListUpdateController extends ApiController {
    private final DataListUpdateDTOReverseMapper dataListUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListService dataListService;
    private final DataListRestDTOMapperV2 dataListRestDTOMapperV2;
    private final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;
    private final DataListAttributeRestDTOReverseMapper dataListAttributeRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListUpdateV1", summary = "Data list update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list/{dataListId}/v1")
    public ResponseEntity<?> dataListUpdateV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapperV2.class, response = DataListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId,
            @RequestBody DataListUpdateRqDTOv1 request) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            DataListEntity dataListEntity = dataListUpdateDTOReverseMapper.convert(request);
            dataListEntity.setId(dataListId);
            DataListField dataListField = new DataListField()
                    .setNameI18n(i18nRestDTOReverseMapper.convert(request.getNameI18n(), mapperContext))
                    .setDescriptionI18n(i18nRestDTOReverseMapper.convert(request.getDescriptionI18n(), mapperContext))
                    .setAttribute1(dataListAttributeRestDTOReverseMapper.convert(request.getAttribute1()))
                    .setAttribute2(dataListAttributeRestDTOReverseMapper.convert(request.getAttribute2()))
                    .setAttribute3(dataListAttributeRestDTOReverseMapper.convert(request.getAttribute3()))
                    .setAttribute4(dataListAttributeRestDTOReverseMapper.convert(request.getAttribute4()));
            dataListEntity = dataListService.updateDataList(dataListEntity, dataListField);
            rs
                    .setDataList(dataListRestDTOMapperV2.convert(dataListEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
