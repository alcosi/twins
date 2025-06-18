package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;
import org.twins.face.domain.twidget.wt002.FaceWT002ButtonTwin;
import org.twins.face.domain.twidget.wt002.FaceWT002Twin;
import org.twins.face.dto.rest.widget.wt002.FaceWT002DTOv1;
import org.twins.face.service.widget.FaceWT002Service;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceWT002RestDTOMapper extends RestSimpleDTOMapper<FaceWT002Twin, FaceWT002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceWT002Service faceWT002Service;

    @MapperModePointerBinding(modes = FaceWT002Modes.FaceWT002Button2TwinClassMode.class)
    protected final FaceWT002ButtonRestDTOMapper faceWT002ButtonRestDTOMapper;

    @Override
    public void map(FaceWT002Twin src, FaceWT002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getEntity().getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getEntity().getKey());
            case DETAILED -> {
                faceWT002Service.loadButtons(src.getEntity());

                List<FaceWT002ButtonTwin> buttonTwinList = src.getEntity().getButtons().stream().map(button -> new FaceWT002ButtonTwin(button, src.getCurrentTwinId())).toList();

                dst
                        .setKey(src.getEntity().getKey())
                        .setStyleClasses(StringUtils.splitToSet(src.getEntity().getStyleClasses(), " "))
                        .setButtons(faceWT002ButtonRestDTOMapper.convertCollection(buttonTwinList, mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT002Twin> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        Collection<FaceWT002Entity> entities = srcCollection.stream().map(FaceWT002Twin::getEntity).toList();
        faceWT002Service.loadButtons(entities);
    }
}
