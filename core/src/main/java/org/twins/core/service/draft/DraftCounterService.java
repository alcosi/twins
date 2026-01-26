package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.CUD;
import org.twins.core.dao.draft.*;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.enums.draft.DraftTwinEraseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.twins.core.domain.draft.DraftCounters.Counter.*;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.*;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DraftCounterService {
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldSimpleNonIndexedRepository draftTwinFieldSimpleNonIndexedRepository;
    private final DraftTwinFieldBooleanRepository draftTwinFieldBooleanRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    private final DraftTwinFieldTwinClassRepository draftTwinFieldTwinClassRepository;

    public void loadCounters(DraftEntity draftEntity) throws ServiceException {
        syncCounters(draftEntity);
    }

    public DraftCounters syncCounters(DraftEntity draftEntity) throws ServiceException {
        DraftCounters counters = draftEntity.getCounters();
        if (counters.allAreValid())
            return counters;
        syncPersists(draftEntity);
        syncErases(draftEntity);
        syncFields(draftEntity);
        syncLinks(draftEntity);
        syncMarkers(draftEntity);
        syncTags(draftEntity);
        syncAttachments(draftEntity);
        return counters;
    }

    private void syncFields(DraftEntity draftEntity) throws ServiceException {
        syncFieldsSimple(draftEntity);
        syncFieldsSimpleNonIndexed(draftEntity);
        syncFieldsBoolean(draftEntity);
        syncFieldsUser(draftEntity);
        syncFieldsDatalist(draftEntity);
        syncFieldsTwinClass(draftEntity);
    }

    private void syncFieldsSimple(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_SIMPLE))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinFieldSimpleRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(FIELD_SIMPLE_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_SIMPLE_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_SIMPLE_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncFieldsSimpleNonIndexed(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_SIMPLE_NON_INDEXED)) {
            return;
        }

        Map<Object, Integer> countersMap = toMap(draftTwinFieldSimpleNonIndexedRepository.getCounters(draftEntity.getId()));

        draftEntity.getCounters().set(FIELD_SIMPLE_NON_INDEXED_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_SIMPLE_NON_INDEXED_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_SIMPLE_NON_INDEXED_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncFieldsBoolean(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_BOOLEAN)) {
            return;
        }

        Map<Object, Integer> countersMap = toMap(draftTwinFieldBooleanRepository.getCounters(draftEntity.getId()));

        draftEntity.getCounters().set(FIELD_BOOLEAN_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_BOOLEAN_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_BOOLEAN_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncFieldsUser(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_USER))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinFieldUserRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(FIELD_USER_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_USER_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_USER_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncFieldsTwinClass(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_TWIN_CLASS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinFieldTwinClassRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(FIELD_TWIN_CLASS_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_TWIN_CLASS_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_TWIN_CLASS_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncFieldsDatalist(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(FIELDS_DATALIST))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinFieldDataListRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(FIELD_DATALIST_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(FIELD_DATALIST_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(FIELD_DATALIST_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncLinks(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(LINKS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinLinkRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(LINK_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(LINK_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(LINK_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncMarkers(DraftEntity draftEntity) {
        if (draftEntity.getCounters().isValid(MARKERS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinMarkerRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(MARKER_CREATE, countersMap.getOrDefault(Boolean.TRUE, 0));
        draftEntity.getCounters().set(MARKER_DELETE, countersMap.getOrDefault(Boolean.FALSE, 0));
    }

    private void syncTags(DraftEntity draftEntity) {
        if (draftEntity.getCounters().isValid(TAGS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinTagRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(TAG_CREATE, countersMap.getOrDefault(Boolean.TRUE, 0));
        draftEntity.getCounters().set(TAG_DELETE, countersMap.getOrDefault(Boolean.FALSE, 0));
    }

    private void syncAttachments(DraftEntity draftEntity) {
        if (draftEntity.getCounters().isValid(ATTACHMENTS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinAttachmentRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(ATTACHMENT_CREATE, countersMap.getOrDefault(CUD.CREATE, 0));
        draftEntity.getCounters().set(ATTACHMENT_UPDATE, countersMap.getOrDefault(CUD.UPDATE, 0));
        draftEntity.getCounters().set(ATTACHMENT_DELETE, countersMap.getOrDefault(CUD.DELETE, 0));
    }

    private void syncPersists(DraftEntity draftEntity) {
        if (draftEntity.getCounters().isValid(PERSISTS))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinPersistRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(PERSIST_CREATE, countersMap.getOrDefault(Boolean.TRUE, 0));
        draftEntity.getCounters().set(PERSIST_UPDATE, countersMap.getOrDefault(Boolean.FALSE, 0));
    }

    private void syncErases(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isValid(ERASES))
            return;
        Map<Object, Integer> countersMap = toMap(draftTwinEraseRepository.getCounters(draftEntity.getId()));
        draftEntity.getCounters().set(ERASE_UNDETECTED, countersMap.getOrDefault(DraftTwinEraseStatus.UNDETECTED, 0));
        draftEntity.getCounters().set(ERASE_BY_STATUS, countersMap.getOrDefault(DraftTwinEraseStatus.STATUS_CHANGE_ERASE_DETECTED, 0));
        draftEntity.getCounters().set(ERASE_LOCK, countersMap.getOrDefault(DraftTwinEraseStatus.LOCK_DETECTED, 0));
        draftEntity.getCounters().set(ERASE_IRREVOCABLE_DETECTED, countersMap.getOrDefault(DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED, 0));
        draftEntity.getCounters().set(ERASE_IRREVOCABLE_HANDLED, countersMap.getOrDefault(DraftTwinEraseStatus.IRREVOCABLE_ERASE_HANDLED, 0));
        draftEntity.getCounters().set(ERASE_CASCADE_PAUSE, countersMap.getOrDefault(DraftTwinEraseStatus.CASCADE_DELETION_PAUSE, 0));
        draftEntity.getCounters().set(ERASE_CASCADE_EXTRACTED, countersMap.getOrDefault(DraftTwinEraseStatus.CASCADE_DELETION_EXTRACTED, 0));
        draftEntity.getCounters().set(ERASE_SKIP, countersMap.getOrDefault(DraftTwinEraseStatus.SKIP_DETECTED, 0));
    }

    private Map<Object, Integer> toMap(List<Object[]> counters) {
        Map<Object, Integer> ret = new HashMap<>();
        for (Object[] row : counters) {
            ret.put(row[0], Math.toIntExact((Long) row[1]));
        }
        return ret;
    }
}
