package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.vertx.vabr.anno.Handles;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Handles( value = "/api/groups/:group/notices" )
public class GroupNoticeResource
    extends AbstractMessagingResource
{

    protected GroupNoticeResource()
    {
        super( GroupingType.group );
    }

    public GroupNoticeResource( final AppConfiguration config, final ObjectMapper objectMapper )
    {
        super( GroupingType.group, config, objectMapper );
    }

}
