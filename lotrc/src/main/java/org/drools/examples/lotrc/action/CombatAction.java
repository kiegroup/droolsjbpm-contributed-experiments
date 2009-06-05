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
import org.drools.examples.lotrc.model.Card;
import org.drools.examples.lotrc.model.CharacterName;
import org.drools.examples.lotrc.model.RegionName;

/**
 * A combat action triggered by a move
 * 
 * @author etirelli
 */
public class CombatAction
    implements
    Action {

    private final Allegiance    allegiance;
    private final RegionName    region;
    private final CharacterName attacker;
    private CharacterName       defender;
    private CombatStatus        status;
    private Card                attackerCard;
    private Card                defenderCard;
    private int                 result;

    public CombatAction(Allegiance allegiance,
                        RegionName region,
                        CharacterName attacker) {
        super();
        this.allegiance = allegiance;
        this.region = region;
        this.attacker = attacker;
        this.status = CombatStatus.UNSETTLED;
    }

    public Allegiance getAllegiance() {
        return allegiance;
    }

    public RegionName getRegionName() {
        return region;
    }

    public CharacterName getAttackerName() {
        return attacker;
    }

    public CharacterName getDefenderName() {
        return defender;
    }

    public void setDefenderName(CharacterName defender) {
        this.defender = defender;
    }

    public CombatStatus getStatus() {
        return status;
    }

    public void setStatus(CombatStatus status) {
        this.status = status;
    }

    public CombatAction getHidden() {
        return this;
    }

    public Card getAttackerCard() {
        return attackerCard;
    }

    public void setAttackerCard(Card attackerCard) {
        this.attackerCard = attackerCard;
    }

    public Card getDefenderCard() {
        return defenderCard;
    }

    public void setDefenderCard(Card defenderCard) {
        this.defenderCard = defenderCard;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
    
    public void prepareForCombat() {
        setDefenderName( null );
        setAttackerCard( null );
        setDefenderCard( null );
        setResult( 0 );
        setStatus( CombatStatus.UNSETTLED );
    }

    @Override
    public String toString() {
        return " => "+attacker+"("+attackerCard+") attacks "+defender+"("+defenderCard+") on "+region+" : result is "+result; 
    }
}
