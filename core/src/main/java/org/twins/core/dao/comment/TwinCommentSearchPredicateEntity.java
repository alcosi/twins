package org.twins.core.dao.comment;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.domain.search.ConfiguredSearchPredicate;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_comment_search_predicate")
@FieldNameConstants
public class TwinCommentSearchPredicateEntity implements EasyLoggable, ConfiguredSearchPredicate {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_comment_search_id")
    private UUID twinCommentSearchId;

    @Column(name = "comment_finder_featurer_id")
    private Integer commentFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "comment_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> commentFinderParams;

    @Override
    public Integer getFinderFeaturerId() {
        return commentFinderFeaturerId;
    }

    @Override
    public HashMap<String, String> getFinderParams() {
        return commentFinderParams;
    }

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "twin_comment_search_id", insertable = false, updatable = false, nullable = false)
    private TwinCommentSearchEntity twinCommentSearchSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinCommentSearchEntity twinCommentSearch;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinCommentSearchPredicate[" + id + "]";
            case NORMAL:
                return "twinCommentSearchPredicate[id:" + id + ", searchId:" + twinCommentSearchId + "]";
            default:
                return "twinCommentSearchPredicate[id:" + id + ", searchId:" + twinCommentSearchId + ", featurerId:" + commentFinderFeaturerId + "]";
        }
    }
}
