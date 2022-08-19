package com.jagex.js5.definition.accumulators;

import com.jagex.js5.JS5Archive;
import com.jagex.js5.JS5Cache;
import com.jagex.js5.JS5Container;
import com.jagex.js5.JS5ReferenceTable;
import com.jagex.js5.def.ItemDefinition;
import com.jagex.js5.definition.JS5DefinitionAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>
 * An implementation of {@link JS5DefinitionAccumulator} which reads {@link ItemDefinition}'s from the {@link JS5Cache}
 * and maps them to {@link JS5DefinitionAccumulator#definitions}.
 * </p>
 *
 * @see JS5DefinitionAccumulator
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/18/2022}
 * @version 1.0.0
 */
public class ItemJS5DefinitionAccumulator extends JS5DefinitionAccumulator<Integer, ItemDefinition> {

    /**
     * Class instanced {@link Logger}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemJS5DefinitionAccumulator.class);

    /**
     * The target archive index for this {@link JS5DefinitionAccumulator}
     */
    private static final int JS5_ARCHIVE_INDEX = 255;

    /**
     * The target file index for this {@link JS5DefinitionAccumulator}
     */
    private static final int JS5_FILE_INDEX = 19;

    @Override
    public void map(JS5Cache cache) throws IOException {
        LOGGER.info("Mapping item definitions from js5 cache.");

        var js5Container = JS5Container.decode(cache.getStore().read(JS5_ARCHIVE_INDEX, JS5_FILE_INDEX));
        var js5ReferenceTable = JS5ReferenceTable.decode(js5Container.getData());

        for (int refIndex = 0; refIndex < js5ReferenceTable.capacity(); refIndex++) {
            var entry = js5ReferenceTable.getEntry(refIndex);

            if (entry == null) continue;

            var archive = JS5Archive.decode(cache.read(JS5_FILE_INDEX, refIndex).getData(), entry.size());
            var non_sparseEntryIndex = 0;

            for (int entryIndex = 0; entryIndex < entry.capacity(); entryIndex++) {
                var childEntry = entry.getEntry(entryIndex);

                if (childEntry == null) continue;

                var itemId = refIndex * 256 + entryIndex;
                var definition = ItemDefinition.decode(archive.getEntry(non_sparseEntryIndex++));

                definitions.put(itemId, definition);
            }
        }
        LOGGER.info("Successfully mapped " + definitions.size() + " item definitions.");
    }

}
