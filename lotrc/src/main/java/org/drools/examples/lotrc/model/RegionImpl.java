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
import java.util.Collections;
import java.util.List;

/**
 * The region implementation
 * 
 * @author etirelli
 */
public class RegionImpl
    implements
    Region {

    private static final long serialVersionUID = 3228332055793827395L;
    
    private final RegionName      name;
    private final int             row;
    private final int             col;
    private final int             capacity;
    private final List<Character> characters;

    public RegionImpl(RegionName name,
                      int row,
                      int col,
                      int capacity) {
        super();
        this.name = name;
        this.row = row;
        this.col = col;
        this.capacity = capacity;
        this.characters = new ArrayList<Character>();
    }

    public RegionName getName() {
        return name;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Character> getCharacters() {
        return Collections.unmodifiableList( characters );
    }

    public void addCharacter(Character character) {
        this.characters.add( character );
    }

    public boolean removeCharacter(Character character) {
        return this.characters.remove( character );
    }

    @Override
    public String toString() {
        return "Region( " + this.name + " characters=" + this.characters + " )";
    }

    public boolean isAdjacentTo(Region region) {
        boolean isAdjacent = ( this.row == region.getRow() && Math.abs( this.col - region.getCol() ) == 1 );
        if( this.row != region.getRow() ) {
            Region fwd = this.row > region.getRow() ? this : region;
            Region bwd = this.row < region.getRow() ? this : region;
            if( fwd.getRow() == bwd.getRow()+1 ) {
                if ( fwd.getRow() > 3 ) {
                    isAdjacent = ( fwd.getCol() == bwd.getCol() || fwd.getCol() == (bwd.getCol() + 1) );
                } else {
                    isAdjacent = ( fwd.getCol() == bwd.getCol() || fwd.getCol() == (bwd.getCol() - 1) );
                }
            }
        }
        return isAdjacent;
    }

    public boolean isMountain() {
        // all regions in row 3 are mountain regions
        return row == 3;
    }
}
