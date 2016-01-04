package com.github.AsaiYusuke.SushMock.task.capture;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.AsaiYusuke.SushMock.task.transform.AbstractTransformTask;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class CaptureInTask extends AbstractStreamTask {

	public CaptureInTask(InputStream input, OutputStream output) {
		super(input, output);
	}

	@Override
	protected void task(byte[] buffer) {
		AbstractTransformTask transformTask = getTransformTask();
		if (transformTask != null) {
			transformTask.addStream(StreamType.Input, buffer);
		}
	}

	@Override
	protected void finalTask() {
		AbstractTransformTask transformTask = getTransformTask();
		if (transformTask != null) {
			transformTask.flushStream();
		}
	}

}
