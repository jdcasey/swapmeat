package org.commonjava.swapmeat.model;

import java.io.Serializable;
import java.util.Set;

public class User
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String passwordSalt;

    private String passwordEncrypted;

    private Set<String> groups;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( final String userId )
    {
        this.userId = userId;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( final String firstName )
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( final String lastName )
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( final String email )
    {
        this.email = email;
    }

    public String getPasswordSalt()
    {
        return passwordSalt;
    }

    public void setPasswordSalt( final String passwordSalt )
    {
        this.passwordSalt = passwordSalt;
    }

    public String getHashedPassword()
    {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted( final String passwordEncrypted )
    {
        this.passwordEncrypted = passwordEncrypted;
    }

    public Set<String> getGroups()
    {
        return groups;
    }

    public void setGroups( final Set<String> groups )
    {
        this.groups = groups;
    }

}
