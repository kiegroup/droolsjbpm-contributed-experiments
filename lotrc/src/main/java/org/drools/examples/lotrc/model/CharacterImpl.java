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
package org.drools.examples.lotrc.model;

/**
 * A character implementation
 * 
 * @author etirelli
 *
 */
public class CharacterImpl
    implements
    Character {

    private static final long serialVersionUID = -5261840037413369523L;
    
    private final Allegiance       allegiance;
    private final CharacterName    name;
    private final int              strength;
    private final CharacterAbility ability;
    private CharacterStatus                 status;

    public CharacterImpl(Allegiance allegiance,
                         CharacterName name,
                         int strength,
                         CharacterAbility ability) {
        this.allegiance = allegiance;
        this.name = name;
        this.strength = strength;
        this.ability = ability;
        this.status = CharacterStatus.INIT;
    }

    public CharacterStatus getStatus() {
        return status;
    }

    public void setStatus(CharacterStatus status) {
        this.status = status;
    }

    public Allegiance getAllegiance() {
        return allegiance;
    }

    public CharacterName getName() {
        return name;
    }

    public int getStrength() {
        return strength;
    }

    public CharacterAbility getAbility() {
        return ability;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.strength + "]";
    }

    @Override
    public Character clone() {
        return new CharacterImpl( allegiance,
                                  name,
                                  strength,
                                  ability );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        CharacterImpl other = (CharacterImpl) obj;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        return true;
    }
    
    

}
