package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRuleRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRuleSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleSearchService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_RULE_MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_VIEW})
public class TwinClassFieldRuleSearchController extends ApiController {

    private final TwinClassFieldRuleSearchRestDTOReverseMapper twinClassFieldRuleSearchRestDTOReverseMapper;
    private final TwinClassFieldRuleSearchService twinClassFieldRuleSearchService;
    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldRuleSearchV1", summary = "Returns twin class field rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field rules list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldRuleSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_rule/search/v1")
    public ResponseEntity<?> twinClassFieldRuleSearchV1(
            @MapperContextBinding(roots = TwinClassFieldRuleRestDTOMapper.class, response = TwinClassFieldRuleSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassFieldRuleSearchRqDTOv1 request) {
        TwinClassFieldRuleSearchRsDTOv1 rs = new TwinClassFieldRuleSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFieldRuleEntity> twinClassFieldRuleList = twinClassFieldRuleSearchService.findTwinClassFieldRules(twinClassFieldRuleSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinClassFieldRuleList))
                    .setFieldRules(twinClassFieldRuleRestDTOMapper.convertCollection(twinClassFieldRuleList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
