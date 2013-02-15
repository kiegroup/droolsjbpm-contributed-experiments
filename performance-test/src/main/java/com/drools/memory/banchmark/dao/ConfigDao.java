package com.drools.memory.banchmark.dao;

import com.drools.memory.banchmark.model.ConfigData;

/**
 * @author Victor
 */
public interface ConfigDao {
    ConfigData getConfigData(String id);
    void updateConfigData(ConfigData ConfigData);
}
