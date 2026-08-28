package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.history.HistoryTypeRepository;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * String-keyed dictionary service for {@code history_type}. Does not extend
 * {@code EntitySecureFindServiceImpl}: that base is UUID-typed end to end
 * ({@code entityRepository(): CrudRepository<T, UUID>}, {@code findEntitiesSafe(Collection<UUID>})},
 * and a String-keyed {@code load} overload cannot coexist with the UUID one (identical erasure).
 * {@link #load} mirrors the base-class load shape (same four functional arguments, same idempotent
 * skip-already-populated semantics) with String grouping ids instead.
 */
@Service
@RequiredArgsConstructor
public class HistoryTypeService {
    private final HistoryTypeRepository repository;

    public <E> void load(Collection<E> srcCollection,
                         Function<? super E, String> functionGetGroupingId,
                         Function<? super E, HistoryTypeEntity> functionGetGroupingEntity,
                         BiConsumer<E, HistoryTypeEntity> functionSetGroupingEntity) throws ServiceException {
        if (CollectionUtils.isEmpty(srcCollection)) {
            return;
        }
        List<E> needLoad = null;
        Set<String> groupingIds = null;
        for (E item : srcCollection) {
            if (functionGetGroupingEntity.apply(item) == null) {
                String id = functionGetGroupingId.apply(item);
                if (id != null) {
                    if (needLoad == null) {
                        needLoad = new ArrayList<>();
                        groupingIds = new LinkedHashSet<>();
                    }
                    needLoad.add(item);
                    groupingIds.add(id);
                }
            }
        }
        if (needLoad == null) {
            return;
        }
        Map<String, HistoryTypeEntity> historyTypeById = new HashMap<>();
        for (HistoryTypeEntity historyType : repository.findAllById(groupingIds)) {
            historyTypeById.put(historyType.getId(), historyType);
        }
        for (E item : needLoad) {
            functionSetGroupingEntity.accept(item, historyTypeById.get(functionGetGroupingId.apply(item)));
        }
    }
}
