package org.twins.core.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.history.*;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.user.UserRepository;

@Service
@Getter
@RequiredArgsConstructor
public class RepositoryService {
    final TwinRepository twinRepository;
    final TwinStatusRepository twinStatusRepository;
    final DataListOptionRepository dataListOptionRepository;
    final UserRepository userRepository;
    final LinkRepository linkRepository;
    final HistoryRepository historyRepository;
    final HistoryTypeRepository historyTypeRepository;
    final HistoryTypeConfigDomainRepository historyTypeConfigDomainRepository;
    final HistoryTypeConfigTwinClassRepository historyTypeConfigTwinClassRepository;
    final HistoryTypeConfigTwinClassFieldRepository historyTypeConfigTwinClassFieldRepository;
}
