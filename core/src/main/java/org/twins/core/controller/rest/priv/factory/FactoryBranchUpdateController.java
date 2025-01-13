package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.factory.FactoryBranchCreateRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactoryBranchSaveDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.TwinFactoryBranchService;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryBranchUpdateController extends ApiController {
    private final FactoryBranchRestDTOMapperV2 factoryBranchRestDTOMapperV2;
    private final FactoryBranchSaveDTOReverseMapper factoryBranchSaveDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinFactoryBranchService twinFactoryBranchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchUpdateV1", summary = "Factory branch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory data branch update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory_branch/{factoryBranchId}/v1")
    public ResponseEntity<?> factoryBranchUpdateV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapperV2.class, response = FactoryBranchCreateRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_BRANCH_ID) @PathVariable UUID factoryBranchId,
            @RequestBody FactoryBranchUpdateRqDTOv1 request) {
        FactoryBranchCreateRsDTOv1 rs = new FactoryBranchCreateRsDTOv1();
        try {
            TwinFactoryBranchEntity branchEntity = factoryBranchSaveDTOReverseMapper.convert(request);
            branchEntity.setId(factoryBranchId);
            branchEntity = twinFactoryBranchService.updateFactoryBranch(branchEntity);
            rs
                    .setFactoryBranch(factoryBranchRestDTOMapperV2.convert(branchEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
