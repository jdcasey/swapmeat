package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.vertx.vabr.anno.Handles;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Handles( value = "/api/users/:user/messages" )
public class UserMessageResource
    extends AbstractMessagingResource
{

    protected UserMessageResource()
    {
        super( GroupingType.group );
    }

    public UserMessageResource( final AppConfiguration config, final ObjectMapper objectMapper )
    {
        super( GroupingType.group, config, objectMapper );
    }

}
