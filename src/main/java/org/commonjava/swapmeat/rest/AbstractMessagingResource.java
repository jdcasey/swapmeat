package org.commonjava.swapmeat.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.commonjava.swapmeat.cdi.Providers;
import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingType;
import org.commonjava.swapmeat.model.Notice;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.types.BindingType;
import org.commonjava.vertx.vabr.types.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Force implementors to provide storage directory and grouping parameter name (group vs user)
public abstract class AbstractMessagingResource
    implements RequestHandler
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String GROUP = "group";

    private static final String ID = "id";

    private static final String LAST_MODIFIED_HEADER = "EEE, dd MM yyyy hh:mm:ss z";

    @Inject
    protected AppConfiguration config;

    @Inject
    protected ObjectMapper objectMapper;

    private final GroupingType type;

    protected AbstractMessagingResource( final GroupingType type )
    {
        this.type = type;
    }

    protected AbstractMessagingResource( final GroupingType type, final AppConfiguration config,
                                         final ObjectMapper objectMapper )
    {
        this.type = type;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Route( binding = BindingType.raw, method = Method.GET )
    public void list( final HttpServerRequest request )
    {
        final File dir = getFile( request, null );

        if ( dir.exists() )
        {
            final List<Notice> notices = new ArrayList<Notice>();
            for ( final File file : dir.listFiles() )
            {
                try
                {
                    final Notice notice = objectMapper.readValue( file, Notice.class );
                    if ( notice != null )
                    {
                        notices.add( notice );
                    }
                }
                catch ( final IOException e )
                {
                    logger.error( String.format( "Cannot read notice: %s. Reason: %s", file, e.getMessage() ), e );
                }
            }

            final String accept = request.headers()
                                         .get( "Accept" );
            if ( accept.startsWith( "application/json" ) )
            {
                final Map<String, List<Notice>> listing = Collections.singletonMap( "items", notices );
                try
                {
                    final String json = objectMapper.writeValueAsString( listing );
                    request.response()
                           .setStatusCode( 200 )
                           .setStatusMessage( "Ok" )
                           .putHeader( "Content-Length", Integer.toString( json.length() ) )
                           .putHeader( "Content-Type", "application/json" )
                           .end( json );
                }
                catch ( final JsonProcessingException e )
                {
                    logger.error( String.format( "Failed to render JSON directory listing for: %s. Reason: %s", dir,
                                                 e.getMessage() ), e );
                    request.response()
                           .setStatusCode( 500 )
                           .setStatusMessage( "Internal Server Error" )
                           .end( "Failed to list: " + dir.getName() );
                }
            }
            else
            {
                final StringBuilder content = new StringBuilder();
                for ( final Notice notice : notices )
                {
                    if ( content.length() > 0 )
                    {
                        content.append( "\n" );
                    }

                    content.append( format( notice.getDatestamp() ) )
                           .append( ": " )
                           .append( notice.getSubject() );
                }

                request.response()
                       .setStatusCode( 200 )
                       .setStatusMessage( "Ok" )
                       .putHeader( "Content-Length", Integer.toString( content.length() ) )
                       .putHeader( "Content-Type", "text/plain" )
                       .end( content.toString() );
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

    private String format( final Date datestamp )
    {
        return new SimpleDateFormat( Providers.DATE_FORMAT ).format( datestamp );
    }

    @Route( binding = BindingType.raw, path = "/:id", method = Method.HEAD )
    public void head( final HttpServerRequest request )
    {
        final String id = getId( request );
        final File file = getFile( request, id );
        logger.info( "HEAD: {}", file );
        final HttpServerResponse response = request.response();
        prepareResponseHeaders( response, file ).end();
    }

    @Route( binding = BindingType.raw, path = "/:id", method = Method.GET )
    public void get( final HttpServerRequest request )
    {
        final String id = getId( request );
        final File file = getFile( request, id );
        logger.info( "GET: {}", file );
        final HttpServerResponse response = request.response();

        prepareResponseHeaders( response, file );

        if ( file != null && file.exists() )
        {
            try
            {
                response.end( FileUtils.readFileToString( file ) );
            }
            catch ( final IOException e )
            {
                logger.error( String.format( "Failed to read notice from: %s. Reason: %s", file, e.getMessage() ), e );
                response.headers()
                        .clear();

                response.setStatusCode( 500 )
                        .setStatusMessage( "Internal Server Error" )
                        .end( "Failed to read: " + file.getName() );
            }
        }
        else
        {
            response.end();
        }
    }

    @Route( binding = BindingType.body_handler, method = Method.POST )
    public void post( final HttpServerRequest request, final Buffer body )
    {
        request.pause();

        final String id = getId( request );
        final File file = getFile( request, id );

        file.getParentFile()
            .mkdirs();

        logger.info( "POST: {}", file );

        String json = body.getString( 0, body.length() );

        Notice notice;
        try
        {
            notice = objectMapper.readValue( json, Notice.class );
            notice.setId( id );

            if ( !notice.isValid() )
            {
                request.response()
                       .setStatusCode( 400 )
                       .setStatusMessage( "Bad Request" )
                       .end( "Invalid notice JSON" );
                return;
            }
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Cannot read notice from request body JSON. Reason: %s", e.getMessage() ), e );
            request.response()
                   .setStatusCode( 400 )
                   .setStatusMessage( "Bad Request" )
                   .end( "Cannot read Notice from body JSON" );
            return;
        }

        try
        {
            json = objectMapper.writeValueAsString( notice );
            FileUtils.write( file, json );

            request.response()
                   .setStatusCode( 201 )
                   .setStatusMessage( "Created" )
                   .putHeader( "Location", request.absoluteURI()
                                                  .toString() + "/" + notice.getId() )
                   .putHeader( "Content-Type", "application/json" )
                   .putHeader( "Content-Length", Integer.toString( json.length() ) )
                   .end( json );
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Cannot write notice to: %s. Reason: %s", file, e.getMessage() ), e );
            request.response()
                   .setStatusCode( 500 )
                   .setStatusMessage( "Internal Server Error" )
                   .end( "Cannot write Notice to file: " + file.getName() );
        }
    }

    @Route( binding = BindingType.raw, path = "/:id", method = Method.DELETE )
    public void delete( final HttpServerRequest request )
    {
        final String id = getId( request );
        final File file = getFile( request, id );
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

    private String getId( final HttpServerRequest request )
    {
        final String id = request.params()
                                 .get( ID );

        return id == null ? Long.toString( System.currentTimeMillis() ) : id;
    }

    private File getFile( final HttpServerRequest request, final String id )
    {
        final String dir = config.getFileStorageDir( type );
        if ( dir == null )
        {
            logger.error( "Data directory not configured!" );
            return null;
        }

        final String group = request.params()
                                    .get( GROUP );

        if ( id == null )
        {
            return Paths.get( dir, group )
                        .toFile();
        }
        else
        {
            return Paths.get( dir, group, id + ".json" )
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
            response.putHeader( "Content-Type", "application/json" );
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