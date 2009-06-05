package org.drools.examples.lotrc.model;

/**
 * An enum to serve as characters ID
 * 
 * @author etirelli
 */
public enum CharacterName implements Model {
    
    // Sauron
    CAVE_TROLL("Cave Troll"),
    BALROG("Balrog"),
    BLACK_RIDER("Black Rider"),
    FLYING_NAZGUL("Flying Nazgul"),
    ORCS("Orcs"),
    SARUMAN("Saruman"),
    SHELOB("Shelob"),
    WARG("Warg"),
    WITCH_KING("Witch King"),
    
    // Fellowship
    ARAGORN("Aragorn"),
    BOROMIR("Boromir"),
    FRODO("Frodo"),
    GANDALF("Gandalf"),
    GIMLI("Gimli"),
    LEGOLAS("Legolas"),
    MERRY("Merry"),
    PIPPIN("Pippin"),
    SAM("Sam"), 
    
    // Mark up constant for hidden characters
    HIDDEN("[hidden]");
    
    private final String name;
    
    CharacterName( String name ) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
}
