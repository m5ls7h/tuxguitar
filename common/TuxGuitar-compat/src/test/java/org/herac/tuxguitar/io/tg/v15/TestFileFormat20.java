package org.herac.tuxguitar.io.tg.v15;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.herac.tuxguitar.io.base.TGFileFormatUtils;
import org.herac.tuxguitar.io.base.TGSongReaderHandle;
import org.herac.tuxguitar.io.base.TGSongWriterHandle;
import org.herac.tuxguitar.song.factory.TGFactory;
import org.junit.jupiter.api.Test;

/* Objective of this class is to test file format 2.0: compressed xml
 * especially the absence of regression with binary file format (1.5)
 *
 * Formally, this class tests org.herac.tuxguitar.io.tg package in tuxguitar-lib
 * but to proceed it requires access to both packages org.herac.tuxguitar.io.tg and org.herac.tuxguitar.io.tg.v15
 * so it cannot be implemented in tuxguitar-lib (this would create a circular dependency with tuxguitar-compat)
 */

public class TestFileFormat20 {

	@Test
	/* test full backward compatibility of file format 2.0 with TuxGuitar file format 1.5
	 * important note: to succeed, file provided to this test MUST have been written by TuxGuitar version >= 1.6.3
	 * version 1.6.3 introduced a modification (79512b9): keySignature is NOT stored for percussion tracks (meaningless)
	 * binary comparison between .tg files written by versions before and after 1.6.3 already fails
	 */
	public void testFileFormatEquivalence() throws FileNotFoundException, Throwable {
		assertTrue(xmlFileIsEquivalent("Untitled_15.tg", false));
		assertTrue(xmlFileIsEquivalent("Untitled_15.tg", true));
		// "reference" file is a .tg file containing many (all?) different elements of a TuxGuitar file
		// i.e. song attributes, channels, measureHeaders, tracks, measures, voices, notes, effects, ...
		assertTrue(xmlFileIsEquivalent("reference_15.tg",false));
		assertTrue(xmlFileIsEquivalent("reference_15.tg",true));
	}

	/* check file format equivalence:
	 * - import .tg v1.5 file into song
	 * - save song under .tg format 2.0
	 * - import .tg format 2.0 into song
	 * - save song as .tg format 1.5
	 * - compare with original .tg file, shall be identical
	 */
	private boolean xmlFileIsEquivalent(String resourceFileName, boolean compress) throws FileNotFoundException, Throwable {
		TGFactory factory = new TGFactory();
		// load original tg file
		File original = new File(getClass().getClassLoader().getResource(resourceFileName).getFile());
		byte[] bufferOriginal = TGFileFormatUtils.getBytes(new FileInputStream(original));
		TGSongReaderHandle handleRead = new TGSongReaderHandle();
		handleRead.setFactory(factory);
		handleRead.setInputStream(new ByteArrayInputStream(bufferOriginal));
		new org.herac.tuxguitar.io.tg.v15.TGSongReaderImpl().read(handleRead);

		// save song under xml format in byte buffer
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		TGSongWriterHandle handleWrite = new TGSongWriterHandle();
		handleWrite.setFactory(factory);
		handleWrite.setSong(handleRead.getSong());
		handleWrite.setOutputStream(outputStream);
		org.herac.tuxguitar.io.tg.TGSongWriterImpl writer = new org.herac.tuxguitar.io.tg.TGSongWriterImpl();
		if (compress) {
			writer.write(handleWrite);
		} else {
			writer.writeContent(handleWrite);
		}

		// load xml format
		handleRead.setSong(null);
		byte[] bufferXml = outputStream.toByteArray();
		handleRead.setInputStream(new ByteArrayInputStream(bufferXml));
		org.herac.tuxguitar.io.tg.TGSongReaderImpl reader = new org.herac.tuxguitar.io.tg.TGSongReaderImpl();
		if (compress) {
			reader.read(handleRead);
		} else {
			reader.readContent(handleRead, handleRead.getInputStream());
		}
		// save under tg format in byte buffer
		outputStream = new ByteArrayOutputStream();
		handleWrite.setSong(handleRead.getSong());
		handleWrite.setOutputStream(outputStream);
		handleWrite.setFormat(org.herac.tuxguitar.io.tg.TGStream.TG_FORMAT);
		new org.herac.tuxguitar.io.tg.v15.TGSongWriterImpl().write(handleWrite);
		byte[] bufferFinal = outputStream.toByteArray();

		// compare
		if (bufferFinal.length != bufferOriginal.length) {
			return false;
		}
		for (int i=0; i<bufferOriginal.length; i++) {
			if (bufferFinal[i] != bufferFinal[i]) {
				return false;
			}
		}
		return true;
	}
}
