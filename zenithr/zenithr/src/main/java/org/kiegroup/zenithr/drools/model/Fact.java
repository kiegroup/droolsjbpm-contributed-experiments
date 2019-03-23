/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiegroup.zenithr.drools.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.kiegroup.zenithr.drools.json.FactSerializer;

@JsonSerialize(using = FactSerializer.class)
public class Fact {

    private final String id;
    private Double numberValue;
    private Boolean booleanValue;
    private String stringValue;
    private Integer integerValue;

    public Fact(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Integer getInteger() {
        return integerValue;
    }

    public Fact setInteger(Integer integerValue) {
        this.integerValue = integerValue;
        return this;
    }
    
    public Fact setInteger(String integerValue) {
        if(useDefaultValue(integerValue)) {
            this.integerValue = 0;
        } else {
            this.integerValue = Integer.parseInt(integerValue);
        }
        return this;
    }
    
    public Double getNumber() {
        return numberValue;
    }

    public Fact setNumber(Double numberValue) {
        this.numberValue = numberValue;
        return this;
    }
    
    public Fact setNumber(String numberValue) {
        if(useDefaultValue(numberValue)) {
            this.numberValue = 0d;
        } else {
            this.numberValue = Double.parseDouble(numberValue);
        }
        return this;
    }
    
    public Boolean getBoolean() {
        return booleanValue;
    }

    public Fact setBoolean(Boolean booleanValue) {
        this.booleanValue = booleanValue;
        return this;
    }
    
    public Fact setBoolean(String booleanValue) {
        if(useDefaultValue(booleanValue)) {
            this.booleanValue = Boolean.FALSE;
        } else {
            this.booleanValue = Boolean.parseBoolean(booleanValue);
        }
        return this;
    }

    public String getString() {
        return stringValue;
    }

    public Fact setString(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }
    
    @Override
    public String toString() {
        if(stringValue != null) {
            return stringValue;
        }
        if(numberValue != null) {
            return numberValue.toString();
        }
        if(booleanValue != null) {
            return booleanValue.toString();
        }
        if(integerValue != null) {
            return integerValue.toString();
        }
        return "";
    }
    
    private boolean useDefaultValue(String value) {
        return value == null || value.trim().isEmpty();
    }
}
