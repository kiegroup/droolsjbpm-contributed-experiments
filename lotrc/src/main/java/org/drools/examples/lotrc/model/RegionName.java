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
 * @author etirelli
 *
 */
public enum RegionName implements Model {
    
    MORDOR("Mordor"),
    GONDOR("Gondor"),
    DAGORLAD("Dagorlad"),
    ROHAN("Rohan"),
    FANGORN("Fangorn"),
    MIRKWOOD("Mirkwood"),
    GAP_OF_ROHAN("Gap of Rohan"),
    MORIA("Moria"),
    MISTY_MOUNTAINS("Misty Mountains"),
    HIGH_PASS("High Pass"),
    ENEDWAITH("Enedwaith"),
    HOLLIN("Hollin"),
    RHUDAUR("Rhudaur"),
    CARDOLAN("Cardolan"),
    ARTHEDAIN("Arthedain"),
    SHIRE("Shire");
    
    private final String name;
    RegionName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
