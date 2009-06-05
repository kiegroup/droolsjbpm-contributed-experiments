package org.drools.examples.lotrc.model;

/**
 * An interface for all characters in the game
 * 
 * @author etirelli
 */
public interface Character extends Cloneable, Model {

    public Allegiance getAllegiance();

    public CharacterName getName();

    public int getStrength();

    public CharacterAbility getAbility();

    public CharacterStatus getStatus();
    
    public void setStatus( CharacterStatus status );
    
    public Character clone();

}
