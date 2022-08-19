package com.jagex.js5.util;

import com.jagex.js5.util.zip.CBZip2InputStream;
import com.jagex.js5.util.zip.CBZip2OutputStream;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A class that contains methods to compress and uncompress BZIP2 and GZIP
 * byte arrays.
 * @author Graham
 * @author `Discardedx2
 */
public final class CompressionUtils {

	/**
	 * Compresses a GZIP file.
	 * @param bytes The uncompressed bytes.
	 * @return The compressed bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	public static byte[] gzip(byte[] bytes) throws IOException {
		/* create the streams */
		try (InputStream is = new ByteArrayInputStream(bytes)) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try (OutputStream os = new GZIPOutputStream(bout)) {
				/* copy data between the streams */
				byte[] buf = new byte[4096];
				int len;
				while ((len = is.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, len);
				}
			}

			/* return the compressed bytes */
			return bout.toByteArray();
		}
	}

	/**
	 * Uncompresses a GZIP file.
	 * @param bytes The compressed bytes.
	 * @return The uncompressed bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	public static byte[] gunzip(byte[] bytes) throws IOException {
		/* create the streams */
		try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				/* copy data between the streams */
				byte[] buf = new byte[4096];
				int len;
				while ((len = is.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, len);
				}

                /* return the uncompressed bytes */
                return os.toByteArray();
			}
		}
	}

	/**
	 * Compresses a BZIP2 file.
	 * @param bytes The uncompressed bytes.
	 * @return The compressed bytes without the header.
	 * @throws IOException if an I/O erorr occurs.
	 */
	public static byte[] bzip2(byte[] bytes) throws IOException {
		try (InputStream is = new ByteArrayInputStream(bytes)) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try (OutputStream os = new CBZip2OutputStream(bout, 1)) {
				byte[] buf = new byte[4096];
				int len;
				while ((len = is.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, len);
				}
			}

			/* strip the header from the byte array and return it */
			bytes = bout.toByteArray();
			byte[] bzip2 = new byte[bytes.length - 2];
			System.arraycopy(bytes, 2, bzip2, 0, bzip2.length);
			return bzip2;
		}
	}

	/**
	 * Uncompresses a BZIP2 file.
	 * @param bytes The compressed bytes without the header.
	 * @return The uncompressed bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	public static byte[] bunzip2(byte[] bytes) throws IOException {
		/* prepare a new byte array with the bzip2 header at the start */
		byte[] bzip2 = new byte[bytes.length + 2];
		bzip2[0] = 'h';
		bzip2[1] = '1';
		System.arraycopy(bytes, 0, bzip2, 2, bytes.length);

		try (InputStream is = new CBZip2InputStream(new ByteArrayInputStream(bzip2))) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				byte[] buf = new byte[4096];
				int len;
				while ((len = is.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, len);
				}

                return os.toByteArray();
			}
		}
	}

	/**
	 * Default private constructor to prevent instantiation.
	 */
	private CompressionUtils() {

	}

}
