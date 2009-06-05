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
 * A Move action representation
 * 
 * @author etirelli
 */
public class MoveAction
    implements
    Action {
    
    private final Allegiance allegiance;
    private final CharacterName character;
    private final RegionName fromRegion;
    private final RegionName toRegion;
    private MoveStatus status;
    
    public MoveAction(Allegiance allegiance,
                      CharacterName character,
                      RegionName fromRegion,
                      RegionName toRegion) {
        super();
        this.allegiance = allegiance;
        this.character = character;
        this.fromRegion = fromRegion;
        this.toRegion = toRegion;
        this.status = MoveStatus.ISSUED;
    }
    
    public Allegiance getAllegiance() {
        return allegiance;
    }
    
    public CharacterName getCharacterName() {
        return character;
    }
    
    public RegionName getFromRegion() {
        return fromRegion;
    }
    
    public RegionName getToRegion() {
        return toRegion;
    }
    
    public MoveStatus getStatus() {
        return status;
    }

    public void setStatus(MoveStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "["+allegiance+"]("+status+") Moves "+character+" from "+fromRegion+" to "+toRegion+".";
    }

    public MoveAction getHidden() {
        return new MoveAction(allegiance, CharacterName.HIDDEN, fromRegion, toRegion );
    }

}
