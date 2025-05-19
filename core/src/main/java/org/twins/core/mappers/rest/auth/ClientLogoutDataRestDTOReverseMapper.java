package org.twins.core.mappers.rest.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Map;

@Component
@AllArgsConstructor
public class ClientLogoutDataRestDTOReverseMapper extends RestSimpleDTOMapper<Map<String, String>, ClientLogoutData> {

    @Override
    public void map(Map<String, String> src, ClientLogoutData dst, MapperContext mapperContext) throws Exception {
        dst
                .putAll(src);
    }
}
