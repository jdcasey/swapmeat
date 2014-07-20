package org.commonjava.swapmeat.rest;

import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.vertx.java.core.http.HttpServerRequest;

public interface FileResource
    extends RequestHandler
{

    public abstract void list( HttpServerRequest request );

    public abstract void head( HttpServerRequest request );

    public abstract void get( HttpServerRequest request );

    public abstract void put( HttpServerRequest request );

    public abstract void delete( HttpServerRequest request );

}