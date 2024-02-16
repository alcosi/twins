package org.twins.core.domain.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class SpaceRoleUserSearchRqDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserSearchDTOv1, SpaceRoleUserSearch> {

    @Override
    public void map(SpaceRoleUserSearchDTOv1 src, SpaceRoleUserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameLike(src.getNameLike())
                .setRolesList(convertSafe(src.getRolesList()));
    }

    private <T> Set<T> convertSafe(List<T> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }

}
