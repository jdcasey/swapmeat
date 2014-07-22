package org.commonjava.swapmeat;

import java.text.MessageFormat;
import java.util.IllegalFormatException;

public class SwapmeatException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    private Object[] params;

    private transient String formatted;

    public SwapmeatException( final String format, final Object... params )
    {
        super( format );
        this.params = params;
    }

    public SwapmeatException( final String format, final Throwable error, final Object... params )
    {
        super( format, error );
        this.params = params;
    }

    @Override
    public String getMessage()
    {
        if ( formatted == null )
        {
            formatted = super.getMessage();

            if ( params != null )
            {
                try
                {
                    formatted = String.format( formatted.replaceAll( "\\{\\}", "%s" ), params );
                }
                catch ( final IllegalFormatException ife )
                {
                    try
                    {
                        formatted = MessageFormat.format( formatted, params );
                    }
                    catch ( final IllegalArgumentException iae )
                    {
                    }
                }
            }
        }

        return formatted;
    }

    private Object writeReplace()
    {
        final Object[] newParams = new Object[params.length];
        int i = 0;
        for ( final Object object : params )
        {
            newParams[i] = String.valueOf( object );
            i++;
        }

        this.params = newParams;
        return this;
    }

}
