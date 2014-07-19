package org.commonjava.swapmeat.rest;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang.StringUtils.join;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.commonjava.vertx.vabr.util.VertXInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//TODO: Force implementors to provide storage directory and grouping parameter name (group vs user)
public abstract class AbstractFileResource
    implements RequestHandler
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String GROUP = "group";

    private static final String NAME = "name";

    private static final String LAST_MODIFIED_HEADER = "EEE, dd MM yyyy hh:mm:ss z";

    @Inject
    protected AppConfiguration config;

    @Inject
    protected ObjectMapper objectMapper;

    private final AppConfiguration.GroupingType groupingParam;

    protected AbstractFileResource( final AppConfiguration.GroupingType groupingParam )
    {
        this.groupingParam = groupingParam;
    }

    protected AbstractFileResource( final AppConfiguration.GroupingType groupingParam, final AppConfiguration config,
                                    final ObjectMapper objectMapper )
    {
        this.groupingParam = groupingParam;
        this.config = config;
        this.objectMapper = objectMapper;
    }
    
    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        final File dir = getFile( request );
        
        if ( dir.exists() )
        {
            final String accept = request.headers().get( "Accept" );
            if ( accept.startsWith( "application/json" ) )
            {
                final Map<String, String[]> listing = Collections.singletonMap( "items", dir.list() );
                try
                {
                    final String content = objectMapper.writeValueAsString( listing );
                    request.response()
                           .setStatusCode( 200 )
                           .setStatusMessage( "Ok" )
                           .putHeader( "Content-Length", Integer.toString( content.length() ) )
                           .end( content );
                }
                catch ( final JsonProcessingException e )
                {
                    logger.error( String.format( "Failed to render directory listing to json: %s. Reason: %s", dir,
                                                 e.getMessage() ), e );
                    request.response()
                           .setStatusCode( 500 )
                           .setStatusMessage( "Internal Server Error" )
                           .end( "Failed to render json directory listing for: " + dir.getName() );
                }
            }
            else
            {
                final String content = join( dir.list(), "\n" );
                request.response()
                       .setStatusCode( 200 )
                       .setStatusMessage( "Ok" )
                       .putHeader( "Content-Length", Integer.toString( content.length() ) )
                       .end( content );
            }
        }
        else
        {
            request.response()
                   .setStatusCode( 404 )
                   .setStatusMessage( "Not Found" )
                   .end();
        }
    }

    @Route( binding = BindingType.raw, path = "/:name", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        final File file = getFile( request );
        logger.info( "HEAD: {}", file );
        final HttpServerResponse response = request.response();
        prepareResponseHeaders( response, file ).end();
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

    @Route( binding = BindingType.raw, path = "/:name", method = Method.PUT )
    public void put( final HttpServerRequest request )
    {
        request.pause();

        final File file = getFile( request );
        final boolean update = file.exists();
        
        file.getParentFile()
            .mkdirs();

        logger.info( "PUT: {}", file );

        final String contentLen = request.headers()
                                         .get( "Content-Length" );

        VertXInputStream is = null;
        OutputStream os = null;
        try
        {
            is =
                contentLen == null ? new VertXInputStream( request )
                                : new VertXInputStream( request, Long.parseLong( contentLen ) );

            os = new FileOutputStream( file );

            logger.info( "Saving stream ({} bytes)...", ( contentLen == null ? -1 : Long.parseLong( contentLen ) ) );
            copy( is, os );
            
            if ( update )
            {
                request.response()
                       .setStatusCode( 200 )
                       .setStatusMessage( "Ok" )
                       .end();
            }
            else
            {
                request.response().setStatusCode( 201 ).setStatusMessage( "Created" ).end();
            }
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Failed to save: %s. Reason: %s", file, e.getMessage() ), e );
            request.response()
                   .setStatusCode( 500 )
                   .setStatusMessage( "Internal Server Error" )
                   .end( String.format( "Failed to save: %s", file.getName() ) );
        }
        finally
        {
            closeQuietly( is );
            closeQuietly( os );
        }
    }

    @Route( binding = BindingType.raw, path = "/:name", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        final File file = getFile( request );
        if ( file.exists() )
        {
            if ( file.delete() )
            {
                request.response()
                       .setStatusCode( 204 )
                       .setStatusMessage( "No Content" )
                       .end();
            }
            else
            {
                request.response()
                       .setStatusCode( 500 )
                       .setStatusMessage( "Internal Server Error" )
                       .end( "Failed to delete: " + file.getName() );
            }
        }
        else
        {
            request.response()
                   .setStatusCode( 404 )
                   .setStatusMessage( "Not Found" )
                   .end();
        }
    }

    private File getFile( final HttpServerRequest request )
    {
        final String dir = config.getFileStorageDir( groupingParam );

        if ( dir == null )
        {
            logger.error( "Data directory not configured!" );
            return null;
        }

        final String group = request.params()
                                    .get( GROUP );

        final String name = request.params()
                                   .get( NAME );

        if ( name == null )
        {
            return Paths.get( dir, group )
                        .toFile();
        }
        else
        {
            return Paths.get( dir, group, name )
                            .toFile();
        }
    }

    private HttpServerResponse prepareResponseHeaders( final HttpServerResponse response, final File file )
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

        return response;
    }

}
