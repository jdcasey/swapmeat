package org.commonjava.swapmeat.model;

import java.util.Date;

public class Message
{

    private String id;

    private String from;

    private String subject;

    private String message;

    private Date datestamp;

    public String getId()
    {
        return id;
    }

    public void setId( final String id )
    {
        this.id = id;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject( final String subject )
    {
        this.subject = subject;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( final String message )
    {
        this.message = message;
    }

    public Date getDatestamp()
    {
        return datestamp;
    }

    public void setDatestamp( final Date datestamp )
    {
        this.datestamp = datestamp;
    }

    public boolean isValid()
    {
        return id != null && subject != null && message != null && datestamp != null;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom( final String from )
    {
        this.from = from;
    }

}
