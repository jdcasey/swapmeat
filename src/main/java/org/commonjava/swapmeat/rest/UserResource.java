package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.helper.RequestHandler;

@ApplicationScoped
@Handles( value = "/api/users" )
public class UserResource
    implements RequestHandler
{

}
