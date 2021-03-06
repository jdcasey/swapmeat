package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration.GroupingParameter;
import org.commonjava.swapmeat.data.FileContentController;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.vertx.java.core.http.HttpServerRequest;

@ApplicationScoped
@Handles( value = "/api/groups/:group/files" )
public class GroupFileResource
    implements FileResource
{

    @Inject
    protected FileContentController controller;

    protected GroupFileResource()
    {
    }

    public GroupFileResource( final FileContentController controller )
    {
        this.controller = controller;
    }

    @Override
    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        controller.list( request, GroupingParameter.group );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        controller.head( request, GroupingParameter.group );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        controller.get( request, GroupingParameter.group );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.PUT )
    public void put( final HttpServerRequest request )
    {
        controller.put( request, GroupingParameter.group );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        controller.delete( request, GroupingParameter.group );
    }

}
