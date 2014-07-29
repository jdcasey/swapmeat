package org.commonjava.swapmeat.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LoginFilterTest
{

    @Test
    public void filterRegex()
    {
        assertThat( "/api/groups/foo/messages".matches( "/api/(?!login|logout).*" ), equalTo( true ) );
    }

}
