package com.github.AsaiYusuke.SushMock.exception;

import com.github.AsaiYusuke.SushMock.record.Sequence;

public class InvalidDataFound extends Exception {

	private Sequence prevSequence;
	private byte[] stream;

	public InvalidDataFound(Sequence prevSequence, byte[] stream) {
		this.prevSequence = prevSequence;
		this.stream = stream;
	}

	public Sequence getPrevSequence() {
		return prevSequence;
	}

	public void setPrevSequence(Sequence prevSequence) {
		this.prevSequence = prevSequence;
	}

	public byte[] getStream() {
		return stream;
	}

	public void setStream(byte[] stream) {
		this.stream = stream;
	}
}
