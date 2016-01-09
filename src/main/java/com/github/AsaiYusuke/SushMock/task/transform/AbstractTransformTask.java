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
