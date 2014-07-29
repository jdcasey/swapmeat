package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration.GroupingParameter;
import org.commonjava.swapmeat.data.MessagingContentController;
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
    private MessagingContentController controller;

    protected UserMessageResource()
    {
    }

    public UserMessageResource( final MessagingContentController controller )
    {
        this.controller = controller;
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        controller.delete( request, GroupingParameter.user );
    }

    @Override
    @Route( binding = BindingType.body_handler, method = Method.POST )
    public void post( final HttpServerRequest request, final Buffer body )
    {
        controller.post( request, body, GroupingParameter.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        controller.get( request, GroupingParameter.user );
    }

    @Override
    @Route( binding = BindingType.raw, path = "/:id", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        controller.head( request, GroupingParameter.user );
    }

    @Override
    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        controller.list( request, GroupingParameter.user );
    }

}
