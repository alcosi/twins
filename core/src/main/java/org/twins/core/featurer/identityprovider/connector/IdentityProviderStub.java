package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

@Component
@Featurer(id = FeaturerTwins.ID_3301,
        name = "Stub",
        description = "")
@RequiredArgsConstructor
public class IdentityProviderStub extends IdentityProviderConnector {

}
