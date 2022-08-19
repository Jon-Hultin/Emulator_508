package com.jagex.js5;

import com.jagex.js5.util.FileChannelUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * A file store holds multiple files inside a "virtual" file system made up of
 * several index files and a single data file.
 *
 * @author Graham
 * @author `Discardedx2
 */
public final class JS5FileStore implements Closeable {

	public static JS5FileStore create(String root, int indexes) throws IOException {
		return create(new File(root), indexes);
	}

	public static JS5FileStore create(File root, int indexes) throws IOException {
		if (!root.mkdirs())
			throw new IOException();

		for (int i = 0; i < indexes; i++) {
			File index = new File(root, "main_file_cache.idx" + i);
			if (!index.createNewFile())
				throw new IOException();
		}

		File meta = new File(root, "main_file_cache.idx255");
		if (!meta.createNewFile())
			throw new IOException();

		File data = new File(root, "main_file_cache.dat2");
		if (!data.createNewFile())
			throw new IOException();

		return open(root);
	}

	/**
	 * Opens the file store stored in the specified directory.
	 * @param root The directory containing the index and data files.
	 * @return The file store.
	 * @throws IOException if any of the {@code main_file_cache.*} files could
	 * not be opened.
	 */
	public static JS5FileStore open(String root) throws IOException {
		return open(new File(root));
	}

	/**
	 * Opens the file store stored in the specified directory.
	 * @param root The directory containing the index and data files.
	 * @return The file store.
	 * @throws IOException if any of the {@code main_file_cache.*} files could
	 * not be opened.
	 */
	public static JS5FileStore open(File root) throws IOException {
		File data = new File(root, "main_file_cache.dat2");
		if (!data.exists())
			throw new FileNotFoundException();

		FileChannel dataChannel = FileChannel.open(data.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);

		List<FileChannel> indexChannels = new ArrayList<>();
		for (int i = 0; i < 254; i++) {
			File index = new File(root, "main_file_cache.idx" + i);
			if (!index.exists())
				break;

			FileChannel indexChannel = FileChannel.open(index.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
			indexChannels.add(indexChannel);
		}

		if (indexChannels.isEmpty())
			throw new FileNotFoundException();

		File meta = new File(root, "main_file_cache.idx255");
		if (!meta.exists())
			throw new FileNotFoundException();

		FileChannel metaChannel = FileChannel.open(meta.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);

		return new JS5FileStore(dataChannel, indexChannels.toArray(new FileChannel[0]), metaChannel);
	}

	/**
	 * The data file.
	 */
	private final FileChannel dataChannel;

	/**
	 * The index files.
	 */
	private final FileChannel[] indexChannels;

	/**
	 * The 'meta' index files.
	 */
	private final FileChannel metaChannel;

	/**
	 * Creates a new file store.
	 * @param data The data file.
	 * @param indexes The index files.
	 * @param meta The 'meta' index file.
	 */
	public JS5FileStore(FileChannel data, FileChannel[] indexes, FileChannel meta) {
		this.dataChannel = data;
		this.indexChannels = indexes;
		this.metaChannel = meta;
	}

	/**
	 * Gets the number of index files, not including the meta index file.
	 * @return The number of index files.
	 * @throws IOException if an I/O error occurs.
	 */
	public int getTypeCount() throws IOException {
		return indexChannels.length;
	}

	/**
	 * Gets the number of files of the specified type.
	 * @param type The type.
	 * @return The number of files.
	 * @throws IOException if an I/O error occurs.
	 */
	public int getFileCount(int type) throws IOException {
		if ((type < 0 || type >= indexChannels.length) && type != 255)
			throw new FileNotFoundException();

		if (type == 255)
			return (int) (metaChannel.size() / JS5FileIndex.SIZE);
		return (int) (indexChannels[type].size() / JS5FileIndex.SIZE);
	}

	/**
	 * Writes a file.
	 * @param type The type of the file.
	 * @param id The id of the file.
	 * @param data A {@link ByteBuffer} containing the contents of the file.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(int type, int id, ByteBuffer data) throws IOException {
		data.mark();
		if (!write(type, id, data, true)) {
			data.reset();
			write(type, id, data, false);
		}
	}

	/**
	 * Writes a file.
	 * @param type The type of the file.
	 * @param id The id of the file.
	 * @param data A {@link ByteBuffer} containing the contents of the file.
	 * @param overwrite A flag indicating if the existing file should be
	 * overwritten.
	 * @return A flag indicating if the file was written successfully.
	 * @throws IOException if an I/O error occurs.
	 */
	@SuppressWarnings("resource")
	private boolean write(int type, int id, ByteBuffer data, boolean overwrite) throws IOException {
		if ((type < 0 || type >= indexChannels.length) && type != 255)
			throw new FileNotFoundException();

		FileChannel indexChannel = type == 255 ? metaChannel : indexChannels[type];

		int nextSector;
		long ptr = id * JS5FileIndex.SIZE;
		if (overwrite) {
			if (ptr < 0)
				throw new IOException();
			else if (ptr >= indexChannel.size())
				return false;

			ByteBuffer buf = ByteBuffer.allocate(JS5FileIndex.SIZE);
			FileChannelUtils.readFully(indexChannel, buf, ptr);

			JS5FileIndex index = JS5FileIndex.decode((ByteBuffer) buf.flip());
			nextSector = index.getSector();
			if (nextSector <= 0 || nextSector > dataChannel.size() * JS5Slice.SIZE)
				return false;
		} else {
			nextSector = (int) ((dataChannel.size() + JS5Slice.SIZE - 1) / JS5Slice.SIZE);
			if (nextSector == 0)
				nextSector = 1;
		}

		JS5FileIndex index = new JS5FileIndex(data.remaining(), nextSector);
		indexChannel.write(index.encode(), ptr);

		ByteBuffer buf = ByteBuffer.allocate(JS5Slice.SIZE);

		int chunk = 0, remaining = index.getSize();
		do {
			int curSector = nextSector;
			ptr = curSector * JS5Slice.SIZE;
			nextSector = 0;

			if (overwrite) {
				buf.clear();
				FileChannelUtils.readFully(dataChannel, buf, ptr);

				JS5Slice sector = JS5Slice.decode((ByteBuffer) buf.flip());

				if (sector.type() != type)
					return false;

				if (sector.id() != id)
					return false;

				if (sector.chunk() != chunk)
					return false;

				nextSector = sector.nextSlice();
				if (nextSector < 0 || nextSector > dataChannel.size() / JS5Slice.SIZE)
					return false;
			}

			if (nextSector == 0) {
				overwrite = false;
				nextSector = (int) ((dataChannel.size() + JS5Slice.SIZE - 1) / JS5Slice.SIZE);
				if (nextSector == 0)
					nextSector++;
				if (nextSector == curSector)
					nextSector++;
			}

			byte[] bytes = new byte[JS5Slice.DATA_SIZE];
			if (remaining < JS5Slice.DATA_SIZE) {
				data.get(bytes, 0, remaining);
				nextSector = 0; // mark as EOF
				remaining = 0;
			} else {
				remaining -= JS5Slice.DATA_SIZE;
				data.get(bytes, 0, JS5Slice.DATA_SIZE);
			}

			JS5Slice sector = new JS5Slice(type, id, chunk++, nextSector, bytes);
			dataChannel.write(sector.encode(), ptr);
		} while (remaining > 0);

		return true;
	}

	/**
	 * Reads a file.
	 * @param type The type of the file.
	 * @param id The id of the file.
	 * @return A {@link ByteBuffer} containing the contents of the file.
	 * @throws IOException if an I/O error occurs.
	 */
	@SuppressWarnings("resource")
	public ByteBuffer read(int type, int id) throws IOException {
		if ((type < 0 || type >= indexChannels.length) && type != 255)
			throw new FileNotFoundException();

		FileChannel indexChannel = type == 255 ? metaChannel : indexChannels[type];

		long ptr = id * JS5FileIndex.SIZE;
		if (ptr < 0 || ptr >= indexChannel.size())
			throw new FileNotFoundException();

		ByteBuffer buf = ByteBuffer.allocate(JS5FileIndex.SIZE);
		FileChannelUtils.readFully(indexChannel, buf, ptr);

		JS5FileIndex index = JS5FileIndex.decode((ByteBuffer) buf.flip());

		ByteBuffer data = ByteBuffer.allocate(index.getSize());
		buf = ByteBuffer.allocate(JS5Slice.SIZE);

		int chunk = 0, remaining = index.getSize();
		ptr = index.getSector() * JS5Slice.SIZE;
		do {
			buf.clear();
			FileChannelUtils.readFully(dataChannel, buf, ptr);
			JS5Slice sector = JS5Slice.decode((ByteBuffer) buf.flip());

			if (remaining > JS5Slice.DATA_SIZE) {
				data.put(sector.data(), 0, JS5Slice.DATA_SIZE);
				remaining -= JS5Slice.DATA_SIZE;

				if (sector.type() != type)
					throw new IOException("File type mismatch.");

				if (sector.id() != id)
					throw new IOException("File id mismatch.");

				if (sector.chunk() != chunk++)
					throw new IOException("Chunk mismatch.");

				ptr = sector.nextSlice() * JS5Slice.SIZE;
			} else {
				data.put(sector.data(), 0, remaining);
				remaining = 0;
			}
		} while (remaining > 0);

		return (ByteBuffer) data.flip();
	}

	@Override
	public void close() throws IOException {
		dataChannel.close();

		for (FileChannel channel : indexChannels)
			channel.close();

		metaChannel.close();
	}

}
