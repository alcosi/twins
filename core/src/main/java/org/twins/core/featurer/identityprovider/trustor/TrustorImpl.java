package org.twins.core.featurer.identityprovider.trustor;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_3501,
        name = "Impl",
        description = "No encryption")
@RequiredArgsConstructor
public class TrustorImpl extends Trustor {
    @Override
    public CryptKey.CryptPublicKey getActAsUserPublicKey(Properties properties) throws ServiceException {
        return null; //no encryption needed
    }

    @Override
    public ActAsUser resolveActAsUser(Properties properties, String actAsUserHeader) throws ServiceException {
        String[] actAsUserData = actAsUserHeader.split(",");
        ActAsUser ret = new ActAsUser()
                .setUserId(UUID.fromString(actAsUserData[0].trim()));
        if (actAsUserData.length > 1)
            ret.setBusinessAccountId(UUID.fromString(actAsUserData[1].trim()));
        return ret;
    }
}
