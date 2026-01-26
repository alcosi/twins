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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryBranchCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryBranchCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryBranchService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.BRANCH_MANAGE, Permissions.BRANCH_CREATE})
public class FactoryBranchCreateController extends ApiController {
    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;
    private final FactoryBranchCreateDTOReverseMapper factoryBranchCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryBranchService factoryBranchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchCreateV1", summary = "Factory branch add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory branch add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryBranchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/{factoryId}/factory_branch/v1")
    public ResponseEntity<?> factoryBranchCreateV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapper.class, response = FactoryBranchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_ID) @PathVariable UUID factoryId,
            @RequestBody FactoryBranchCreateRqDTOv1 request) {
        FactoryBranchRsDTOv1 rs = new FactoryBranchRsDTOv1();
        try {
            TwinFactoryBranchEntity branchEntity = factoryBranchCreateDTOReverseMapper.convert(request);
            branchEntity.setTwinFactoryId(factoryId);
            branchEntity = factoryBranchService.createFactoryBranch(branchEntity);
            rs
                    .setFactoryBranch(factoryBranchRestDTOMapper.convert(branchEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
