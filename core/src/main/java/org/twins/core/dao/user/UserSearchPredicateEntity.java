package org.twins.core.dao.user;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_search_predicate")
public class UserSearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "user_search_id")
    private UUID userSearchId;

    @Column(name = "user_finder_featurer_id")
    private Integer userFinderFeaturerId;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "user_search_id", insertable = false, updatable = false, nullable = false)
    private UserSearchEntity userSearch;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "user_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> userFinderParams;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "userSearchPredicate[" + id + "]";
            default -> "userSearchPredicate[id:" + id + ", userSearchId:" + userSearchId + "]";
        };
    }
}

