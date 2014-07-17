package org.commonjava.swapmeat.rest;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.commonjava.vertx.vabr.ApplicationRouter;
import org.commonjava.vertx.vabr.filter.FilterCollection;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.route.RouteCollection;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AppRouter
    extends ApplicationRouter
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private Instance<RequestHandler> handlerInstances;

    @Inject
    private Instance<RouteCollection> routeCollectionInstances;

    @Inject
    private Instance<FilterCollection> filterCollectionInstances;

    protected AppRouter()
    {
    }

    public AppRouter( final Set<RequestHandler> handlers, final Set<RouteCollection> routeCollections,
                      final Set<FilterCollection> filterCollections )
    {
        super( handlers, routeCollections );
        bindFilters( handlers, filterCollections );
    }

    public void containerInit( @Observes final Event<ContainerInitialized> evt )
    {
        initializeComponents();
    }

    @PostConstruct
    public void initializeComponents()
    {
        logger.info( "\n\nCONSTRUCTING WEB ROUTES...\n\n" );

        final Set<RouteCollection> routes = new HashSet<>();
        for ( final RouteCollection collection : routeCollectionInstances )
        {
            routes.add( collection );
        }

        final Set<FilterCollection> filters = new HashSet<>();
        for ( final FilterCollection collection : filterCollectionInstances )
        {
            filters.add( collection );
        }

        bindRoutes( handlerInstances, routes );
        bindFilters( handlerInstances, filters );

        logger.info( "\n\n...done.\n\n" );
    }

}
