package com.jagex.js5.tools;

import com.jagex.js5.JS5Container;
import com.jagex.js5.JS5FileStore;
import com.jagex.js5.JS5ReferenceTable;
import com.jagex.js5.JS5ReferenceTable.Entry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public final class CacheAggregator {

	public static void main(String[] args) throws IOException {
		JS5FileStore otherStore = JS5FileStore.open("/home/graham/Downloads/rscd/data/");
		JS5FileStore store = JS5FileStore.open("../game/data/cache/");

		for (int type = 0; type < store.getFileCount(255); type++) {
			if (type == 7) continue; // TODO need support for newer ref table format for this index

			JS5ReferenceTable otherTable = JS5ReferenceTable.decode(JS5Container.decode(otherStore.read(255, type)).getData());
			JS5ReferenceTable table = JS5ReferenceTable.decode(JS5Container.decode(store.read(255, type)).getData());
			for (int file = 0; file < table.capacity(); file++) {
				Entry entry = table.getEntry(file);
				if (entry == null)
					continue;

				if (isRepackingRequired(store, entry, type, file)) {
					Entry otherEntry = otherTable.getEntry(file);
					if (entry.getVersion() == otherEntry.getVersion() && entry.getCrc() == otherEntry.getCrc()) {
						store.write(type, file, otherStore.read(type, file));
					}
				}
			}
		}
	}

	private static boolean isRepackingRequired(JS5FileStore store, Entry entry, int type, int file) {
		ByteBuffer buffer;
		try {
			buffer = store.read(type, file);
		} catch (IOException ex) {
			return true;
		}

		if (buffer.capacity() <= 2) {
			return true;
		}

		byte[] bytes = new byte[buffer.limit() - 2]; // last two bytes are the version and shouldn't be included
		buffer.position(0);
		buffer.get(bytes, 0, bytes.length);

		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);

		if ((int) crc.getValue() != entry.getCrc()) {
			return true;
		}

		buffer.position(buffer.limit() - 2);
		if ((buffer.getShort() & 0xFFFF) != entry.getVersion()) {
			return true;
		}

		return false;
	}

}
