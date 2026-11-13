package org.twins.core.controller.rest.priv.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MultipartFileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.resource.ResourceCreateDTOv1;
import org.twins.core.dto.rest.resource.ResourceCreateRqDTOv1;
import org.twins.core.dto.rest.resource.ResourceListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.resource.ResourceCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.resource.ResourceRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.resource.ResourceService;

import java.util.List;
import java.util.Map;

/**
 * Resource creation endpoint, same multipart contract as the twin attachment flow: json "request" part
 * (ResourceCreateRqDTOv1, files referenced via storageLink = "multipart://<part_name>", non-multipart
 * link is stored as an external uri) plus parts carrying the referenced files. Unlike the twin flow
 * there is no bean validation / attachment restrictions; files are stored as unbound resource rows of
 * the caller's domain (DomainId header, standard ApiUser flow). Access is guarded by the dedicated
 * {@link Permissions#RESOURCE_CREATE} permission.
 *
 * The controller only resolves the multipart parts into files (web-layer concern); the whole
 * creation batch (naming, storage writes) lives in {@link ResourceService#createResources}.
 */
@Tag(name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.RESOURCE_CREATE)
public class ResourceCreateController extends ApiController {
    private static final String REQUEST_PART = "request";
    private final ResourceService resourceService;
    private final ResourceCreateRestDTOReverseMapper resourceCreateRestDTOReverseMapper;
    private final ResourceRestDTOMapper resourceRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "resourceCreateV1", summary = "Create domain resources from uploaded files or external links",
            description = "Creates one or more standalone resources of the DomainId domain. "
                    + "Same multipart contract as the twin attachment flow: 'request' part is a ResourceCreateRqDTOv1 json "
                    + "(files referenced via storageLink = \"multipart://<part_name>\", any other link is stored as an external uri), "
                    + "each referenced part carries the file itself. Each item must carry a file (multipart link) or an external uri; "
                    + "an empty files list is rejected with 400.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created resources", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/resource/create/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resourceCreateV1(
            @MapperContextBinding(roots = ResourceRestDTOMapper.class, response = ResourceListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = ResourceCreateRqDTOv1.class) @RequestPart(REQUEST_PART) byte[] requestBytes) {
        // mapRequest outside try: malformed json part must surface as 400 ResponseStatusException (same as the twin flow),
        // not as the generic 500 error body the catch block below produces
        ResourceCreateRqDTOv1 rq = mapRequest(requestBytes, ResourceCreateRqDTOv1.class);
        ResourceListRsDTOv1 rs = new ResourceListRsDTOv1();
        try {
            Map<String, MultipartFile> filesMap = MultipartFileUtils.collectFiles(request);
            if (rq.getFiles() != null)
                for (ResourceCreateDTOv1 item : rq.getFiles())
                    item.setFile(MultipartFileUtils.resolveMultipartFile(item.getStorageLink(), filesMap));
            List<ResourceEntity> stored = resourceService.createResources(resourceCreateRestDTOReverseMapper.convertCollection(rq.getFiles()));
            rs
                    .setResources(resourceRestDTOMapper.convertCollection(stored, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
            return new ResponseEntity<>(rs, HttpStatus.OK);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
