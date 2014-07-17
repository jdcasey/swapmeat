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
import org.kohsuke.args4j.Option;

public class BootOptions
{
    public static final String BIND_PROP = "bind";

    public static final String PORT_PROP = "port";

    public static final String CONFIG_PROP = "config";

    public static final String WORKERS_PROP = "workers";

    public static final String CONTEXT_PATH_PROP = "context-path";

    public static final String DATA_DIR_PROP = "data-dir";

    public static final String DEFAULT_BIND = "0.0.0.0";

    public static final int DEFAULT_PORT = 8081;

    public static final int DEFAULT_WORKERS_COUNT = 5;

    @Option( name = "-h", aliases = { "--help" }, usage = "Print this and exit" )
    private boolean help;

    @Option( name = "-i", aliases = { "--interface", "--bind", "--listen" }, usage = "Bind to a particular IP address (default: 0.0.0.0, or all available)" )
    private String bind;

    @Option( name = "-p", aliases = { "--port" }, usage = "Use different port (default: 8080)" )
    private Integer port;

    @Option( name = "-c", aliases = { "--config" }, usage = "Use an alternative configuration file (default: <aprox-home>/etc/aprox/main.conf)" )
    private String config;

    @Option( name = "-w", aliases = { "--workers" }, usage = "Number of worker threads to serve content (default: 5)" )
    private Integer workers;

    @Option( name = "-C", aliases = { "--context-path" }, usage = "Specify a root context path for all of aprox to use" )
    private String contextPath;

    private StringSearchInterpolator interp;

    private Properties bootProps;

    private final String appHome;

    @Option( name = "-d", aliases = { "--data" }, usage = "Directory where files should be stored (default: ${app.home}/data)" )
    private String dataDir;

    public BootOptions( final File bootDefaults, final String aproxHome )
        throws IOException, InterpolationException
    {
        this.appHome = aproxHome;
        this.bootProps = new Properties();

        if ( bootDefaults != null && bootDefaults.exists() )
        {
            FileInputStream stream = null;
            try
            {
                stream = new FileInputStream( bootDefaults );

                bootProps.load( stream );
            }
            finally
            {
                IOUtils.closeQuietly( stream );
            }
        }

        if ( bind == null )
        {
            bind = resolve( bootProps.getProperty( BIND_PROP, DEFAULT_BIND ) );
        }

        if ( port == null )
        {
            port = Integer.parseInt( resolve( bootProps.getProperty( PORT_PROP, Integer.toString( DEFAULT_PORT ) ) ) );
        }

        if ( workers == null )
        {
            workers =
                Integer.parseInt( resolve( bootProps.getProperty( WORKERS_PROP,
                                                                  Integer.toString( DEFAULT_WORKERS_COUNT ) ) ) );
        }

        if ( config == null )
        {
            final String defaultConfigPath = new File( aproxHome, "etc/aprox/main.conf" ).getPath();
            config = resolve( bootProps.getProperty( CONFIG_PROP, defaultConfigPath ) );
        }

        if ( dataDir == null )
        {
            final String defDataDir = Paths.get( aproxHome, "data" )
                                           .toString();
            dataDir = resolve( bootProps.getProperty( DATA_DIR_PROP, defDataDir ) );
        }

        contextPath = bootProps.getProperty( CONTEXT_PATH_PROP, contextPath );
    }

    public String resolve( final String value )
        throws InterpolationException
    {
        if ( value == null || value.trim()
                                   .length() < 1 )
        {
            return null;
        }

        if ( bootProps == null )
        {
            if ( appHome == null )
            {
                return value;
            }
            else
            {
                bootProps = new Properties();
            }
        }

        bootProps.setProperty( "app.home", appHome );

        if ( interp == null )
        {
            interp = new StringSearchInterpolator();
            interp.addValueSource( new PropertiesBasedValueSource( bootProps ) );
        }

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

    public BootOptions setBind( final String bind )
    {
        this.bind = bind;
        return this;
    }

    public BootOptions setPort( final int port )
    {
        this.port = port;
        return this;
    }

    public BootOptions setConfig( final String config )
    {
        this.config = config;
        return this;
    }

    public BootOptions setWorkers( final int workers )
    {
        this.workers = workers;
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

    public void setContextPath( final String contextPath )
    {
        this.contextPath = contextPath;
    }

    public void setDataDir( final String dataDir )
    {
        this.dataDir = dataDir;
    }

    public String getDataDir()
    {
        return dataDir;
    }

    public void initConfig( final AppConfiguration config )
    {
        config.setDataDir( getDataDir() );
    }

}
