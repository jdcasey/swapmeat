package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.swapmeat.data.MessagingController;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

@ApplicationScoped
@Handles( value = "/api/users/:user/messages" )
public class UserMessageResource
    implements MessagingResource
{

    @Inject
    private MessagingController controller;

    protected UserMessageResource()
    {
    }

    public UserMessageResource( final MessagingController controller )
    {
        this.controller = controller;
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        controller.delete( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.body_handler, method = Method.POST )
    public void post( final HttpServerRequest request, final Buffer body )
    {
        controller.post( request, body, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        controller.get( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        controller.head( request, GroupingType.user );
    }

    @Override
    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        controller.list( request, GroupingType.user );
    }

}
