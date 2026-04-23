package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySmartService;
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
import org.twins.core.mappers.rest.link.TwinLinkAddTemporalRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.user.UserService;

import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class TwinCreateRqRestDTOReverseMapper extends RestSimpleDTOMapper<TwinCreateRqDTOv2, TwinCreate> {

    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    private final TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;
    private final TwinLinkAddTemporalRestDTOReverseMapper twinLinkAddTemporalRestDTOReverseMapper;
    private final UserService userService;
    private final AuthService authService;
    private final TemporalIdContext temporalIdContext;


    @Override
    public void map(TwinCreateRqDTOv2 src, TwinCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();

        dst
                .setCreateStrategy(src.getCreateStrategy() != null ? src.getCreateStrategy() : Boolean.TRUE.equals(src.isSketch) ? TwinCreateStrategy.SKETCH : TwinCreateStrategy.STRICT) //legacy support
                .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(src.getClassId(), src.getFields()))
                .setTwinEntity(new TwinEntity()
                        .setId(temporalIdContext.resolve(src.getTemporalId()))
                        .setTwinClassId(src.getClassId())
                        .setName(src.getName() == null ? "" : src.getName())
                        .setCreatedByUserId(apiUser.getUser().getId())
                        .setCreatedByUser(apiUser.getUser())
                        .setHeadTwinId(UuidUtils.fromStringOrNull(src.getHeadTwinId()))
                        .setAssignerUserId(userService.checkId(src.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                        .setDescription(src.getDescription())
                        .setExternalId(src.getExternalId()));

        dst
                .setAttachmentEntityList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getAttachments()))
                .setLinksEntityList(twinLinkAddTemporalRestDTOReverseMapper.convertCollection(src.getLinks()))
                .setTwinFieldAttributeEntityList(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(src.getFieldAttributes()))
                .setTagsAddNew(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::newTags)
                        .orElseGet(HashSet::new))
                .setTagsAddExisted(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::existingTags)
                        .orElseGet(HashSet::new));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinCreateRqDTOv2> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        if (srcCollection.stream().anyMatch(dto -> dto.getTemporalId() != null)) {
            collectTemporalIds(srcCollection);
            validateTemporalIdReferencesAndReplace(srcCollection);
            //elements should be sorted be levels, but this will be done on service level
        }
    }

    @Override
    public void afterCollectionConversion(Collection<TwinCreate> dstCollection, MapperContext mapperContext) throws Exception {
        super.afterCollectionConversion(dstCollection, mapperContext);
    }

    /**
     * 1. Validates temporalId uniqueness in DTO list
     * 2. Map to new UUID
     * 3. Collect map
     */
    public void collectTemporalIds(Collection<TwinCreateRqDTOv2> dtoList) throws ServiceException {
        temporalIdContext.clear();
        for (TwinCreateRqDTOv2 dto : dtoList) {
            if (dto.getTemporalId() != null) {
                if (temporalIdContext.contains(dto.getTemporalId())) {
                    throw new ServiceException(ErrorCodeTwins.DUPLICATE_TEMPORAL_ID,
                            "Duplicate temporalId: " + dto.getTemporalId());
                }
                temporalIdContext.put(dto.getTemporalId(), UuidUtils.generate());
            }
        }
    }

    /**
     * Validates that all temporalId references point to existing temporalIds in DTO list and replace them with ids
     */
    public void validateTemporalIdReferencesAndReplace(Collection<TwinCreateRqDTOv2> dtoList) throws ServiceException {
        for (TwinCreateRqDTOv2 dto : dtoList) {
            // Check headTwinId
            if (dto.getHeadTwinId() != null) {
                dto.setHeadTwinId(resolveTemporalOrReturnOriginal(dto.getHeadTwinId(),
                        dto.getTemporalId() + ".headTwinId"));
            }

            // Check fields
            if (dto.getFields() != null) {
                for (Map.Entry<String, String> entry : dto.getFields().entrySet()) {
                    if (entry.getValue() != null) {
                        entry.setValue(resolveTemporalOrReturnOriginal(entry.getValue(),
                                dto.getTemporalId() + ".field." + entry.getKey()));
                    }
                }
            }

            // Check links
            if (dto.getLinks() != null) {
                for (var link : dto.getLinks()) {
                    if (link.getDstTwinId() != null) {
                        link.setDstTwinId(resolveTemporalOrReturnOriginal(link.getDstTwinId(),
                                dto.getTemporalId() + ".link"));
                    }
                }
            }
        }
    }

    private String resolveTemporalOrReturnOriginal(String value, String contextPath) throws ServiceException {
        if (!TemporalIdContext.isTemporalReference(value))
            return value;
        String target = TemporalIdContext.extractTemporalKey(value);
        UUID replaceId = temporalIdContext.resolve(target);
        if (replaceId == null) {
            throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                    "Temporal reference '" + target + "' not found (referenced from " + contextPath + ")");
        }
        return replaceId.toString();
    }
}
