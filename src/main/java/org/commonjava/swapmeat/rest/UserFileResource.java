package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.vertx.vabr.anno.Handles;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Handles( value = "/api/users/:user/files" )
public class UserFileResource
    extends AbstractFileResource
{

    protected UserFileResource()
    {
        super( GroupingType.user );
    }

    public UserFileResource( final AppConfiguration config, final ObjectMapper objectMapper )
    {
        super( GroupingType.user, config, objectMapper );
    }

}
