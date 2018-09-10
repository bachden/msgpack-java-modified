package org.msgpack.unpacker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.CharacterCodingException;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.util.json.JSON;

public class TestMalformedEncoding {
	private byte[][] malforms = new byte[][] { { (byte) 0xc0, (byte) 0xaf }, // '/'
																				// in
																				// 2
																				// bytes
			{ (byte) 0xe0, (byte) 0x80, (byte) 0xaf } // '/' in 3 bytes
	};

	@Test
	public void testRawValueGetString() throws Exception {
		for (byte[] malform : malforms) {
			RawValue r = ValueFactory.createRawValue(malform);
			try {
				r.getString();
				fail("no exception");
			} catch (MessageTypeException expected) {
			}
			byte[] a = r.getByteArray();
			assertArrayEquals(malform, a);
		}
	}

	@Test
	public void testBufferUnpackerUnpackString() throws Exception {
		for (byte[] malform : malforms) {
			MessagePack msgpack = new MessagePack();
			BufferPacker pk = msgpack.createBufferPacker();
			pk.write(malform);
			byte[] b = pk.toByteArray();
			Unpacker u = msgpack.createBufferUnpacker(b);
			try {
				u.readString();
				System.out.println("******** No exception");
				fail("no exception");
			} catch (MessageTypeException expected) {
			}
			// byte[] a = u.readByteArray();
			// assertArrayEquals(malform, a);
		}
	}

	@Test
	public void testUnpackerUnpackString() throws Exception {
		for (byte[] malform : malforms) {
			MessagePack msgpack = new MessagePack();
			BufferPacker pk = msgpack.createBufferPacker();
			pk.write(malform);
			byte[] b = pk.toByteArray();
			Unpacker u = msgpack.createUnpacker(new ByteArrayInputStream(b));
			try {
				u.readString();
				fail("no exception");
			} catch (MessageTypeException expected) {

			}

			byte[] a = u.readByteArray();
			assertArrayEquals(malform, a);
		}
	}

	private String bytesToString(byte[] bytes) {
		if (bytes != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (byte b : bytes) {
				if (sb.length() > 1) {
					sb.append(", ");
				}
				sb.append(b);
			}
			sb.append("]");
			return sb.toString();
		}
		return null;
	}

	@Test
	public void testConverterUnpackString() throws Exception {
		for (byte[] malform : malforms) {
			MessagePack msgpack = new MessagePack();
			RawValue r = ValueFactory.createRawValue(malform);
			Converter u = new Converter(msgpack, r);
			try {
				u.readString();
				fail("no exception");
			} catch (MessageTypeException expected) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				u.close();
			}
			byte[] a = u.readByteArray();
			// System.out.println("a: " + bytesToString(a) + ", malform: " +
			// bytesToString(malform));
			assertArrayEquals(malform, a);
		}
	}

	@Test
	public void testJSONPackerWriteString() throws Exception {
		for (byte[] malform : malforms) {
			JSON json = new JSON();
			Packer pk = json.createPacker(new ByteArrayOutputStream());
			try {
				pk.write(malform);
				fail("no exception");
			} catch (CharacterCodingException expected) {
			}
		}
	}

	@Test
	public void testJSONBufferPackerWriteString() throws Exception {
		for (byte[] malform : malforms) {
			JSON json = new JSON();
			Packer pk = json.createBufferPacker();
			try {
				pk.write(malform);
				fail("no exception");
			} catch (CharacterCodingException expected) {
			}
		}
	}

	@Test
	public void testValueToString() throws Exception {
		for (byte[] malform : malforms) {
			RawValue r = ValueFactory.createRawValue(malform);
			String str = r.toString();
			// malformed bytes will be ignored
			assertEquals("\"\"", str);
		}
	}
}
