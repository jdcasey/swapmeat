package org.commonjava.swapmeat.model;

import java.util.Set;

public class Group
{

    public static final String ADMIN_GROUP = "admin";

    private String name;

    private Set<String> members;

    private Set<String> admins;

    public String getName()
    {
        return name;
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    public Set<String> getMembers()
    {
        return members;
    }

    public void setMembers( final Set<String> members )
    {
        this.members = members;
    }

    public Set<String> getAdmins()
    {
        return admins;
    }

    public void setAdmins( final Set<String> admins )
    {
        this.admins = admins;
    }

    public boolean hasAdmin( final User user )
    {
        return admins != null && admins.contains( user.getUserId() );
    }

    public boolean hasAdmin( final String userId )
    {
        return admins != null && admins.contains( userId );
    }

    public boolean hasMember( final User user )
    {
        return members != null && members.contains( user.getUserId() );
    }

    public boolean hasMember( final String userId )
    {
        return members != null && members.contains( userId );
    }

}
