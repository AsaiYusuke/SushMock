package com.github.AsaiYusuke.SushMock.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.github.AsaiYusuke.SushMock.ext.CompareTransformer;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class Sequence {

	private int lineNum;
	private int sequenceNum;

	private File file;
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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public byte[] getByteArray() {
		if (byteArray == null) {
			byteArray = new byte[(int) file.length()];
			try {
				FileInputStream fis = new FileInputStream(file);
				fis.read(byteArray);
				fis.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		return byteArray;
	}

	private void saveByteArray() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(byteArray);
			fos.close();
		} catch (IOException e2) {
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
