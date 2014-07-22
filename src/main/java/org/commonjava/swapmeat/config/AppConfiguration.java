package org.commonjava.swapmeat.config;

import java.io.File;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfiguration
{

    public enum GroupingParameter
    {
        group, user;
    }

    private String dataDir;

    public File getEntityBaseDir( final GroupingParameter type, final String named )
    {
        return Paths.get( dataDir, type.name(), named )
                    .toFile();
    }

    public File getFileStorageDir( final GroupingParameter type, final String named )
    {
        return Paths.get( dataDir, type.name(), named, "files" )
                    .toFile();
    }

    public File getMessageStorageDir( final GroupingParameter type, final String named )
    {
        return Paths.get( dataDir, type.name(), named, "messages" )
                    .toFile();
    }

    public void setDataDir( final String dataDir )
    {
        this.dataDir = dataDir;
    }

}
