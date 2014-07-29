package org.commonjava.swapmeat.util;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.vertx.java.core.http.HttpServerRequest;

@ApplicationScoped
public class CookieMonster
{

    private static final String LOGIN_COOKIE = "SESSION=";

    private static final String LOGIN_COOKIE_LOWER = LOGIN_COOKIE.toLowerCase();

    private static final String COOKIE_HEADER = "Cookie";

    public Subject readLoginCookie( final HttpServerRequest request )
    {
        final List<String> cookies = request.headers()
                                            .getAll( COOKIE_HEADER );

        for ( String cookie : cookies )
        {
            if ( cookie.startsWith( LOGIN_COOKIE ) || cookie.startsWith( LOGIN_COOKIE_LOWER ) )
            {
                cookie = cookie.substring( LOGIN_COOKIE.length() );
                final String ip = request.remoteAddress()
                                         .getAddress()
                                         .getHostAddress();

                // FIXME: Need a proper HMAC, most likely
                final String sessionId = DigestUtils.sha512Hex( ip + ":" + cookie );
                final DefaultSubjectContext ctx = new DefaultSubjectContext();
                ctx.setSessionId( sessionId );

                final Subject subject = SecurityUtils.getSecurityManager()
                                                     .createSubject( ctx );

                if ( subject != null )
                {
                    ThreadContext.bind( subject );
                }

                return subject;
            }
        }

        return null;
    }

}
