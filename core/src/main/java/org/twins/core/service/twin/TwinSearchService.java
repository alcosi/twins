package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.auth.AuthService;

import java.util.*;

import static org.cambium.common.util.PaginationUtils.sort;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    final EntityManager entityManager;
    final TwinRepository twinRepository;
    final TwinService twinService;
    @Lazy
    final AuthService authService;

    private Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        return where(
                checkTwinLinks(basicSearch.getTwinLinksMap(), basicSearch.getTwinNoLinksMap())
                        .and(checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinEntity.Fields.name, basicSearch.getTwinNameLikeList(), true))
                        .and(checkClass(basicSearch.getTwinClassIdList(), authService.getApiUser()))
                        //todo create filter by basicSearch.getExtendsTwinClassIdList()
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, basicSearch.getAssignerUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.createdByUserId, basicSearch.getCreatedByUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.twinStatusId, basicSearch.getStatusIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.headTwinId, basicSearch.getHeaderTwinIdList(), false))
        );
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        List<TwinEntity> ret = twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sort(false, TwinEntity.Fields.createdAt));
        //todo чей-та за проверка если мы ранее проверили домен юзера и бизнес акк. Чисто лог для контроля если что-то проскочит?
        return ret.stream().filter(t -> !twinService.isEntityReadDenied(t)).toList();
    }

    //*****************************************************//
    //todo уточнить про offset и кратность size.           //
    // ибо в репозитории pagination постраничный           //
    // of25 + sz10 = pg2 (21-30) могут ожидать 26-35?      //
    // of8 + s10 = pg0 (1-10) могут ожидать 9-18?          //
    // если кратно, то of30 + sz10 = pg3 (31-40) - все окей//
    //*****************************************************//

    public TwinSearchResult findTwins(BasicSearch basicSearch, int offset, int size) throws ServiceException {
        TwinSearchResult twinSearchResult = new TwinSearchResult();
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        Page<TwinEntity> ret = twinRepository.findAll(where(spec), PaginationUtils.pagination(offset / size, size, sort(false, TwinEntity.Fields.createdAt)));
        return (TwinSearchResult) twinSearchResult
                .setTwinList(ret.getContent().stream().filter(t -> !twinService.isEntityReadDenied(t)).toList())
                .setOffset(offset)
                .setLimit(size)
                .setTotal(ret.getTotalElements());
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        return twinRepository.count(createTwinEntitySearchSpecification(basicSearch));
    }

    public Map<String, Long> countTwinsInBatch(Map<String, BasicSearch> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, BasicSearch> entry : searchMap.entrySet())
            result.put(entry.getKey(), count(entry.getValue()));
        return result;
    }


}
