package org.commonjava.swapmeat.config;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfiguration
{

    private String dataDir;

    public String getDataDir()
    {
        return dataDir;
    }

    public void setDataDir( final String dataDir )
    {
        this.dataDir = dataDir;
    }

}
