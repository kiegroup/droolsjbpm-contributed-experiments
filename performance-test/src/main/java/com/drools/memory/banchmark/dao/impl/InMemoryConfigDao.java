package com.drools.memory.banchmark.dao.impl;

import com.drools.memory.banchmark.dao.ConfigDao;
import com.drools.memory.banchmark.model.ConfigData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Victor
 */
public class InMemoryConfigDao implements ConfigDao {

	private final ConcurrentHashMap<String, ConfigData> cache = new ConcurrentHashMap<String, ConfigData>();

	public void initialize() throws Exception {
    }

	@Override
	public ConfigData getConfigData(String id) {
		return cache.get(id);
	}

	@Override
	public void updateConfigData(ConfigData configData) {
		cache.put(configData.getId(), configData);
	}
}
