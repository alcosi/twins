package org.twins.core.controller.rest.priv.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MultipartFileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.resource.ResourceListRsDTOv1;
import org.twins.core.dto.rest.resource.ResourceUpdateDTOv1;
import org.twins.core.dto.rest.resource.ResourceUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.resource.ResourceRestDTOMapper;
import org.twins.core.mappers.rest.resource.ResourceUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.resource.ResourceService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Tag(name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.RESOURCE_UPDATE)
public class ResourceUpdateController extends ApiController {
    private final ResourceService resourceService;
    private final ResourceUpdateRestDTOReverseMapper resourceUpdateRestDTOReverseMapper;
    private final ResourceRestDTOMapper resourceRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "resourceUpdateV1", summary = "Resource batch update",
            description = "Updates editable resource attributes: original file name and public flag. "
                    + "Null fields keep the current values. File content replace needs the multipart variant of this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated resources", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/resource/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resourceUpdateV1(
            @MapperContextBinding(roots = ResourceRestDTOMapper.class, response = ResourceListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid ResourceUpdateRqDTOv1 request) {
        return processUpdate(mapperContext, request, Collections.emptyMap());
    }

    @Operation(operationId = "resourceUpdateV1", summary = "Resource batch update with file replacement",
            description = "Same as the json variant, plus each update item may carry storageLink = \"multipart://<part_name>\" "
                    + "to replace the stored file with the given multipart part (non-multipart link is stored as an external uri). "
                    + "The resource name follows the new file name unless originalFileName is set on the item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated resources", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/resource/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resourceUpdateV1Multipart(
            @MapperContextBinding(roots = ResourceRestDTOMapper.class, response = ResourceListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = ResourceUpdateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        // mapRequest outside try: malformed json part must surface as 400 ResponseStatusException (same as the twin flow),
        // not as the generic 500 error body the catch block below produces
        ResourceUpdateRqDTOv1 rq = mapRequest(requestBytes, ResourceUpdateRqDTOv1.class);
        return processUpdate(mapperContext, rq, MultipartFileUtils.collectFiles(request));
    }

    protected ResponseEntity<?> processUpdate(MapperContext mapperContext, ResourceUpdateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        ResourceListRsDTOv1 rs = new ResourceListRsDTOv1();
        try {
            if (request.getResources() != null)
                for (ResourceUpdateDTOv1 item : request.getResources())
                    item.setFile(MultipartFileUtils.resolveMultipartFile(item.getStorageLink(), filesMap));
            Collection<ResourceEntity> resources = resourceService.updateResources(resourceUpdateRestDTOReverseMapper.convertCollection(request.getResources()));
            rs
                    .setResources(resourceRestDTOMapper.convertCollection(resources, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
            return new ResponseEntity<>(rs, HttpStatus.OK);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
