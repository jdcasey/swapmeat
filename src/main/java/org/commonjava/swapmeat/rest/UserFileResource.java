package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.swapmeat.data.FileController;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.vertx.java.core.http.HttpServerRequest;

@ApplicationScoped
@Handles( value = "/api/users/:user/files" )
public class UserFileResource
    implements FileResource
{

    @Inject
    protected FileController controller;

    protected UserFileResource()
    {
    }

    public UserFileResource( final FileController controller )
    {
        this.controller = controller;
    }

    @Override
    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        controller.list( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        controller.head( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        controller.get( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.PUT )
    public void put( final HttpServerRequest request )
    {
        controller.put( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:name", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        controller.delete( request, GroupingType.user );
    }

}
