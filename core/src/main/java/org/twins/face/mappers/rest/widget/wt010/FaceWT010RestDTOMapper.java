package org.twins.face.mappers.rest.widget.wt010;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt010.FaceWT010Entity;
import org.twins.face.dto.rest.widget.wt010.FaceWT010DTOv1;
import org.twins.face.service.widget.FaceWT010Service;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT010RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT010Entity>, FaceWT010DTOv1> {

    private final FaceWidgetRestDTOMapper<FaceWT010Entity> faceWidgetRestDTOMapper;
    private final ResourceService resourceService;
    private final FaceWT010Service faceWT010Service;

    @Override
    public void map(PointedFace<FaceWT010Entity> src, FaceWT010DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        faceWT010Service.loadIconResources(src.getConfig());

        dst
                .setIconUrl(resourceService.getResourceUri(src.getConfig().getIconResource()))
                .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelId()))
                .setClassSelectorLabel(I18nCacheHolder.addId(src.getConfig().getClassSelectorLabelI18nId()))
                .setSaveChangesLabel(I18nCacheHolder.addId(src.getConfig().getSaveChangesLabelI18nId()))
                .setTwinClassSearchId(src.getConfig().getTwinClassSearchId());
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT010Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT010Service.loadIconResources(srcCollection.stream().map(PointedFace::getConfig).toList());
    }
}
