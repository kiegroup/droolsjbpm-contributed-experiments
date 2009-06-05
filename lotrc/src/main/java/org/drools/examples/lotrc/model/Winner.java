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

import org.drools.examples.lotrc.player.Player;

/**
 * A winner representation
 * 
 * @author etirelli
 */
public class Winner implements Model {
    
    private static final long serialVersionUID = 4286339102504245979L;

    private final Player winner;

    public Winner(Player winner) {
        this.winner = winner;
    }

    public Player getWinner() {
        return winner;
    }

}
