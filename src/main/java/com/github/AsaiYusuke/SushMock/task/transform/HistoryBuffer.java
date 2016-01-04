package com.github.AsaiYusuke.SushMock.task.transform;

import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

//TODO このクラスの機能をSequenceクラスへマージする
public class HistoryBuffer {
	private ByteBuffer streamBuffer;
	private StreamType type;

	public HistoryBuffer(StreamType type) {
		this.type = type;

		streamBuffer = ByteBuffer
				.allocate(Constants.Option.getHistoryBufferSize());
		streamBuffer.mark();
	}

	public StreamType getType() {
		return type;
	}

	public void setType(StreamType streamType) {
		this.type = streamType;
	}

	public void addStream(byte[] buf) {
		streamBuffer.put(buf);
	}

	public byte[] getStream() {
		return getStream(streamBuffer.position());
	}

	public byte[] getStream(int length) {
		int pos = streamBuffer.position();
		byte[] buffer = new byte[length];
		try {
			streamBuffer.reset();
		} catch (InvalidMarkException e) {
			streamBuffer.rewind();
		}
		streamBuffer.get(buffer);

		streamBuffer.mark();
		streamBuffer.position(pos);

		return buffer;
	}

	public boolean checkBufferSize(byte[] buf) {
		int bufLen = buf.length;
		if (streamBuffer.position() + bufLen > streamBuffer.limit()) {
			return false;
		}
		return true;
	}

	public int getPosition() {
		return streamBuffer.position();
	}

	public void rewind() {
		streamBuffer.rewind();
		streamBuffer.mark();
	}
}