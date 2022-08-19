package com.jagex.js5;

import com.jagex.js5.definition.accumulators.ItemJS5DefinitionAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/18/2022}
 * @version 1.0.0
 */
public class JS5CacheHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS5CacheHandler.class);

    private final JS5Cache cache;

    private final JS5ChecksumTable checksumTable;

    private final ItemJS5DefinitionAccumulator itemDefinitionAccumulator;

    public JS5CacheHandler() throws IOException {
        LOGGER.info("Loading JS5 cache.");

        this.cache = new JS5Cache(JS5FileStore.open(JS5Constants.JS5_CACHE_PATH));

        LOGGER.info("Generating JS5 cache checksum table.");
        this.checksumTable = cache.createChecksumTable();
        this.itemDefinitionAccumulator = new ItemJS5DefinitionAccumulator();
    }

    public void accumulate() throws IOException {
        LOGGER.info("Accumulating JS5 cache definitions.");
        itemDefinitionAccumulator.map(cache);
    }

    public JS5Cache getCache() {
        return cache;
    }

    public JS5ChecksumTable getChecksumTable() {
        return checksumTable;
    }
}
