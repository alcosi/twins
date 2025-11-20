package org.twins.face.mappers.rest.twidget.tw007;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.twidget.tw007.FaceTW007Entity;
import org.twins.face.dto.rest.twidget.tw007.FaceTW007DTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW007RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW007Entity>, FaceTW007DTOv1> {

    private final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final ResourceService resourceService;

    @Override
    public void map(PointedFace<FaceTW007Entity> src, FaceTW007DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);

        dst
                .setIconUrl(resourceService.getResourceUri(src.getConfig().getIconResource()))
                .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelId()))
                .setClassSelectorLabel(I18nCacheHolder.addId(src.getConfig().getClassSelectorLabelI18nId()))
                .setSaveChangesLabel(I18nCacheHolder.addId(src.getConfig().getSaveChangesLabelI18nId()))
                .setTwinClassSearchId(src.getConfig().getTwinClassSearchId());
    }
}
