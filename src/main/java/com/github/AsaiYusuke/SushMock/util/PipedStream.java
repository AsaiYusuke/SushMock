package com.github.AsaiYusuke.SushMock.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedStream {

	private PipedOutputStream src;
	private PipedInputStream dst;

	public PipedStream() throws IOException {
		int pipeSize = Constants.PipedStreamSize;
		this.src = new PipedOutputStream();
		this.dst = new PipedInputStream(pipeSize);
		src.connect(dst);
	}

	public PipedOutputStream getSrc() {
		return this.src;
	}

	public PipedInputStream getDst() {
		return this.dst;
	}

}