package org.twins.core.controller.rest.priv.factory;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.factory.FactoryBranchDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryBranchDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryBranchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_BRANCH_CREATE})
public class FactoryBranchDuplicateController extends ApiController {
    private final FactoryBranchService factoryBranchService;
    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;
    private final FactoryBranchDuplicateRestDTOReverseMapper factoryBranchDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchDuplicateV1", summary = "Duplicates factory branches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory branches copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryBranchListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_branch/duplicate/v1")
    public ResponseEntity<?> factoryBranchDuplicateV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapper.class, response = FactoryBranchListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryBranchDuplicateRqDTOv1 request) {
        var rs = new FactoryBranchListRsDTOv1();

        try {
            var duplicates = factoryBranchDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedBranches = factoryBranchService.duplicateBranches(duplicates);
            rs
                    .setFactoryBranchList(factoryBranchRestDTOMapper.convertCollection(duplicatedBranches, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
