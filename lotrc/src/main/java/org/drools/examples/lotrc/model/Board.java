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

import static org.drools.examples.lotrc.model.RegionName.*;

import java.util.Arrays;

/**
 * The actual board of the game
 * 
 * @author etirelli
 */
public class Board implements Model {

    private static final long serialVersionUID = -7959119262916027202L;
    
    // regions are represented by row,col coordinates
    // Shire is 0,0 and Mordor is 6,0
    private Region[][] regions;

    public Board() {
        this.regions = new Region[7][];
        // row 0
        this.regions[0] = new Region[1];
        this.regions[0][0] = new RegionImpl( SHIRE,
                                             0,
                                             0,
                                             4 );
        // row 1
        this.regions[1] = new Region[2];
        this.regions[1][0] = new RegionImpl( ARTHEDAIN,
                                             1,
                                             0,
                                             2 );
        this.regions[1][1] = new RegionImpl( CARDOLAN,
                                             1,
                                             1,
                                             2 );
        // row 2
        this.regions[2] = new Region[3];
        this.regions[2][0] = new RegionImpl( RHUDAUR,
                                             2,
                                             0,
                                             2 );
        this.regions[2][1] = new RegionImpl( HOLLIN,
                                             2,
                                             1,
                                             2 );
        this.regions[2][2] = new RegionImpl( ENEDWAITH,
                                             2,
                                             2,
                                             2 );
        // row 3
        this.regions[3] = new Region[4];
        this.regions[3][0] = new RegionImpl( HIGH_PASS,
                                             3,
                                             0,
                                             1 );
        this.regions[3][1] = new RegionImpl( MISTY_MOUNTAINS,
                                             3,
                                             1,
                                             1 );
        this.regions[3][2] = new RegionImpl( MORIA,
                                             3,
                                             2,
                                             1 );
        this.regions[3][3] = new RegionImpl( GAP_OF_ROHAN,
                                             3,
                                             3,
                                             1 );
        // row 4
        this.regions[4] = new Region[3];
        this.regions[4][0] = new RegionImpl( MIRKWOOD,
                                             4,
                                             0,
                                             2 );
        this.regions[4][1] = new RegionImpl( FANGORN,
                                             4,
                                             1,
                                             2 );
        this.regions[4][2] = new RegionImpl( ROHAN,
                                             4,
                                             2,
                                             2 );
        // row 5
        this.regions[5] = new Region[2];
        this.regions[5][0] = new RegionImpl( DAGORLAD,
                                             5,
                                             0,
                                             2 );
        this.regions[5][1] = new RegionImpl( GONDOR,
                                             5,
                                             1,
                                             2 );
        // row 6
        this.regions[6] = new Region[1];
        this.regions[6][0] = new RegionImpl( MORDOR,
                                             6,
                                             0,
                                             4 );
    }

    public Region getRegion(int row,
                            int col) {
        return regions[row][col];
    }

    public Region[][] getRegions() {
        return regions;
    }

    public void printBoard() {
        for ( Region[] row : regions ) {
            System.out.println( Arrays.toString( row ) );
        }
    }

}
