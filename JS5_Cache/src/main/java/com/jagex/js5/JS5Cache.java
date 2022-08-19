package com.jagex.js5;

import com.jagex.js5.util.ByteBufferUtils;
import com.jagex.js5.util.crypto.hash.Whirlpool;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * The {@link JS5Cache} class provides a unified, high-level API for modifying
 * the cache of a Jagex game.
 *
 * @param store The file store that backs this cache.
 * @author Graham
 * @author `Discardedx2
 */
public record JS5Cache(JS5FileStore store) implements Closeable {

	/**
	 * Creates a new {@link JS5Cache} backed by the specified {@link JS5FileStore}.
	 *
	 * @param store The {@link JS5FileStore} that backs this {@link JS5Cache}.
	 */
	public JS5Cache {
	}

	/**
	 * Gets the number of index files, not including the meta index file.
	 *
	 * @return The number of index files.
	 * @throws IOException if an I/O error occurs.
	 */
	public int getTypeCount() throws IOException {
		return store.getTypeCount();
	}

	/**
	 * Gets the number of files of the specified type.
	 *
	 * @param type The type.
	 * @return The number of files.
	 * @throws IOException if an I/O error occurs.
	 */
	public int getFileCount(int type) throws IOException {
		return store.getFileCount(type);
	}

	/**
	 * Gets the {@link JS5FileStore} that backs this {@link JS5Cache}.
	 *
	 * @return The underlying file store.
	 */
	@Override
	public JS5FileStore store() {
		return store;
	}

	/**
	 * Computes the {@link JS5ChecksumTable} for this cache. The checksum table
	 * forms part of the so-called "update keys".
	 *
	 * @return The {@link JS5ChecksumTable}.
	 * @throws IOException if an I/O error occurs.
	 */
	public JS5ChecksumTable createChecksumTable() throws IOException {
		/* create the checksum table */
		int size = store.getTypeCount();
		JS5ChecksumTable table = new JS5ChecksumTable(size);

		/* loop through all the reference tables and get their CRC and versions */
		for (int i = 0; i < size; i++) {
			ByteBuffer buf = store.read(255, i);

			int crc = 0;
			int version = 0;
			byte[] whirlpool = new byte[64];

			/*
			 * if there is actually a reference table, calculate the CRC,
			 * version and whirlpool hash
			 */
			if (buf.limit() > 0) { // some indices are not used, is this appropriate?
				JS5ReferenceTable ref = JS5ReferenceTable.decode(JS5Container.decode(buf).getData());
				crc = ByteBufferUtils.getCrcChecksum(buf);
				version = ref.getVersion();
				buf.position(0);
				whirlpool = ByteBufferUtils.getWhirlpoolDigest(buf);
			}

			table.setEntry(i, new JS5ChecksumTable.Entry(crc, version, whirlpool));
		}

		/* return the table */
		return table;
	}

	/**
	 * Reads a file from the cache.
	 *
	 * @param type The type of file.
	 * @param file The file id.
	 * @return The file.
	 * @throws IOException if an I/O error occurred.
	 */
	public JS5Container read(int type, int file) throws IOException {
		/* we don't want people reading/manipulating these manually */
		if (type == 255)
			throw new IOException("Reference tables can only be read with the low level FileStore API!");

		/* delegate the call to the file store then decode the container */
		return JS5Container.decode(store.read(type, file));
	}

	/**
	 * Writes a file to the cache and updates the {@link JS5ReferenceTable} that
	 * it is associated with.
	 *
	 * @param type      The type of file.
	 * @param file      The file id.
	 * @param container The {@link JS5Container} to write.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(int type, int file, JS5Container container) throws IOException {
		/* we don't want people reading/manipulating these manually */
		if (type == 255)
			throw new IOException("Reference tables can only be modified with the low level FileStore API!");

		/* increment the container's version */
		container.setVersion(container.getVersion() + 1);

		/* decode the reference table for this index */
		JS5Container tableContainer = JS5Container.decode(store.read(255, type));
		JS5ReferenceTable table = JS5ReferenceTable.decode(tableContainer.getData());

		/* grab the bytes we need for the checksum */
		ByteBuffer buffer = container.encode();
		byte[] bytes = new byte[buffer.limit() - 2]; // last two bytes are the version and shouldn't be included
		buffer.mark();
		try {
			buffer.position(0);
			buffer.get(bytes, 0, bytes.length);
		} finally {
			buffer.reset();
		}

		/* calculate the new CRC checksum */
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);

		/* update the version and checksum for this file */
		JS5ReferenceTable.Entry entry = table.getEntry(file);
		if (entry == null) {
			/* create a new entry for the file */
			entry = new JS5ReferenceTable.Entry();
			table.putEntry(file, entry);
		}
		entry.setVersion(container.getVersion());
		entry.setCrc((int) crc.getValue());

		/* calculate and update the whirlpool digest if we need to */
		if ((table.getFlags() & JS5ReferenceTable.FLAG_WHIRLPOOL) != 0) {
			byte[] whirlpool = Whirlpool.whirlpool(bytes, 0, bytes.length);
			entry.setWhirlpool(whirlpool);
		}

		/* update the reference table version */
		table.setVersion(table.getVersion() + 1);

		/* save the reference table */
		tableContainer = new JS5Container(tableContainer.getType(), table.encode());
		store.write(255, type, tableContainer.encode());

		/* save the file itself */
		store.write(type, file, buffer);
	}

	/**
	 * Reads a file contained in an archive in the cache.
	 *
	 * @param type   The type of the file.
	 * @param file   The archive id.
	 * @param member The file within the archive.
	 * @return The file.
	 * @throws IOException if an I/O error occurred.
	 */
	public ByteBuffer read(int type, int file, int member) throws IOException {
		/* grab the container and the reference table */
		JS5Container container = read(type, file);
		JS5Container tableContainer = JS5Container.decode(store.read(255, type));
		JS5ReferenceTable table = JS5ReferenceTable.decode(tableContainer.getData());

		/* check if the file/member are valid */
		JS5ReferenceTable.Entry entry = table.getEntry(file);
		if (entry == null || member < 0 || member >= entry.capacity())
			throw new FileNotFoundException();

		/* convert member id */
		int nonSparseMember = 0;
		for (int i = 0; i < member; i++) {
			if (entry.getEntry(i) != null)
				nonSparseMember++;
		}

		/* extract the entry from the archive */
		JS5Archive archive = JS5Archive.decode(container.getData(), entry.size());
		return archive.getEntry(nonSparseMember);
	}

	/**
	 * Writes a file contained in an archive to the cache.
	 *
	 * @param type   The type of file.
	 * @param file   The id of the archive.
	 * @param member The file within the archive.
	 * @param data   The data to write.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(int type, int file, int member, ByteBuffer data) throws IOException {
		/* grab the reference table */
		JS5Container tableContainer = JS5Container.decode(store.read(255, type));
		JS5ReferenceTable table = JS5ReferenceTable.decode(tableContainer.getData());

		/* create a new entry if necessary */
		JS5ReferenceTable.Entry entry = table.getEntry(file);
		int oldArchiveSize = -1;
		if (entry == null) {
			entry = new JS5ReferenceTable.Entry();
			table.putEntry(file, entry);
		} else {
			oldArchiveSize = entry.capacity();
		}

		/* add a child entry if one does not exist */
		JS5ReferenceTable.ChildEntry child = entry.getEntry(member);
		if (child == null) {
			child = new JS5ReferenceTable.ChildEntry();
			entry.putEntry(member, child);
		}

		/* extract the current archive into memory so we can modify it */
		JS5Archive archive;
		int containerType, containerVersion;
		if (file < store.getFileCount(type) && oldArchiveSize != -1) {
			JS5Container container = read(type, file);
			containerType = container.getType();
			containerVersion = container.getVersion();
			archive = JS5Archive.decode(container.getData(), oldArchiveSize);
		} else {
			containerType = JS5Container.COMPRESSION_GZIP;
			containerVersion = 1;
			archive = new JS5Archive(member + 1);
		}

		/* expand the archive if it is not large enough */
		if (member >= archive.size()) {
			JS5Archive newArchive = new JS5Archive(member + 1);
			for (int id = 0; id < archive.size(); id++) {
				newArchive.putEntry(id, archive.getEntry(id));
			}
			archive = newArchive;
		}

		/* put the member into the archive */
		archive.putEntry(member, data);

		/* create 'dummy' entries */
		for (int id = 0; id < archive.size(); id++) {
			if (archive.getEntry(id) == null) {
				entry.putEntry(id, new JS5ReferenceTable.ChildEntry());
				archive.putEntry(id, ByteBuffer.allocate(1));
			}
		}

		/* write the reference table out again */
		tableContainer = new JS5Container(tableContainer.getType(), table.encode());
		store.write(255, type, tableContainer.encode());

		/* and write the archive back to memory */
		JS5Container container = new JS5Container(containerType, archive.encode(), containerVersion);
		write(type, file, container);
	}

	@Override
	public void close() throws IOException {
		store.close();
	}

}
