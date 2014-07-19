package org.commonjava.swapmeat.config;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfiguration
{

    public enum GroupingType
    {
        group, user;
    }

    private final Map<GroupingType, String> fileStorageDirs = new HashMap<>();

    private final Map<GroupingType, String> noticeStorageDirs = new HashMap<>();

    public String getFileStorageDir( final GroupingType type )
    {
        return fileStorageDirs.get( type );
    }

    public void setFileStorageDir( final GroupingType type, final String dataDir )
    {
        this.fileStorageDirs.put( type, dataDir );
    }

    public String getNoticeStorageDir( final GroupingType type )
    {
        return noticeStorageDirs.get( type );
    }

    public void setNoticeStorageDir( final GroupingType type, final String dataDir )
    {
        this.noticeStorageDirs.put( type, dataDir );
    }

}
