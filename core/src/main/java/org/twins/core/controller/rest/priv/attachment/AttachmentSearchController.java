package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentSearchRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentSearchRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.ATTACHMENT_VIEW)
public class AttachmentSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final AttachmentSearchDTOReverseMapper attachmentSearchDTOReverseMapper;
    private final AttachmentRestDTOMapper attachmentRestDTOMapper;
    private final AttachmentSearchService attachmentSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentSearchV1", summary = "Search data list of attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of attachments", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment/search/v1")
    public ResponseEntity<?> attachmentSearchV1(
            @MapperContextBinding(roots = AttachmentRestDTOMapper.class, response = AttachmentSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody AttachmentSearchRqDTOv1 request) {
        AttachmentSearchRsDTOv1 rs = new AttachmentSearchRsDTOv1();
        try {
            PaginationResult<TwinAttachmentEntity> attachmentList = attachmentSearchService
                    .findAttachments(attachmentSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setAttachments(attachmentRestDTOMapper.convertCollection(attachmentList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(attachmentList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
