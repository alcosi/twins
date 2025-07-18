package org.twins.core.featurer.fieldfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.cambium.common.util.UuidUtils;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3205,
        name = "Stub (does not return fields)",
        description = "")
public class FieldFinderStub extends FieldFinder {

    @Override
    protected void createSearch(Properties properties, UUID twinClassId, TwinClassFieldSearch fieldSearch) throws ServiceException {
        fieldSearch.setIdList(Set.of(UuidUtils.NULLIFY_MARKER));
    }
}
