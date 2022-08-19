package com.jagex.js5;

import com.jagex.js5.util.ByteBufferUtils;

import java.nio.ByteBuffer;

/**
 * A {@link JS5Slice} contains a header and data. The header contains information
 * used to verify the integrity of the cache like the current file id, type and
 * chunk. It also contains a pointer to the next slice such that the slice
 * form a singly-linked list. The data is simply up to 512 bytes of the file.
 *
 * @param type       The type of file this slice contains.
 * @param id         The id of the file this slice contains.
 * @param chunk      The chunk within the file that this slice contains.
 * @param nextSlice  The next slice.
 * @param data       The data in this slice.
 * @author Graham
 * @author `Discardedx2
 */
public record JS5Slice(int type, int id, int chunk, int nextSlice, byte[] data) {

	/**
	 * The size of the header within a slice in bytes.
	 */
	public static final int HEADER_SIZE = 8;

	/**
	 * The size of the data within a slice in bytes.
	 */
	public static final int DATA_SIZE = 512;

	/**
	 * The total size of a slice in bytes.
	 */
	public static final int SIZE = HEADER_SIZE + DATA_SIZE;

	/**
	 * Decodes the specified {@link ByteBuffer} into a {@link JS5Slice} object.
	 *
	 * @param buf The buffer.
	 * @return The slice.
	 */
	public static JS5Slice decode(ByteBuffer buf) {
		if (buf.remaining() != SIZE)
			throw new IllegalArgumentException();

		int id = buf.getShort() & 0xFFFF;
		int chunk = buf.getShort() & 0xFFFF;
		int nextslice = ByteBufferUtils.getTriByte(buf);
		int type = buf.get() & 0xFF;
		byte[] data = new byte[DATA_SIZE];
		buf.get(data);

		return new JS5Slice(type, id, chunk, nextslice, data);
	}

	/**
	 * Creates a new slice.
	 *
	 * @param type       The type of the file.
	 * @param id         The file's id.
	 * @param chunk      The chunk of the file this slice contains.
	 * @param nextSlice The slice containing the next chunk.
	 * @param data       The data in this slice.
	 */
	public JS5Slice {
	}

	/**
	 * Gets the type of file in this slice.
	 *
	 * @return The type of file in this slice.
	 */
	@Override
	public int type() {
		return type;
	}

	/**
	 * Gets the id of the file within this slice.
	 *
	 * @return The id of the file in this slice.
	 */
	@Override
	public int id() {
		return id;
	}

	/**
	 * Gets the chunk of the file this slice contains.
	 *
	 * @return The chunk of the file this slice contains.
	 */
	@Override
	public int chunk() {
		return chunk;
	}

	/**
	 * Gets the next slice.
	 *
	 * @return The next slice.
	 */
	public int nextSlice() {
		return nextSlice;
	}

	/**
	 * Gets this slice's data.
	 *
	 * @return The data within this slice.
	 */
	@Override
	public byte[] data() {
		return data;
	}

	/**
	 * Encodes this slice into a {@link ByteBuffer}.
	 *
	 * @return The encoded buffer.
	 */
	public ByteBuffer encode() {
		ByteBuffer buf = ByteBuffer.allocate(SIZE);
		buf.putShort((short) id);
		buf.putShort((short) chunk);
		ByteBufferUtils.putTriByte(buf, nextSlice);
		buf.put((byte) type);
		buf.put(data);

		return (ByteBuffer) buf.flip();
	}

}
