package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;
    final AttachmentRestDTOMapper attachmentRestDTOMapper;
    final AttachmentService attachmentService;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv2 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(TwinRestDTOMapper.TwinMode.ID_NAME_ONLY)) {
            case DETAILED:
                dst
                        .description(src.description())
                        .assignerUser(userDTOMapper.convert(src.assignerUser(), mapperProperties))
                        .authorUser(userDTOMapper.convert(src.createdByUser(), mapperProperties))
                        .status(twinStatusRestDTOMapper.convert(src.twinStatus(), mapperProperties))
                        .twinClass(twinClassRestDTOMapper.convert(src.twinClass(), mapperProperties))
                        .createdAt(src.createdAt().toInstant());
            case ID_NAME_ONLY:
                dst
                        .id(src.id())
                        .name(src.name());
        }

        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperProperties.getModeOrUse(TwinRestDTOMapper.FieldsMode.ALL_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapperV2.convertList(twinFieldEntityList, mapperProperties).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
            case NOT_EMPTY_FIELDS:
                twinFieldEntityList = twinService.findTwinFields(src.id());
                dst.fields(twinFieldRestDTOMapperV2.convertList(twinFieldEntityList, mapperProperties).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
        }
        switch (mapperProperties.getModeOrUse(TwinRestDTOMapper.AttachmentsMode.HIDE)) {
            case HIDE:
                break;
            case SHOW:
                dst.attachments(attachmentRestDTOMapper.convertList(attachmentService.findAttachmentByTwinId(src.id()), mapperProperties));
                break;
        }
    }
}
