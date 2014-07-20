package org.commonjava.swapmeat.rest;

import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

interface MessagingResource
    extends RequestHandler
{
    void delete( HttpServerRequest request );

    void post( HttpServerRequest request, Buffer body );

    void get( HttpServerRequest request );

    void head( HttpServerRequest request );

    void list( HttpServerRequest request );

}
