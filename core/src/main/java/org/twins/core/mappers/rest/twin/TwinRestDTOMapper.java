package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final AttachmentService attachmentService;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(TwinMode.ID_NAME_ONLY)) {
            case DETAILED:
                dst
                        .description(src.getDescription())
                        .assignerUser(userDTOMapper.convert(src.getAssignerUser(), mapperProperties))
                        .authorUser(userDTOMapper.convert(src.getCreatedByUser(), mapperProperties))
                        .status(twinStatusRestDTOMapper.convert(src.getTwinStatus(), mapperProperties))
                        .twinClass(twinClassRestDTOMapper.convert(src.getTwinClass(), mapperProperties))
                        .createdAt(src.getCreatedAt().toInstant());
            case ID_NAME_ONLY:
                dst
                        .id(src.getId())
                        .name(src.getName());
        }

        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperProperties.getModeOrUse(FieldsMode.ALL_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                break;
            case NOT_EMPTY_FIELDS:
                twinFieldEntityList = twinService.findTwinFields(src.getId());
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                break;
        }
        switch (mapperProperties.getModeOrUse(AttachmentsMode.HIDE)) {
            case HIDE:
                break;
            case SHOW:
                dst.attachments(attachmentRestDTOMapper.convertList(attachmentService.findAttachmentByTwinId(src.getId()), mapperProperties));
                break;
        }
    }

    public enum TwinMode implements MapperMode {
        ID_NAME_ONLY, DETAILED;

        public static final String _ID_NAME_ONLY = "ID_NAME_ONLY";
        public static final String _DETAILED = "DETAILED";
    }

    public enum FieldsMode implements MapperMode {
        NO_FIELDS, ALL_FIELDS, NOT_EMPTY_FIELDS;

        public static final String _NO_FIELDS = "NO_FIELDS";
        public static final String _ALL_FIELDS = "ALL_FIELDS";
        public static final String _NOT_EMPTY_FIELDS = "NOT_EMPTY_FIELDS";
    }

    public enum AttachmentsMode implements MapperMode {
        SHOW, HIDE;

        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }
}
