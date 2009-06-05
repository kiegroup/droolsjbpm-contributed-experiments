package org.drools.examples.lotrc.player.command;

import org.drools.examples.lotrc.action.Action;

/**
 * A markup interface for Drools players command classes
 * 
 * @author etirelli
 */
public interface PlayerCommand<T extends Action> {
    
    public void setAction( T action );
    
    public T getAction();

}
