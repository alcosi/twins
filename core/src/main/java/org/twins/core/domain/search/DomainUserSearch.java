package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserStatus;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainUserSearch {
    public Set<UUID> userIdList;
    public Set<UUID> userIdExcludeList;
    public Set<String> nameLikeList;
    public Set<String> nameNotLikeList;
    public Set<String> emailLikeList;
    public Set<String> emailNotLikeList;
    public Set<UserStatus> statusIdList;
    public Set<UserStatus> statusIdExcludeList;
}
