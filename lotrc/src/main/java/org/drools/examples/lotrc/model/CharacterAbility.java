package org.drools.examples.lotrc.model;

/**
 * A representation of character abilities
 * 
 * @author etirelli
 */
public enum CharacterAbility implements Model {
    // Sauron characters
    IGNORE_TEXT, RETURN_TO_ROHAN, DEFEAT_ON_ATTACK, 
    FLY_TO_PREY, IGNORE_CARD, CHARGE_TO_ATTACK, 
    ATTACK_SIDEWAYS, NO_CARDS_ON_COMBAT, GUARDS_MORIA_TUNEL, 
    
    // Fellowship characters
    RETREAT_SIDEWAYS, PROTECTS_FRODO, ATTACK_ADJACENT, 
    RETREAT_ON_ATTACK, DEFEAT_WITCH_KING, DEFEAT_NAZGUL, 
    DEFEAT_ORCS, PLAY_CARD_LAST, SACRIFICE, 
    
    // Mark up value for hidden characters 
    UNKNOWN
}
