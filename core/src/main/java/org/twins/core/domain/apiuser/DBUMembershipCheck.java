package org.twins.core.domain.apiuser;

public enum DBUMembershipCheck {
    BLOCKED,
    DB, // domain_business_account_map
    DU, // domain_user_map
    BU, // business_account_user_map
    DBU_FOR_USER, // domain_business_account_map + business_account_user_map + domain_user_map
    DBU_FOR_BUSINESS_ACCOUNT // domain_business_account_map + business_account_user_map + domain_user_map
}
