package org.twins.core.controller.rest.priv.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.util.MultipartFileUtils;
import org.cambium.common.util.StringUtils;
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
import org.twins.core.service.permission.Permissions;
import org.twins.core.dto.rest.resource.ResourceCreateDTOv1;
import org.twins.core.dto.rest.resource.ResourceCreateRqDTOv1;
import org.twins.core.dto.rest.resource.ResourceListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.resource.ResourceRestDTOMapper;
import org.twins.core.service.resource.ResourceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File upload endpoint, same multipart contract as the twin attachment flow: json "request" part
 * (ResourceCreateRqDTOv1, files referenced via storageLink = "multipart://<part_name>", non-multipart
 * link is stored as an external uri) plus parts carrying the referenced files. Unlike the twin flow
 * there is no bean validation / attachment restrictions; files are stored as unbound resource rows of
 * the caller's domain (DomainId header, standard ApiUser flow). Access is guarded by the dedicated
 * {@link Permissions#RESOURCE_CREATE} permission.
 */
@Tag(name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.RESOURCE_CREATE)
public class ResourceUploadController extends ApiController {
    private static final String REQUEST_PART = "request";
    private static final String MULTIPART_SCHEME = "multipart://";
    private final ResourceService resourceService;
    private final ResourceRestDTOMapper resourceRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "resourceUploadV1", summary = "Upload files as domain resources",
            description = "Uploads one or more files as standalone resources of the DomainId domain. "
                    + "Same multipart contract as the twin attachment flow: 'request' part is a ResourceCreateRqDTOv1 json "
                    + "(files referenced via storageLink = \"multipart://<part_name>\", any other link is stored as an external uri), "
                    + "each referenced part carries the file itself. No validation is performed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded files", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/resource/upload/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resourceUploadV1(
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
            List<ResourceEntity> stored = new ArrayList<>();
            if (rq.getFiles() != null)
                for (ResourceCreateDTOv1 item : rq.getFiles()) {
                    FileData file = MultipartFileUtils.resolveMultipartFile(item.getStorageLink(), filesMap);
                    if (file == null) // non-multipart storageLink - store as external uri, named by title or the link itself
                        stored.add(resourceService.addResource(StringUtils.isNotEmpty(item.getTitle()) ? item.getTitle() : item.getStorageLink(), item.getStorageLink()));
                    else {
                        if (StringUtils.isEmpty(file.originalFileName())) // part sent without a filename - name it by the part name
                            file = new FileData(file.content(), item.getStorageLink().substring(MULTIPART_SCHEME.length()), file.fileSize());
                        if (StringUtils.isNotEmpty(item.getTitle())) // title overrides the uploaded file name
                            file = new FileData(file.content(), item.getTitle(), file.fileSize());
                        stored.add(resourceService.addResource(file.originalFileName(), file.content()));
                    }
                }
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
