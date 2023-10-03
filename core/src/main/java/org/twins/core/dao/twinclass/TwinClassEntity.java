package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class")
public class TwinClassEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;

    @Column(name = "space")
    private boolean space;

    @Column(name = "abstract")
    private boolean abstractt;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "logo")
    private String logo;

    @Column(name = "head_twin_class_id")
    private UUID headTwinClassId;

    @Column(name = "extends_twin_class_id")
    private UUID extendsTwinClassId;

    @Column(name = "domain_alias_counter")
    private int domainAliasCounter;

    @Column(name = "twin_class_owner_type_id")
    @Convert(converter = TwinClassOwnerTypeConverter.class)
    private OwnerType ownerType;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    public String logShort() {
        return "twinClass[id:" + id + ", key:" + key + "]";
    }

    @Getter
    public enum OwnerType {
        DOMAIN("domain"),
        DOMAIN_BUSINESS_ACCOUNT("domainBusinessAccount"),
        DOMAIN_USER("domainUser"),
        DOMAIN_BUSINESS_ACCOUNT_USER("domainBusinessAccountUser");

        private final String id;

        OwnerType(String id) {
            this.id = id;
        }

        public static OwnerType valueOd(String type) {
            return Arrays.stream(OwnerType.values()).filter(t -> t.id.equals(type)).findAny().orElse(DOMAIN_BUSINESS_ACCOUNT);
        }
    }
}
