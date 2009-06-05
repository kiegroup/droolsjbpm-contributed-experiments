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
package org.drools.examples.lotrc.action;

import org.drools.examples.lotrc.model.Allegiance;
import org.drools.examples.lotrc.model.CharacterName;
import org.drools.examples.lotrc.model.RegionName;

/**
 * An action representing the placement of a character in a region
 * 
 * @author etirelli
 */
public class PlaceCharacterAction
    implements
    Action {

    private final Allegiance    allegiance;
    private final CharacterName character;
    private final RegionName    region;

    public PlaceCharacterAction(Allegiance allegiance,
                                CharacterName character,
                                RegionName region) {
        super();
        this.allegiance = allegiance;
        this.character = character;
        this.region = region;
    }
    
    public Allegiance getAllegiance() {
        return allegiance;
    }

    public CharacterName getCharacterName() {
        return character;
    }

    public RegionName getRegionName() {
        return region;
    }

    public PlaceCharacterAction getHidden() {
        return new PlaceCharacterAction(allegiance, CharacterName.HIDDEN, region );
    }
    
    @Override
    public String toString() {
        return "["+allegiance+"] Place " + character + " at " + region;
    }
}
