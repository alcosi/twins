package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.domain.ApiUser;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;

    public List<DataListEntity> findDataLists(ApiUser apiUser, List<UUID> uuidLists) {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return dataListRepository.findByDomainIdAndIdIn(apiUser.domainId(), uuidLists);
        else
            return dataListRepository.findByDomainId(apiUser.domainId());
    }

    public List<DataListOptionEntity> findDataListOptions(UUID dataListId) {
        return dataListOptionRepository.findByDataListId(dataListId);
    }
}

