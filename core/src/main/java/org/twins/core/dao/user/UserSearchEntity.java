package org.twins.core.dao.user;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_search")
public class UserSearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "force_sorting")
    private boolean forceSorting;

    @Column(name = "user_sorter_featurer_id")
    private Integer userSorterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "user_sorter_params", columnDefinition = "hstore")
    private HashMap<String, String> userSorterParams;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "userSearch[id:" + id + "]";
            default -> "userSearch[id:" + id + ", name: " + name + "]";
        };
    }
}

