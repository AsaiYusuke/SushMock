package com.github.AsaiYusuke.SushMock.task.capture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.AsaiYusuke.SushMock.task.AbstractShellSubTask;
import com.github.AsaiYusuke.SushMock.task.transform.AbstractTransformTask;

public abstract class AbstractStreamTask extends AbstractShellSubTask
		implements Runnable {

	private InputStream input;
	private OutputStream output;

	private AbstractTransformTask transform;

	private int queueLength;

	public AbstractStreamTask(InputStream input, OutputStream output) {
		setInput(input);
		setOutput(output);
		queueLength = 0;
	}

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}

	public AbstractTransformTask getTransformTask() {
		return transform;
	}

	public void setTransformTask(AbstractTransformTask transform) {
		this.transform = transform;
	}

	@Override
	public void run() {
		while (channel.isOpen() && isActive()) {
			try {
				queueLength = input.available();
				if (queueLength > 0) {
					byte[] buffer = new byte[queueLength];
					input.read(buffer);
					task(buffer);
					if (output != null) {
						output.write(buffer);
						output.flush();
					}
				} else if (queueLength < 0) {
					input.close();
					output.close();
					break;
				} else {
					Thread.sleep(10);
				}
			} catch (IOException | InterruptedException e) {
				try {
					input.close();
					output.close();
				} catch (IOException e1) {
				}
				break;
			}
		}
		finalTask();

		setActive(false);
	}

	protected abstract void task(byte[] buffer);

	protected abstract void finalTask();

}
