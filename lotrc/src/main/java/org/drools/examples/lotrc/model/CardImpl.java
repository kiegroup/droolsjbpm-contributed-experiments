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
 * A card implementation
 * 
 * @author etirelli
 */
public class CardImpl
    implements
    Card {

    private static final long serialVersionUID = 417307077760796758L;

    private final Allegiance  allegiance;
    private final CardName    name;
    private final int         power;
    private final CardAbility ability;
    private CardStatus status;

    public CardImpl(final Allegiance allegiance,
                    final CardName name,
                    final int power,
                    final CardAbility ability) {
        this.allegiance = allegiance;
        this.name = name;
        this.power = power;
        this.ability = ability;
        this.status = CardStatus.UNPLAYED;
    }

    public Allegiance getAllegiance() {
        return allegiance;
    }
    
    public CardName getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public CardAbility getAbility() {
        return ability;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Card( ["+allegiance+"] Power: "+power+" / "+ability+" ("+status+") )";
    }

}
