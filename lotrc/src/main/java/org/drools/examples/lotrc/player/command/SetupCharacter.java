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
package org.drools.examples.lotrc.player.command;

import org.drools.examples.lotrc.action.PlaceCharacterAction;
import org.drools.examples.lotrc.model.CharacterName;

/**
 * A setup character command
 * 
 * @author etirelli
 */
public class SetupCharacter implements PlayerCommand<PlaceCharacterAction> {

    private CharacterName name;
    private PlaceCharacterAction action;
    
    public SetupCharacter(CharacterName name) {
        super();
        this.name = name;
    }
    public CharacterName getName() {
        return name;
    }
    public void setName(CharacterName name) {
        this.name = name;
    }
    public PlaceCharacterAction getAction() {
        return action;
    }
    public void setAction(PlaceCharacterAction action) {
        this.action = action;
    }

}
