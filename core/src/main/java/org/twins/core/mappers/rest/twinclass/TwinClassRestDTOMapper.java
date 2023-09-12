package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final I18nService i18nService;
    final TwinClassFieldService twinClassFieldService;

    final TwinService twinService;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    @Autowired
    TwinRestDTOMapper twinRestDTOMapper;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(TwinClassRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .key(src.key())
                        .spaceClassId(src.spaceTwinClassId())
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .description(src.descriptionI18n() != null ? i18nService.translateToLocale(src.descriptionI18n()) : "")
                        .logo(src.logo());
            case ID_ONLY:
                dst
                        .id(src.id());
                break;
        }
        if (mapperProperties.getModeOrUse(TwinClassFieldRestDTOMapper.Mode.NONE) != TwinClassFieldRestDTOMapper.Mode.NONE)
            dst.fields(
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.findTwinClassFields(src.id())));
        if (mapperProperties.getModeOrUse(TwinRestDTOMapper.TwinMode.NONE) != TwinRestDTOMapper.TwinMode.NONE && src.spaceTwinClassId() != null)
            dst.validSpaces(
                    twinRestDTOMapper.convertList(
                            twinService.findTwinsByClass(src.spaceTwinClassId()), mapperProperties));
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
