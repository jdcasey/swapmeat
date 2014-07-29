package org.commonjava.swapmeat.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.commonjava.swapmeat.SwapmeatException;
import org.commonjava.swapmeat.aaa.SwapmeatRealm;
import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingParameter;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class EntityController
{
    private static final String ENTITY_FILE = "entity.json";

    @Inject
    private AppConfiguration config;

    @Inject
    private ObjectMapper objectMapper;

    protected EntityController()
    {
    }

    protected EntityController( final AppConfiguration config, final ObjectMapper objectMapper )
    {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public <T> T read( final String name, final GroupingParameter grouping, final Class<T> entityType )
        throws SwapmeatException
    {
        final Subject subject = SecurityUtils.getSubject();
        subject.checkPermission( SwapmeatRealm.readPermission( grouping, name ) );

        final File entity = Paths.get( config.getEntityBaseDir( grouping, name )
                                             .getAbsolutePath(), ENTITY_FILE )
                                 .toFile();
        if ( entity.exists() )
        {
            try
            {
                return objectMapper.readValue( entity, entityType );
            }
            catch ( final IOException e )
            {
                throw new SwapmeatException( "Cannot read %s '%s' from: %s. Reason: %s", e, grouping, name, entity,
                                             e.getMessage() );
            }
        }

        return null;
    }

    public <T> void write( final String name, final T instance, final GroupingParameter grouping )
        throws SwapmeatException
    {
        final Subject subject = SecurityUtils.getSubject();
        subject.checkPermission( SwapmeatRealm.adminPermission( grouping, name ) );

        final File entity = Paths.get( config.getEntityBaseDir( grouping, name )
                                             .getAbsolutePath(), ENTITY_FILE )
                                 .toFile();

        if ( !entity.exists() && !entity.mkdirs() )
        {
            throw new SwapmeatException( "Cannot create %s directory for: '%s'", grouping, name );
        }

        try
        {
            objectMapper.writeValue( entity, instance );
        }
        catch ( final IOException e )
        {
            throw new SwapmeatException( "Cannot write %s '%s' to: %s. Reason: %s", e, grouping, name, entity,
                                         e.getMessage() );
        }
    }

    public void delete( final String name, final GroupingParameter grouping )
        throws SwapmeatException
    {
        final Subject subject = SecurityUtils.getSubject();
        subject.checkPermission( SwapmeatRealm.adminPermission( grouping, name ) );

        final File entity = Paths.get( config.getEntityBaseDir( grouping, name )
                                             .getAbsolutePath(), ENTITY_FILE )
                                 .toFile();

        if ( entity.exists() )
        {
            try
            {
                FileUtils.forceDelete( entity );
            }
            catch ( final IOException e )
            {
                throw new SwapmeatException( "Cannot delete %s '%s' to: %s. Reason: %s", e, grouping, name, entity,
                                             e.getMessage() );
            }
        }
    }

}
