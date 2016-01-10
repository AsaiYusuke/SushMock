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

	public Sequence getSequence(int sequenceNum) throws SequenceNotFound {
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
