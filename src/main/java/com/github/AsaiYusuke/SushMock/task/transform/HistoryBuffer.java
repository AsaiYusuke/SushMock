/*
 * Copyright (C) 2016 Asai Yusuke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	public byte[] cutStream() {
		int length = getLength();
		return cutStream(length);
	}

	public byte[] cutStream(int length) {
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

	public byte[] getStream() {
		int length = getLength();
		return getStream(length);
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

	public int getLength() {
		int curPos = streamBuffer.position();
		streamBuffer.reset();
		int prevPos = streamBuffer.position();
		streamBuffer.position(curPos);
		return curPos - prevPos;
	}

	public void resetMark() {
		int curPos = streamBuffer.position();
		rewind();
		streamBuffer.position(curPos);
	}
}
