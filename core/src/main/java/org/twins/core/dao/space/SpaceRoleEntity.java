package org.twins.core.dao.space;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "space_role")
public class SpaceRoleEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18N;
//
//    @ManyToOne
//    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
//    private I18nEntity descriptionI18N;

}
