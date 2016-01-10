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

import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.AsaiYusuke.SushMock.exception.TaskSleepRequired;
import com.github.AsaiYusuke.SushMock.record.Sequence;
import com.github.AsaiYusuke.SushMock.task.AbstractShellSubTask;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public abstract class AbstractTransformTask extends AbstractShellSubTask
		implements Runnable {

	protected Lock lock;

	public AbstractTransformTask() {
		super();

		lock = new ReentrantLock();
	}

	protected void lock() {
		lock.lock();
	}

	protected void unlock() {
		lock.unlock();
	}

	public abstract void addStream(StreamType type, byte[] buf);

	public abstract void flushStream();

	@Override
	public void run() {
		while (channel.isOpen() && isActive()) {
			try {
				task();

			} catch (TaskSleepRequired e) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		finalTask();

		setActive(false);
	}

	protected abstract void task() throws TaskSleepRequired;

	protected abstract void finalTask();

	public void printLog(Sequence sequence) {
		StreamType type = sequence.getType();
		int lineNum = sequence.getLineNum();
		int seqNum = sequence.getSequenceNum();
		String stream = "";
		try {
			stream = new String(sequence.getByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		System.out.println(String.format(
				"\nType:%10s, Line:%2d, Sequence:%4d\n-----\n%s\n-----",
				type.toString(), lineNum, seqNum, stream));
	}
}
