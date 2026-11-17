package org.twins.core.dao.comment;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.annotation.ConfiguredSearch;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_comment_search")
@FieldNameConstants
public class TwinCommentSearchEntity implements EasyLoggable, ConfiguredSearch {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "force_sorting")
    private Boolean forceSorting;

    @Column(name = "comment_sorter_featurer_id")
    private Integer commentSorterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "comment_sorter_params", columnDefinition = "hstore")
    private HashMap<String, String> commentSorterParams;

    @Override
    public Integer getSorterFeaturerId() {
        return commentSorterFeaturerId;
    }

    @Override
    public HashMap<String, String> getSorterParams() {
        return commentSorterParams;
    }

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinCommentSearchEntity[" + id + "]";
            case NORMAL:
                return "twinCommentSearchEntity[id:" + id + ", name:" + name + "]";
            default:
                return "twinCommentSearchEntity[id:" + id + ", name:" + name + ", featurerId:" + commentSorterFeaturerId + ", forceSorting:" + forceSorting + "]";
        }
    }
}
