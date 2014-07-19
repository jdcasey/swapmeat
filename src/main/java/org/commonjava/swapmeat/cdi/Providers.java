package org.commonjava.swapmeat.cdi;

import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class Providers
{

    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss z";

    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize()
    {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat( new SimpleDateFormat( DATE_FORMAT ) );
    }

    @Produces
    @Default
    public ObjectMapper getMapper()
    {
        return objectMapper;
    }

}
