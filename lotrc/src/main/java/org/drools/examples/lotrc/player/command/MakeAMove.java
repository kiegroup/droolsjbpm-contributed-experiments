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

import org.drools.examples.lotrc.action.MoveAction;

/**
 * Make a move command
 * 
 * @author etirelli
 */
public class MakeAMove implements PlayerCommand<MoveAction> {

    private MoveAction action;

    public MakeAMove() {
    }
    
    public MoveAction getAction() {
        return action;
    }

    public void setAction(MoveAction action) {
        this.action = action;
    }
}
