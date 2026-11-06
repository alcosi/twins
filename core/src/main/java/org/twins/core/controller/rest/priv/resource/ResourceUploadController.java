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
import org.springframework.web.server.ResponseStatusException;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.resource.ResourceUploadItem;
import org.twins.core.service.permission.Permissions;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateRqDTOv1;
import org.twins.core.dto.rest.resource.ResourceUploadRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.service.resource.ResourceUploadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File upload endpoint accepting the same multipart contract as the twin attachment flow:
 * optional json "request" part (AttachmentCreateRqDTOv1, files referenced via
 * storageLink = "multipart://<part_name>") plus raw file parts. Unlike the twin flow there is no
 * bean validation / attachment restrictions; files are stored as unbound resource rows of the
 * caller's domain (DomainId header, standard ApiUser flow). Access is guarded by the dedicated
 * {@link Permissions#RESOURCE} permission.
 */
@Tag(name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.RESOURCE_CREATE)
public class ResourceUploadController extends ApiController {
    private static final String REQUEST_PART = "request";
    private static final String FILES_PART = "files";
    private static final String MULTIPART_SCHEME = "multipart://";
    private final ResourceUploadService resourceUploadService;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "resourceUploadV1", summary = "Upload files as domain resources",
            description = "Uploads one or more files as standalone resources of the DomainId domain. "
                    + "Same multipart contract as the twin attachment flow: optional 'request' part is an AttachmentCreateRqDTOv1 json "
                    + "(files referenced via storageLink = \"multipart://<part_name>\"), any other part is a file; "
                    + "in Swagger UI use the 'files' input. No validation is performed (no permissions, no restrictions).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded files", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceUploadRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/resource/upload/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resourceUploadV1(
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = AttachmentCreateRqDTOv1.class) @RequestPart(name = REQUEST_PART, required = false) byte[] requestBytes,
            @RequestPart(name = FILES_PART, required = false) List<MultipartFile> files) {
        ResourceUploadRsDTOv1 rs = new ResourceUploadRsDTOv1();
        try {
            return new ResponseEntity<>(resourceUploadService.uploadResources(collectUploadItems(request, requestBytes, files)), HttpStatus.OK);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }

    /**
     * Builds upload items in the attachments order when the json part is present (FE-style request),
     * otherwise treats every raw file part as a file (Swagger/curl style).
     */
    private List<ResourceUploadItem> collectUploadItems(MultipartHttpServletRequest request, byte[] requestBytes, List<MultipartFile> files) throws ServiceException {
        List<AttachmentCreateDTOv1> attachments = parseAttachments(requestBytes);
        if (attachments != null && !attachments.isEmpty()) {
            Map<String, MultipartFile> filesMap = MultipartFileUtils.collectFiles(request);
            attachmentCreateRestDTOReverseMapper.preProcessAttachments(attachments, filesMap);
            List<ResourceUploadItem> items = new ArrayList<>();
            for (AttachmentCreateDTOv1 att : attachments) {
                FileData domainFile = att.getDomainFile();
                if (domainFile != null) {
                    String partName = att.storageLink.substring(MULTIPART_SCHEME.length());
                    MultipartFile part = filesMap.get(partName);
                    items.add(new ResourceUploadItem()
                            .setFileName(resolveFileName(att.title, part != null ? part.getOriginalFilename() : null, partName))
                            .setContent(domainFile.content()));
                } else {
                    items.add(new ResourceUploadItem()
                            .setFileName(StringUtils.isNotEmpty(att.title) ? att.title : att.storageLink)
                            .setExternalUri(att.storageLink));
                }
            }
            return items;
        }
        // Swagger/curl style: no json part - every raw part is a file
        List<ResourceUploadItem> items = new ArrayList<>();
        if (files != null)
            for (MultipartFile file : files)
                items.add(item(file));
        java.util.Iterator<String> partNames = request.getFileNames();
        while (partNames.hasNext()) {
            String fileName = partNames.next();
            if (REQUEST_PART.equals(fileName) || FILES_PART.equals(fileName))
                continue;
            for (MultipartFile file : request.getFiles(fileName))
                items.add(item(file));
        }
        return items;
    }

    private String resolveFileName(String title, String partFileName, String partName) {
        if (StringUtils.isNotEmpty(title))
            return title;
        if (StringUtils.isNotEmpty(partFileName))
            return partFileName;
        return partName;
    }

    private ResourceUploadItem item(MultipartFile file) throws ServiceException {
        try {
            return new ResourceUploadItem()
                    .setFileName(file.getOriginalFilename())
                    .setContent(file.getInputStream());
        } catch (IOException e) {
            throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "can not read file part [" + file.getName() + "]");
        }
    }

    /**
     * Parses the json part without bean validation (this endpoint intentionally performs no validation),
     * same deserialization behavior as ApiController.mapRequest minus the validator.
     */
    private List<AttachmentCreateDTOv1> parseAttachments(byte[] requestBytes) {
        if (requestBytes == null || requestBytes.length == 0)
            return null;
        try {
            AttachmentCreateRqDTOv1 rq = objectMapper.readValue(requestBytes, AttachmentCreateRqDTOv1.class);
            return rq != null ? rq.getAttachments() : null;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed request json");
        }
    }
}
