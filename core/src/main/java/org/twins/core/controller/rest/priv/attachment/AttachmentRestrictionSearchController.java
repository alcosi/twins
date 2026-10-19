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
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionSearchRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionSearchRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.attachment.AttachmentRestrictionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_RESTRICTION_MANAGE, Permissions.ATTACHMENT_RESTRICTION_VIEW})
public class AttachmentRestrictionSearchController extends ApiController {
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final AttachmentRestrictionSearchDTOReverseMapper attachmentRestrictionSearchDTOReverseMapper;
    private final AttachmentRestrictionSearchService attachmentRestrictionSearchService;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentRestrictionSearchV1", summary = "Attachment restriction search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment restriction data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentRestrictionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/attachment_restriction/search/v1")
    public ResponseEntity<?> attachmentRestrictionSearchV1(
            @MapperContextBinding(roots = AttachmentRestrictionRestDTOMapper.class, response = AttachmentRestrictionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody AttachmentRestrictionSearchRqDTOv1 request) {
        AttachmentRestrictionSearchRsDTOv1 rs = new AttachmentRestrictionSearchRsDTOv1();
        try {
            PaginationResult<TwinAttachmentRestrictionEntity> attachmentRestrictions = attachmentRestrictionSearchService
                    .search(attachmentRestrictionSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(attachmentRestrictions))
                    .setAttachmentRestrictions(attachmentRestrictionRestDTOMapper.convertCollection(attachmentRestrictions.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
