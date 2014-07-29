package org.commonjava.swapmeat.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.commonjava.swapmeat.config.AppConfiguration;
import org.commonjava.swapmeat.rest.AppRouter;
import org.commonjava.web.config.ConfigurationException;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.impl.DefaultVertx;

public class Booter
{
    public static final String APP_HOME_PROP = "app.home";

    public static final int ERR_CANT_DETECT_APP_HOME = 1;

    public static final int ERR_CANT_PARSE_ARGS = 2;

    public static final int ERR_CANT_INTERP_BOOT_DEFAULTS = 3;

    public static final int ERR_CANT_CONFIGURE_LOGGING = 4;

    public static final int ERR_CANT_LOAD_CONFIGURATION = 5;

    public static void main( final String[] args )
    {
        final BootOptions boot;
        try
        {
            String appHome = System.getProperty( APP_HOME_PROP );
            if ( appHome == null )
            {
                appHome = detectAppHome();
                System.setProperty( APP_HOME_PROP, appHome );
            }

            boot = new BootOptions( appHome );
        }
        catch ( final IOException e )
        {
            System.err.printf( "ERROR DETECTING ${app.home}.\nReason: %s\n\n", e.getMessage() );
            System.exit( ERR_CANT_DETECT_APP_HOME );
            return;
        }

        final CmdLineParser parser = new CmdLineParser( boot );
        boolean canStart = true;
        try
        {
            parser.parseArgument( args );
        }
        catch ( final CmdLineException e )
        {
            System.err.printf( "ERROR: %s", e.getMessage() );
            printUsage( parser, e );
            System.exit( ERR_CANT_PARSE_ARGS );
        }

        if ( boot.isHelp() )
        {
            printUsage( parser, null );
            canStart = false;
        }

        if ( canStart )
        {
            try
            {
                boot.readConfig();
            }
            catch ( IOException | InterpolationException | ConfigurationException e )
            {
                System.err.printf( "ERROR LOADING CONFIGURATION: %s.\nReason: %s\n\n", boot.getConfig(), e.getMessage() );
                System.exit( ERR_CANT_LOAD_CONFIGURATION );
                return;
            }

            final Booter booter = new Booter( boot );
            System.out.println( "Starting AProx booter: " + booter );
            final int result = booter.run();
            if ( result != 0 )
            {
                System.exit( result );
            }
        }
    }

    private static String detectAppHome()
        throws IOException
    {
        final URL url = Thread.currentThread()
                              .getContextClassLoader()
                              .getResource( Booter.class.getName()
                                                        .replace( '.', '/' ) );
        final String appHome = url.toExternalForm();

        // jar containing this class is ${app.home}/lib/swapmeat.jar
        return new File( appHome.substring( 0, appHome.indexOf( ".jar" ) ) ).getParentFile()
                                                                            .getParentFile()
                                                                            .getCanonicalPath();
    }

    public static void printUsage( final CmdLineParser parser, final CmdLineException error )
    {
        if ( error != null )
        {
            System.err.println( "Invalid option(s): " + error.getMessage() );
            System.err.println();
        }

        System.err.println( "Usage: $0 [OPTIONS]" );
        System.err.println();
        System.err.println();
        // If we are running under a Linux shell COLUMNS might be available for the width
        // of the terminal.
        parser.getProperties()
              .withUsageWidth( System.getenv( "COLUMNS" ) == null ? 100 : Integer.valueOf( System.getenv( "COLUMNS" ) ) );

        parser.printUsage( System.err );
        System.err.println();
    }

    private final BootOptions bootOptions;

    private Booter( final BootOptions bootOptions )
    {
        this.bootOptions = bootOptions;
    }

    private int run()
    {
        System.out.println( "Booter running: " + this );

        //        if ( bootOptions.getConfig() != null )
        //        {
        //            final Properties properties = System.getProperties();
        //
        //            System.out.printf( "\n\nUsing AProx configuration: %s\n", bootOptions.getConfig() );
        //            properties.setProperty( AproxConfigFactory.CONFIG_PATH_PROP, bootOptions.getConfig() );
        //            System.setProperties( properties );
        //        }

        final Weld weld = new Weld();
        final WeldContainer container = weld.initialize();

        //        final AproxConfigFactory configFactory = container.instance()
        //                                                          .select( AproxConfigFactory.class )
        //                                                          .get();
        //        try
        //        {
        //            System.out.printf( "\n\nLoading AProx configuration factory: %s\n", configFactory );
        //            configFactory.load( bootOptions.getConfig() );
        //        }
        //        catch ( final ConfigurationException e )
        //        {
        //            System.err.printf( "Failed to configure AProx: %s", e.getMessage() );
        //            e.printStackTrace();
        //            return ERR_CANT_CONFIGURE_APROX;
        //        }
        //
        //        final MasterRouter router = container.instance()
        //                                             .select( MasterRouter.class )
        //                                             .get();
        //
        //        router.setPrefix( bootOptions.getContextPath() );

        final AppConfiguration config = container.instance()
                                                 .select( AppConfiguration.class )
                                                 .get();
        bootOptions.initConfig( config );

        final Realm realm = container.instance()
                                     .select( Realm.class )
                                     .get();

        final SecurityManager sm = new DefaultSecurityManager( realm );
        SecurityUtils.setSecurityManager( sm );

        final AppRouter router = container.instance()
                                          .select( AppRouter.class )
                                          .get();

        //        router.initializeComponents();

        final DefaultVertx vertx = new DefaultVertx();

        for ( int i = 0; i < bootOptions.getWorkers(); i++ )
        {
            final HttpServer server = vertx.createHttpServer();
            server.requestHandler( router )
                  .listen( bootOptions.getPort(), bootOptions.getBind() );
        }

        System.out.printf( "AProx: %s workers listening on %s:%s\n\n", bootOptions.getWorkers(), bootOptions.getBind(),
                           bootOptions.getPort() );

        synchronized ( this )
        {
            try
            {
                wait();
            }
            catch ( final InterruptedException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return 0;
    }

}
