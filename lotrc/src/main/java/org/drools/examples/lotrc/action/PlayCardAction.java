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
import org.drools.examples.lotrc.model.CardName;

/**
 * Represents a card played by a given player for a given combat
 * 
 * @author etirelli
 *
 */
public class PlayCardAction
    implements
    Action {
    
    private final Allegiance allegiance;
    private final CardName card;
    
    public PlayCardAction(Allegiance allegiance,
                          CardName card) {
        super();
        this.allegiance = allegiance;
        this.card = card;
    }

    /* (non-Javadoc)
     * @see org.drools.examples.lotrc.action.Action#getAllegiance()
     */
    public Allegiance getAllegiance() {
        return allegiance;
    }

    /* (non-Javadoc)
     * @see org.drools.examples.lotrc.action.Action#getHidden()
     */
    public Action getHidden() {
        return this;
    }

    public CardName getCardName() {
        return card;
    }
}
