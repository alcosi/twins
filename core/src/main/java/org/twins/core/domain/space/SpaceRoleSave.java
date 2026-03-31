package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.space.SpaceRoleEntity;

@Data
@Accessors(chain = true)
public class SpaceRoleSave {
    public SpaceRoleEntity spaceRole;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}
