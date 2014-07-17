package org.commonjava.swapmeat.rest;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

@ApplicationScoped
@Handles( value = "/api/:group/files" )
public class FileResource
    implements RequestHandler
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String GROUP = "group";

    private static final String NAME = "name";

    private static final String LAST_MODIFIED_HEADER = "EEE, dd MM yyyy hh:mm:ss z";

    @Inject
    private AppConfiguration config;

    @Route( binding = BindingType.raw, path = "/:name", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        final File file = getFile( request );
        logger.info( "HEAD: {}", file );
        final HttpServerResponse response = request.response();
        prepareResponseHeaders( response, file );
        response.end();
    }

    private File getFile( final HttpServerRequest request )
    {
        if ( config.getDataDir() == null )
        {
            logger.error( "Data directory not configured!" );
            return null;
        }

        final String group = request.params()
                                    .get( GROUP );

        final String name = request.params()
                                   .get( NAME );

        final File file = Paths.get( config.getDataDir(), group, name )
                               .toFile();

        logger.info( "Looking for file: {}", file );

        return file;
    }

    private void prepareResponseHeaders( final HttpServerResponse response, final File file )
    {
        if ( file != null && file.exists() )
        {
            logger.info( "OK, setting file info headers" );
            response.setStatusCode( 200 );
            response.setStatusMessage( "OK" );
            response.putHeader( "Last-Modified",
                                new SimpleDateFormat( LAST_MODIFIED_HEADER ).format( new Date( file.lastModified() ) ) );

            response.putHeader( "Content-Length", Long.toString( file.length() ) );
            response.putHeader( "Content-Type", new MimetypesFileTypeMap().getContentType( file.getAbsolutePath() ) );
        }
        else
        {
            logger.info( "NOT FOUND" );
            response.setStatusCode( 404 )
                    .setStatusMessage( "Not Found" );
        }
    }

    @Route( binding = BindingType.raw, path = "/:name", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        final File file = getFile( request );
        logger.info( "GET: {}", file );
        final HttpServerResponse response = request.response();

        prepareResponseHeaders( response, file );

        if ( file != null && file.exists() )
        {
            response.sendFile( file.getAbsolutePath() );
        }
        else
        {
            response.end();
        }
    }

}
