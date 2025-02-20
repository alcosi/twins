package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationRestDTOMapperV2;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;

@Tag(description = "", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationCreateController {
    private final PermissionGrantAssigneePropagationService service;
    private final PermissionGrantAssigneePropagationRestDTOMapperV2 permissionGrantAssigneePropagationRestDTOMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantAssigneePropagationCreateDTOReverseMapper permissionGrantAssigneePropagationCreateDTOReverseMapper;
}
