package com.github.AsaiYusuke.SushMock.record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

import com.github.AsaiYusuke.SushMock.ext.CompareTransformer;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class Sequence {

	private int lineNum;
	private int sequenceNum;

	// private File file;
	private Path path;
	private byte[] byteArray;

	private StreamType type;
	private Line nextLine;

	private Sequence nextSequence;

	private static Map<String, CompareTransformer> exts;

	public Sequence() {
		// CompareTransformer Extensions
		if (exts == null) {
			exts = Constants.Option
					.getExtensionsOfType(CompareTransformer.class);
		}
	}

	public int getLineNum() {
		return lineNum;
	}

	public int getSequenceNum() {
		return sequenceNum;
	}

	public void setId(int lineNum, int sequenceNum) {
		this.lineNum = lineNum;
		this.sequenceNum = sequenceNum;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public byte[] getByteArray() {
		if (byteArray == null) {
			try {
				byteArray = Files.readAllBytes(path);
			} catch (IOException e1) {
			}
		}
		return byteArray;
	}

	private void saveByteArray() {
		try {
			Files.write(path, byteArray, StandardOpenOption.WRITE);
		} catch (IOException e) {
		}
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
		saveByteArray();
	}

	public Sequence getNextSequence() {
		return this.nextSequence;
	}

	public void setNextSequence(Sequence nextSequence) {
		this.nextSequence = nextSequence;
	}

	public StreamType getType() {
		return type;
	}

	public void setType(StreamType type) {
		this.type = type;
	}

	public boolean checkFile(byte[] cmpBuf) {

		byte[] fileBuf = getByteArray();

		for (CompareTransformer trans : exts.values()) {
			cmpBuf = trans.transform(cmpBuf, type);
			fileBuf = trans.transform(fileBuf, type);
		}

		return Arrays.equals(fileBuf, cmpBuf);
	}

	public Line getNextLine() {
		return nextLine;
	}

	public void setNextLine(Line nextLine2) {
		this.nextLine = nextLine2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Sequence sequence = (Sequence) obj;
		return (lineNum == sequence.getLineNum()
				&& sequenceNum == sequence.getSequenceNum());
	}

	@Override
	public int hashCode() {
		int result = lineNum * 100000 + sequenceNum;
		return result;
	}
}
