package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySmartService;
import org.cambium.common.util.UuidUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.dto.rest.twin.TwinTagAddDTOv1;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.twin.TemporalIdResolver;
import org.twins.core.service.user.UserService;
import org.cambium.common.exception.ServiceException;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class TwinCreateRqRestDTOReverseMapper extends RestSimpleDTOMapper<TwinCreateRqDTOv2, TwinCreate> {

    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    private final TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    private final UserService userService;
    private final AuthService authService;
    private final TemporalIdResolver temporalIdResolver;
    private final TemporalIdContext temporalIdContext;


    @Override
    public void beforeCollectionConversion(Collection<TwinCreateRqDTOv2> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        // Validate temporalId if present
        if (srcCollection.stream().anyMatch(dto -> dto.getTemporalId() != null)) {
            temporalIdResolver.validateTemporalIdUniquenessDTO(srcCollection);
            temporalIdResolver.validateTemporalIdReferencesExistDTO(srcCollection);

            // Generate UUID for all temporalId references and store in context
            for (TwinCreateRqDTOv2 dto : srcCollection) {
                if (dto.getTemporalId() != null) {
                    UUID uuid = UuidUtils.generate();
                    temporalIdContext.put(dto.getTemporalId(), uuid);
                }
            }

            // Build temporary TwinCreate list for detectCycles (only for sorting)
            List<TwinCreate> tempCreates = srcCollection.stream()
                    .map(dto -> new TwinCreate().setTemporalId(dto.getTemporalId())
                            .setHeadTwinRef(dto.getHeadTwinId()))
                    .collect(Collectors.toList());

            // Get sorted order by headTwinId dependencies
            List<Integer> sortedIndices = temporalIdResolver.detectCycles(tempCreates);
            temporalIdContext.setSortedIndices(sortedIndices);
        }
    }

    @Override
    public void map(TwinCreateRqDTOv2 src, TwinCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();

        dst
                .setTemporalId(src.getTemporalId())
                .setCreateStrategy(src.getCreateStrategy() != null ? src.getCreateStrategy() : Boolean.TRUE.equals(src.isSketch) ? TwinCreateStrategy.SKETCH : TwinCreateStrategy.STRICT) //legacy support
                .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(src.getClassId(), src.getFields()))
                .setTwinEntity(new TwinEntity()
                        .setTwinClassId(src.getClassId())
                        .setName(src.getName() == null ? "" : src.getName())
                        .setCreatedByUserId(apiUser.getUser().getId())
                        .setCreatedByUser(apiUser.getUser())
                        .setAssignerUserId(userService.checkId(src.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                        .setDescription(src.getDescription())
                        .setExternalId(src.getExternalId()));

        // Set pre-generated UUID from context for temporalId references
        if (src.getTemporalId() != null) {
            UUID uuid = temporalIdContext.get(src.getTemporalId());
            if (uuid != null) {
                dst.getTwinEntity().setId(uuid);
            }
        }

        // Resolve headTwinId - handle temporalId:XXX references
        if (src.getHeadTwinId() != null) {
            if (src.getHeadTwinId().startsWith(TemporalIdResolver.TEMPORAL_ID_PREFIX)) {
                String key = src.getHeadTwinId().substring(TemporalIdResolver.TEMPORAL_ID_PREFIX.length());
                UUID resolvedId = temporalIdContext.get(key);
                if (resolvedId == null) {
                    throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                            "Temporal ID reference not found: " + key);
                }
                dst.getTwinEntity().setHeadTwinId(resolvedId);
            } else {
                try {
                    UUID headTwinId = UUID.fromString(src.getHeadTwinId());
                    dst.getTwinEntity().setHeadTwinId(headTwinId);
                } catch (IllegalArgumentException e) {
                    throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                            "Invalid headTwinId format: " + src.getHeadTwinId() + ". Expected UUID or temporalId:XXX reference.");
                }
            }
        }

        dst
                .setAttachmentEntityList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getAttachments()))
                .setLinksEntityList(twinLinkAddRestDTOReverseMapper.convertCollection(src.getLinks()))
                .setTwinFieldAttributeEntityList(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(src.getFieldAttributes()))
                .setTagsAddNew(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::newTags)
                        .orElseGet(HashSet::new))
                .setTagsAddExisted(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::existingTags)
                        .orElseGet(HashSet::new));
    }
}
