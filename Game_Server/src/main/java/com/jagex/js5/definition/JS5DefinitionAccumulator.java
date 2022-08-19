package com.jagex.js5.definition;

import com.google.common.collect.ImmutableMap;
import com.jagex.js5.JS5Cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/18/2022}
 * @version 1.0.0
 */
public abstract class JS5DefinitionAccumulator<T, V> {

    protected Map<T, V> definitions;

    public JS5DefinitionAccumulator() {
        this.definitions = new HashMap<>();
    }

    public abstract void map(JS5Cache cache) throws IOException;

    public final ImmutableMap<T, V> definitions() {
        return ImmutableMap.copyOf(definitions);
    }

}
