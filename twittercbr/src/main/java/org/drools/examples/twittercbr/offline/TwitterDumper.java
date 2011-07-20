/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.twittercbr.offline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusStream;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * TwitterDumper
 */
public class TwitterDumper {
    
    public static void main(String[] args) throws TwitterException, IOException, InterruptedException{
        final ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( "src/main/resources/twitterstream.dump" ) );
        
        final TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        
        final StatusListener listener = new StatusListener() {
            private int counter = 0;
            
            @Override public void onException( Exception arg0 ) { }
            @Override public void onDeletionNotice( StatusDeletionNotice arg0 ) { }
            @Override public void onScrubGeo( long arg0, long arg1 ) { }
            @Override public void onTrackLimitationNotice( int arg0 ) { }
            
            @Override
            public void onStatus( Status arg0 ) {
                counter++;
                if( counter % 100 == 0 ) {
                    System.out.println("Message = "+counter);
                }
                try {
                    oos.writeObject( arg0 );
                } catch ( IOException e ) {
                    e.printStackTrace();
                    System.exit( 0 );
                }
            }
        };
        StatusStream stream = twitterStream.getSampleStream();
        long start = System.currentTimeMillis();
        while( System.currentTimeMillis()-start < 300000 ) {
            stream.next( listener );
        }
        twitterStream.shutdown();
        Thread.currentThread().sleep( 10000 );
        oos.close();
    }
    

}
