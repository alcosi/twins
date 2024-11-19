package org.twins.core.service.history;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryMutableDataCollector {
    private I18nService i18nService;
    private Set<UUID> userIdSet = new HashSet<>();
    private Set<UUID> dataListOptionIdSet = new HashSet<>();
    private Set<UUID> twinIdSet = new HashSet<>();
    private Set<UUID> linkIdSet = new HashSet<>();
    private Set<UUID> statusIdSet = new HashSet<>();

    private Kit<UserEntity, UUID> userKit;
    private Kit<DataListOptionEntity, UUID> dataListOptionKit;
    private Kit<TwinEntity, UUID> twinKit;
    private Kit<LinkEntity, UUID> linkKit;
    private Kit<TwinStatusEntity, UUID> statusKit;

    public HistoryMutableDataCollector(I18nService i18nService) {
        this.i18nService = i18nService;
    }
}
