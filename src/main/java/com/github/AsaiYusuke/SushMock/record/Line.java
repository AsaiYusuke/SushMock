package com.github.AsaiYusuke.SushMock.record;

import java.util.ArrayList;

import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;

public class Line {
	private ArrayList<Sequence> sequences;

	public Line() {
		this.sequences = new ArrayList<>();
	}

	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	public void setSequence(int sequenceNum, Sequence sequence) {
		while (sequenceNum >= sequences.size()) {
			sequences.add(new DummySequence());
		}
		sequences.set(sequenceNum, sequence);
	}

	public Sequence getSequence(int sequenceNum)
			throws SequenceNotFound {
		if (sequenceNum >= sequences.size()) {
			throw new SequenceNotFound();
		}
		return sequences.get(sequenceNum);
	}

	public ArrayList<Sequence> getSequences() {
		return sequences;
	}

	public int size() {
		return sequences.size();
	}

	public int getIndexOf(Sequence sequence) {
		return sequences.indexOf(sequence);
	}
}
