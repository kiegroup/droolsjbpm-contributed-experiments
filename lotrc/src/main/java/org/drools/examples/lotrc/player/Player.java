/*
 * Copyright 2008 Red Hat
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
 *
 */
package org.drools.examples.lotrc.player;

import org.drools.examples.lotrc.action.CharacterDefeatedAction;
import org.drools.examples.lotrc.action.CombatAction;
import org.drools.examples.lotrc.action.MoveAction;
import org.drools.examples.lotrc.action.PlaceCharacterAction;
import org.drools.examples.lotrc.action.PlayCardAction;
import org.drools.examples.lotrc.model.Allegiance;
import org.drools.examples.lotrc.model.CharacterName;

/**
 * A player abstraction
 * 
 * @author etirelli
 */
public interface Player {

    /**
     * Returns the allegiance of the player
     */
    public Allegiance getAllegiance();

    /**
     * Sets up all characters in the board
     */
    public PlaceCharacterAction setupCharacter( CharacterName character );
    
    /**
     * Makes a move returning the corresponding move action
     */
    public MoveAction makeAMove();
    
    /**
     * Plays a card for the given combat action
     * 
     * @param combat the current combat
     * 
     * @return the card to 
     */
    public PlayCardAction playACard( CombatAction combat );
    
    /**
     * Notifies a player that a PlaceCharacterAction was acknowledged by the system
     * 
     * @param action 
     */
    public void notify( PlaceCharacterAction action );
    
    /**
     * Notifies a player that a MoveAction was acknowledge by the system
     * 
     * @param action
     */
    public void notify( MoveAction action );
    
    /**
     * Notifies a player that a PlayCardAction was acknowledge by the system
     * 
     * @param action
     */
    public void notify( PlayCardAction action );
    
    /**
     * Notifies a player that a Character was defeated
     * 
     * @param action
     */
    public void notify( CharacterDefeatedAction action );
    
    public void printBoard();    

}
