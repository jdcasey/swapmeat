package org.commonjava.swapmeat.aaa;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import org.commonjava.swapmeat.model.User;

public final class SwapmeatSaltedAuthenticationInfo
    implements SaltedAuthenticationInfo
{

    private static final long serialVersionUID = 1L;

    private final User user;

    public SwapmeatSaltedAuthenticationInfo( final User user )
    {
        this.user = user;
    }

    @Override
    public PrincipalCollection getPrincipals()
    {
        return new SimplePrincipalCollection( user.getUserId(), SwapmeatRealm.REALM );
    }

    @Override
    public Object getCredentials()
    {
        return user.getHashedPassword();
    }

    @Override
    public ByteSource getCredentialsSalt()
    {
        return new SimpleByteSource( Base64.decodeBase64( user.getPasswordSalt() ) );
    }

}