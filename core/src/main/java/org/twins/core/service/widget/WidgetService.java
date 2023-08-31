package org.twins.core.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.widget.WidgetEntity;
import org.twins.core.dao.widget.WidgetRepository;
import org.twins.core.featurer.widget.accessor.WidgetAccessor;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetService {
    final WidgetRepository widgetRepository;
    final TwinClassRepository twinClassRepository;
    final FeaturerService featurerService;

    public List<WidgetEntity> findWidgets(UUID twinClassId) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassRepository.findById(twinClassId).get();
        return findWidgets(twinClassEntity);
    }

    public List<WidgetEntity> findWidgets(TwinClassEntity twinClassEntity) throws ServiceException {
        List<WidgetEntity> allWidgets = widgetRepository.findAll();
        ListIterator<WidgetEntity> iter = allWidgets.listIterator();
        WidgetEntity widgetEntity;
        while (iter.hasNext()) {
            widgetEntity = iter.next();
            WidgetAccessor widgetAccessor = featurerService.getFeaturer(widgetEntity.widgetAccessorFeaturer(), WidgetAccessor.class);
            if (!widgetAccessor.isAvailableForClass(widgetEntity.widgetAccessorParams(), twinClassEntity))
                iter.remove();
        }
        return allWidgets;
    }
}

