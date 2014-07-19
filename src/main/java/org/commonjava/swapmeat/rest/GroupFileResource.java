package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.vertx.vabr.anno.Handles;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Handles( value = "/api/groups/:group/files" )
public class GroupFileResource
    extends AbstractFileResource
{

    protected GroupFileResource()
    {
        super( GroupingType.group );
    }

    public GroupFileResource( final AppConfiguration config, final ObjectMapper objectMapper )
    {
        super( GroupingType.group, config, objectMapper );
    }

}
