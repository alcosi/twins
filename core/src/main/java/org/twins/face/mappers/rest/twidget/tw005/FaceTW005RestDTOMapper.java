package org.twins.face.mappers.rest.twidget.tw005;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.twidget.tw005.FaceTW005Entity;
import org.twins.face.dto.rest.twidget.tw005.FaceTW005DTOv1;
import org.twins.face.service.twidget.FaceTW005Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceTW005RestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<FaceTW005Entity>, FaceTW005DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    protected final FaceTW005Service faceTW005Service;
    protected final FaceTW005ButtonRestDTOMapper faceTW005ButtonRestDTOMapper;

    @Override
    public void map(TwidgetConfig<FaceTW005Entity> src, FaceTW005DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case DETAILED -> {
                faceTW005Service.loadButtons(src.getConfig());
                dst
                        .setGlue(src.getConfig().isGlue())
                        .setAlignVertical(src.getConfig().isAlignVertical())
                        .setStyleClasses(StringUtils.splitToSet(src.getConfig().getStyleClasses(), " "))
                        .setButtons(faceTW005ButtonRestDTOMapper.convertCollection(src.getConfig().getButtons(), mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwidgetConfig<FaceTW005Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceTW005Service.loadButtons(srcCollection.stream().map(TwidgetConfig::getConfig).toList());
    }
}
