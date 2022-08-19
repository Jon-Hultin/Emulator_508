package com.jagex.js5.tools;

import com.jagex.js5.JS5Container;
import com.jagex.js5.JS5FileStore;
import com.jagex.js5.JS5ReferenceTable;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class CacheDefragmentor {

	public static void main(String[] args) throws IOException {
		try (JS5FileStore in = JS5FileStore.open("../game/data/cache/")) {
			try (JS5FileStore out = JS5FileStore.create("/tmp/defragmented-cache", in.getTypeCount())) {
				for (int type = 0; type < in.getTypeCount(); type++) {
					ByteBuffer buf = in.read(255, type);
					buf.mark();
					out.write(255, type, buf);
					buf.reset();

					JS5ReferenceTable rt = JS5ReferenceTable.decode(JS5Container.decode(buf).getData());
					for (int file = 0; file < rt.capacity(); file++) {
						if (rt.getEntry(file) == null)
							continue;

						out.write(type, file, in.read(type, file));
					}
				}
			}
		}
	}


}
