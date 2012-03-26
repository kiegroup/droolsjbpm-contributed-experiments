package com.drools.memory.banchmark.model.events;

import java.net.URL;
import java.util.Date;

/**
 * @author Victor
 */
public class MainEvent extends VisitorEvent{
    private URL referer;
    private String pageTitle;


    public MainEvent(URL referer, URL pageUrl, String visitorId, String configId, Date eventTimestamp,String title){
        super(pageUrl,visitorId,configId,eventTimestamp);
        this.pageTitle = title;
        this.referer = referer;
    }


    public String getPageTitle() {
        return pageTitle;
    }

    public URL getReferer() {
        return referer;
    }


}