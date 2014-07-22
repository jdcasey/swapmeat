package org.commonjava.swapmeat.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.web.config.ConfigurationException;
import org.commonjava.web.config.DefaultConfigurationListener;
import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.SectionName;
import org.commonjava.web.config.dotconf.DotConfConfigurationReader;
import org.commonjava.web.config.section.BeanSectionListener;
import org.commonjava.web.config.section.ConfigurationSectionListener;
import org.commonjava.web.config.section.MapSectionListener;
import org.kohsuke.args4j.Option;

@SectionName( ConfigurationSectionListener.DEFAULT_SECTION )
public class BootOptions
{
    public static final String DEFAULT_BIND = "0.0.0.0";

    public static final String DATADIR_PROP = "data-dir";

    public static final int DEFAULT_PORT = 8081;

    public static final int DEFAULT_WORKERS = 5;

    @Option( name = "-c", aliases = { "--config" }, usage = "Use an alternative configuration file (default: ${app.home}/etc/swapmeat/main.conf)" )
    private String config;

    @Option( name = "-C", aliases = { "--context-path" }, usage = "Specify a root context path for all of aprox to use" )
    private String contextPath;

    @Option( name = "-d", aliases = { "--data" }, usage = "Data base directory (default: ${app.home}/var/lib/swapmeat/data" )
    private String dataDir;

    @Option( name = "-h", aliases = { "--help" }, usage = "Print this and exit" )
    private boolean help;

    @Option( name = "-i", aliases = { "--interface", "--bind", "--listen" }, usage = "Bind to a particular IP address (default: 0.0.0.0, or all available)" )
    private String bind;

    @Option( name = "-p", aliases = { "--port" }, usage = "Use different port (default: 8081)" )
    private Integer port;

    @Option( name = "-w", aliases = { "--workers" }, usage = "Number of worker threads to serve content (default: 5)" )
    private Integer workers;

    private final String appHome;

    public BootOptions( final String appHome )
    {
        this.appHome = appHome;
    }

    public void readConfig()
        throws IOException, InterpolationException, ConfigurationException
    {
        final Properties props = new Properties();
        props.put( "app.home", appHome );
        props.put( "app-home", appHome );

        if ( config == null )
        {
            config = resolve( Paths.get( appHome, "etc/swapmeat/main.conf" )
                                   .toString(), props );
        }

        final File configFile = new File( config );
        if ( configFile != null && configFile.exists() )
        {
            props.put( "app.conf", configFile.getAbsolutePath() );
            props.put( "app-conf", configFile.getAbsolutePath() );

            FileInputStream stream = null;
            try
            {
                stream = new FileInputStream( configFile );

                final PropertiesSectionListener propListener = new PropertiesSectionListener( props );

                final DefaultConfigurationListener listener =
                    new DefaultConfigurationListener( new BeanSectionListener<BootOptions>( this ), propListener );

                final DotConfConfigurationReader reader = new DotConfConfigurationReader( listener );

                reader.loadConfiguration( stream, props );
            }
            finally
            {
                IOUtils.closeQuietly( stream );
            }
        }

        if ( dataDir == null )
        {
            dataDir = Paths.get( appHome, "var/lib/swapmeat/data" )
                           .toString();
        }

        if ( bind == null )
        {
            bind = DEFAULT_BIND;
        }

        if ( port == null )
        {
            port = DEFAULT_PORT;
        }

        if ( workers == null )
        {
            workers = DEFAULT_WORKERS;
        }

        props.put( "app.data", dataDir );
        props.put( "app-data", dataDir );

        final Properties sysprops = System.getProperties();
        sysprops.putAll( props );
        System.setProperties( sysprops );
    }

    public String resolve( final String value, final Properties props )
        throws InterpolationException
    {
        if ( value == null || value.trim()
                                   .length() < 1 )
        {
            return null;
        }

        final StringSearchInterpolator interp = new StringSearchInterpolator();
        interp.addValueSource( new PropertiesBasedValueSource( props ) );

        return interp.interpolate( value );
    }

    public int getWorkers()
    {
        return workers;
    }

    public boolean isHelp()
    {
        return help;
    }

    public String getBind()
    {
        return bind;
    }

    public int getPort()
    {
        return port;
    }

    public String getConfig()
    {
        return config;
    }

    public BootOptions setHelp( final boolean help )
    {
        this.help = help;
        return this;
    }

    @ConfigName( "bind" )
    public BootOptions setBind( final String bind )
    {
        if ( this.bind == null )
        {
            this.bind = bind;
        }
        return this;
    }

    @ConfigName( "port" )
    public BootOptions setPort( final int port )
    {
        if ( this.port == null )
        {
            this.port = port;
        }
        return this;
    }

    public BootOptions setConfig( final String config )
    {
        this.config = config;
        return this;
    }

    @ConfigName( "workers" )
    public BootOptions setWorkers( final int workers )
    {
        if ( this.workers == null )
        {
            this.workers = workers;
        }
        return this;
    }

    public String getContextPath()
    {
        if ( contextPath == null )
        {
            return null;
        }

        if ( !contextPath.startsWith( "/" ) )
        {
            contextPath = "/" + contextPath;
        }

        return contextPath;
    }

    @ConfigName( "context-path" )
    public void setContextPath( final String contextPath )
    {
        if ( this.contextPath == null )
        {
            this.contextPath = contextPath;
        }
    }

    public void initConfig( final AppConfiguration config )
    {
        config.setDataDir( dataDir );
    }

    @SectionName( "variables" )
    public static final class PropertiesSectionListener
        extends MapSectionListener
    {

        private final Properties props;

        public PropertiesSectionListener( final Properties props )
        {
            this.props = props;
        }

        @Override
        public void parameter( final String name, final String value )
            throws ConfigurationException
        {
            if ( !props.containsKey( name ) )
            {
                props.setProperty( name, value );
            }
        }

    }

    public String getDataDir()
    {
        return dataDir;
    }

    @ConfigName( "data-dir" )
    public void setDataDir( final String dataDir )
    {
        if ( this.dataDir == null )
        {
            this.dataDir = dataDir;
        }
    }
}
