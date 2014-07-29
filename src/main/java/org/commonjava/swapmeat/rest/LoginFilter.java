package org.commonjava.swapmeat.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.shiro.subject.Subject;
import org.commonjava.swapmeat.util.CookieMonster;
import org.commonjava.vertx.vabr.anno.FilterRoute;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.filter.ExecutionChain;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.types.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerRequest;

@ApplicationScoped
@Handles( "/api/(?!.*(login|logout)).*" )
public class LoginFilter
    implements RequestHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    protected CookieMonster cookieMonster;

    @FilterRoute( method = Method.ANY )
    public void verifyLogin( final HttpServerRequest request, final ExecutionChain chain )
        throws Exception
    {
        final Subject subject = cookieMonster.readLoginCookie( request );
        if ( subject == null )
        {

            logger.info( "sending login route" );
            request.response()
                   .setStatusCode( 302 )
                   .setStatusMessage( "Found" )
                   .putHeader( "Location", "/index.html#/login" )
                   .end();
            return;
        }

        chain.handle();
    }

}
