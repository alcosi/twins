package org.twins.core.featurer.identityprovider.trustor;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_35,
        name = "Trustor",
        description = "Featurer to resolve act as user")
@Slf4j
public abstract class Trustor extends FeaturerTwins {
    public CryptKey.CryptPublicKey getActAsUserPublicKey(HashMap<String, String> trustorParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, trustorParams);
        return getActAsUserPublicKey(properties);
    }

    public abstract CryptKey.CryptPublicKey getActAsUserPublicKey(Properties properties) throws ServiceException;

    public ActAsUser resolveActAsUser(HashMap<String, String> trustorParams, String actAsUserHeader) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, trustorParams);
        return resolveActAsUser(properties, actAsUserHeader);
    }

    public abstract ActAsUser resolveActAsUser(Properties properties, String actAsUserHeader) throws ServiceException;
}
