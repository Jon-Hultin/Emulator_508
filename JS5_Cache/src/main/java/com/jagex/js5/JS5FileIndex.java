package com.jagex.js5;

import com.jagex.js5.util.ByteBufferUtils;

import java.nio.ByteBuffer;

/**
 * An {@link JS5FileIndex} points to a file inside a {@link JS5FileStore}.
 * @author Graham
 * @author `Discardedx2
 */
public final class JS5FileIndex {

	/**
	 * The size of an index, in bytes.
	 */
	public static final int SIZE = 6;

	/**
	 * Decodes the specified {@link ByteBuffer} into an {@link JS5FileIndex} object.
	 * @param buf The buffer.
	 * @return The index.
	 */
	public static JS5FileIndex decode(ByteBuffer buf) {
		if (buf.remaining() != SIZE)
			throw new IllegalArgumentException();

		int size = ByteBufferUtils.getTriByte(buf);
		int sector = ByteBufferUtils.getTriByte(buf);
		return new JS5FileIndex(size, sector);
	}

	/**
	 * The size of the file in bytes.
	 */
	private int size;

	/**
	 * The number of the first sector that contains the file.
	 */
	private int sector;

	/**
	 * Creates a new index.
	 * @param size The size of the file in bytes.
	 * @param sector The number of the first sector that contains the file.
	 */
	public JS5FileIndex(int size, int sector) {
		this.size = size;
		this.sector = sector;
	}

	/**
	 * Gets the size of the file.
	 * @return The size of the file in bytes.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gets the number of the first sector that contains the file.
	 * @return The number of the first sector that contains the file.
	 */
	public int getSector() {
		return sector;
	}

	/**
	 * Encodes this index into a byte buffer.
	 * @return The buffer.
	 */
	public ByteBuffer encode() {
		ByteBuffer buf = ByteBuffer.allocate(JS5FileIndex.SIZE);
		ByteBufferUtils.putTriByte(buf, size);
		ByteBufferUtils.putTriByte(buf, sector);
		return (ByteBuffer) buf.flip();
	}

}