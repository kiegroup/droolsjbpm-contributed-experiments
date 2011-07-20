package org.drools.examples.twittercbr;

import org.drools.runtime.rule.WorkingMemoryEntryPoint;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterStatusListener
    implements
    StatusListener {

    // Drools Fusion entry point
    private WorkingMemoryEntryPoint ep;
    
    /**
     * Default constructor. Stores the session entry point.
     */
    public TwitterStatusListener(WorkingMemoryEntryPoint ep) {
        this.ep = ep;
    }

    /**
     * Whenever a new message (Status) is twitted, it is
     * inserted into the session entry point.
     */
    public void onStatus( Status status ) {
        ep.insert( status );
    }

    public void onDeletionNotice( StatusDeletionNotice statusDeletionNotice ) {}
    public void onTrackLimitationNotice( int numberOfLimitedStatuses ) {}
    public void onScrubGeo( long userId, long upToStatusId ) {}
    public void onException( Exception ex ) {}
}