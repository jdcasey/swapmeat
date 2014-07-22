package org.commonjava.swapmeat.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.commonjava.swapmeat.SwapmeatException;
import org.commonjava.swapmeat.config.AppConfiguration.GroupingParameter;
import org.commonjava.swapmeat.data.EntityController;
import org.commonjava.swapmeat.model.Group;
import org.commonjava.swapmeat.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SwapmeatRealm
    extends AuthorizingRealm
    implements RolePermissionResolver
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String REALM = "SwapMeat";

    public static final String ROLE_ADMIN = "admin";

    public static final String ROLE_MEMBER = "member";

    public static final String PRIV_READ = "read";

    public static final String PRIV_WRITE = "write";

    public static final String PRIV_ADMIN = "admin";

    public static final String ROLE_GLOBAL_ADMIN = "admin:" + PRIV_ADMIN;

    public static final String PRIV_GLOBAL_ADMIN = "_global:" + PRIV_ADMIN;

    public static final String WRA = PRIV_READ + "," + PRIV_WRITE + "," + PRIV_ADMIN;

    @Inject
    protected EntityController entityController;

    protected SwapmeatRealm()
    {
        super( buildCacheManager(), buildCredentialsMatcher() );
    }

    public SwapmeatRealm( final EntityController entityController )
    {
        super( buildCacheManager(), buildCredentialsMatcher() );
        setPermissionResolver( new WildcardPermissionResolver() );
        setRolePermissionResolver( this );
        setName( REALM );
        this.entityController = entityController;
    }

    private static CacheManager buildCacheManager()
    {
        return new MemoryConstrainedCacheManager();
    }

    private static CredentialsMatcher buildCredentialsMatcher()
    {
        final HashedCredentialsMatcher cm = new HashedCredentialsMatcher( Sha512Hash.ALGORITHM_NAME );
        cm.setHashIterations( 1024 );
        cm.setStoredCredentialsHexEncoded( true );

        return cm;
    }

    @Override
    public Collection<Permission> resolvePermissionsInRole( final String roleString )
    {
        final Set<Permission> permissions = new HashSet<>();
        if ( ROLE_GLOBAL_ADMIN.equals( roleString ) )
        {
            permissions.add( new WildcardPermission( PRIV_GLOBAL_ADMIN ) );
            permissions.add( new WildcardPermission( "*:" + WRA ) );
        }
        else
        {
            final String[] parts = roleString.split( ":" );
            if ( parts.length > 1 )
            {
                if ( ROLE_ADMIN.equals( parts[1] ) )
                {
                    permissions.add( new WildcardPermission( parts[0] + ":read,write,admin" ) );
                }
                else if ( ROLE_MEMBER.equals( parts[1] ) )
                {
                    permissions.add( new WildcardPermission( parts[0] + ":read,write" ) );
                }
            }
        }

        if ( permissions.isEmpty() )
        {
            logger.error( "Unknown role(s): '{}'", roleString );
        }

        return permissions;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        final String userId = (String) principals.getPrimaryPrincipal();
        try
        {
            final User user = entityController.read( userId, GroupingParameter.user, User.class );
            final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            for ( final String groupName : user.getGroups() )
            {
                if ( Group.ADMIN_GROUP.equals( groupName ) )
                {
                    info.addRole( ROLE_GLOBAL_ADMIN );
                }
                else
                {
                    final Group group = entityController.read( groupName, GroupingParameter.group, Group.class );
                    if ( group.hasAdmin( user ) )
                    {
                        info.addRole( groupName + ":" + ROLE_ADMIN );
                    }
                    else
                    {
                        info.addRole( groupName + ":" + ROLE_MEMBER );
                    }
                }
            }

            return info;
        }
        catch ( final SwapmeatException e )
        {
            logger.error( String.format( "Failed to read authorization information for user: %s. Reason: %s", userId,
                                         e.getMessage() ), e );
        }

        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( final AuthenticationToken token )
        throws AuthenticationException
    {
        final UsernamePasswordToken upt = (UsernamePasswordToken) token;
        final String username = upt.getUsername();
        try
        {
            final User user = entityController.read( username, GroupingParameter.user, User.class );

            return new SWSaltedAuthenticationInfo( user );
        }
        catch ( final SwapmeatException e )
        {
            throw new AuthenticationException( "Failed to load user: " + username, e );
        }
    }

}
