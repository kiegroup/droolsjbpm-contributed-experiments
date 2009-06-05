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

import java.util.ArrayList;
import java.util.List;

import org.drools.examples.lotrc.player.Player;

/**
 * A class to manage the game
 * 
 * @author etirelli
 */
public class Game implements Model {

    private static final long serialVersionUID = 7644689948063266496L;
    
    private final Player          sauron;
    private final Player          fellowship;
    private final Board           board;
    private final List<Character> characters;
    private final List<Card>      cards;

    private Player                activePlayer;

    public Game(Player sauron,
                Player fellowship) {
        this.sauron = sauron;
        this.fellowship = fellowship;
        this.characters = new ArrayList<Character>();
        this.cards = new ArrayList<Card>();
        this.board = new Board();
        this.activePlayer = sauron;

        // create the cards for Sauron
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P1,
                                 1,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P2,
                                 2,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P3,
                                 3,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P4,
                                 4,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P5,
                                 5,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.P6,
                                 6,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.MAGIC,
                                 0,
                                 CardAbility.MAGIC ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.EYE_OF_SAURON,
                                 0,
                                 CardAbility.IGNORE_TEXT ) );
        cards.add( new CardImpl( Allegiance.SAURON,
                                 CardName.RETREAT,
                                 0,
                                 CardAbility.RETREAT_SIDEWAYS ) );

        // create the cards for the Fellowship
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.P1,
                                 1,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.P2,
                                 2,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.P3,
                                 3,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.P4,
                                 4,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.P5,
                                 5,
                                 CardAbility.NONE ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.NOBLE_SACRIFICE,
                                 0,
                                 CardAbility.SACRIFICE_BOTH ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.MAGIC,
                                 0,
                                 CardAbility.MAGIC ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.ELVEN_CLOAK,
                                 0,
                                 CardAbility.IGNORE_POWER ) );
        cards.add( new CardImpl( Allegiance.FELLOWSHIP,
                                 CardName.RETREAT,
                                 0,
                                 CardAbility.RETREAT_BACKWARDS ) );

        // create the characters for Sauron
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.WARG,
                                           2,
                                           CharacterAbility.IGNORE_TEXT ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.SHELOB,
                                           5,
                                           CharacterAbility.RETURN_TO_ROHAN ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.ORCS,
                                           2,
                                           CharacterAbility.DEFEAT_ON_ATTACK ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.FLYING_NAZGUL,
                                           3,
                                           CharacterAbility.FLY_TO_PREY ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.CAVE_TROLL,
                                           9,
                                           CharacterAbility.IGNORE_CARD ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.BLACK_RIDER,
                                           3,
                                           CharacterAbility.CHARGE_TO_ATTACK ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.WITCH_KING,
                                           5,
                                           CharacterAbility.ATTACK_SIDEWAYS ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.SARUMAN,
                                           4,
                                           CharacterAbility.NO_CARDS_ON_COMBAT ) );
        characters.add( new CharacterImpl( Allegiance.SAURON,
                                           CharacterName.BALROG,
                                           5,
                                           CharacterAbility.GUARDS_MORIA_TUNEL ) );

        // create the characters for the Fellowship
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.FRODO,
                                           1,
                                           CharacterAbility.RETREAT_SIDEWAYS ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.SAM,
                                           2,
                                           CharacterAbility.PROTECTS_FRODO ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.ARAGORN,
                                           4,
                                           CharacterAbility.ATTACK_ADJACENT ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.PIPPIN,
                                           1,
                                           CharacterAbility.RETREAT_ON_ATTACK ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.MERRY,
                                           2,
                                           CharacterAbility.DEFEAT_WITCH_KING ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.LEGOLAS,
                                           3,
                                           CharacterAbility.DEFEAT_NAZGUL ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.GIMLI,
                                           3,
                                           CharacterAbility.DEFEAT_ORCS ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.GANDALF,
                                           5,
                                           CharacterAbility.PLAY_CARD_LAST ) );
        characters.add( new CharacterImpl( Allegiance.FELLOWSHIP,
                                           CharacterName.BOROMIR,
                                           0,
                                           CharacterAbility.SACRIFICE ) );
    }

    public final Player getSauron() {
        return sauron;
    }

    public final Player getFellowship() {
        return fellowship;
    }

    public final Board getBoard() {
        return board;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public List<Card> getCards() {
        return cards;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

}
