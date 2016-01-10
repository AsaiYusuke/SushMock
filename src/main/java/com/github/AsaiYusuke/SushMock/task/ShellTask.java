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
package com.github.AsaiYusuke.SushMock.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.sshd.client.channel.ClientChannel;

import com.github.AsaiYusuke.SushMock.task.capture.CaptureErrTask;
import com.github.AsaiYusuke.SushMock.task.capture.CaptureInTask;
import com.github.AsaiYusuke.SushMock.task.capture.CaptureOutTask;
import com.github.AsaiYusuke.SushMock.task.transform.AbstractTransformTask;
import com.github.AsaiYusuke.SushMock.task.transform.RecordTask;
import com.github.AsaiYusuke.SushMock.task.transform.SimulateTask;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;
import com.github.AsaiYusuke.SushMock.util.PipedStream;

/**
 * <pre>
 *  +--------+--------------------------------------------------------+--------+
 *  | server |                        ShellTask                       | client |
 *  +--------+--------------------------------------------------------+--------+
 *  |        |                     +------------+                     |        |
 *  |       -> Src [serverIn] Dst -> CaptureIn  -> Src [clientIn] Dst ->       |
 *  |        |                     |            |                     |        |
 *  |       <- Dst [serverOut]Src <- CaptureOut <- Dst [clientOut]Src <-       |
 *  |        |                     |            |                     |        |
 *  |       <- Dst [serverErr]Src <- CaptureErr <- Dst [clientErr]Src <-       |
 *  |        |                     +------------+                     |        |
 *  +--------+--------------------------------------------------------+--------+
 * </pre>
 */
public class ShellTask {

	private ExecutorService executor = Executors.newFixedThreadPool(5);

	private CaptureInTask capIn;
	private CaptureOutTask capOut;
	private CaptureErrTask capErr;

	private AbstractTransformTask transform;

	private PipedStream clientIn;
	private PipedStream clientOut;
	private PipedStream clientErr;
	private PipedStream serverIn;
	private PipedStream serverOut;
	private PipedStream serverErr;

	public ShellTask() throws IOException {
		clientIn = new PipedStream();
		clientOut = new PipedStream();
		clientErr = new PipedStream();
		serverIn = new PipedStream();
		serverOut = new PipedStream();
		serverErr = new PipedStream();

		if (Constants.Option.getExecutionType() == ExecutionType.Record) {// RecordShellTaskサブクラスにしたほうがよさそう
			transform = new RecordTask();

			capIn = new CaptureInTask(serverIn.getDst(), clientIn.getSrc());
			capIn.setTransformTask(transform);

			capOut = new CaptureOutTask(clientOut.getDst(), serverOut.getSrc());
			capOut.setTransformTask(transform);

			capErr = new CaptureErrTask(clientErr.getDst(), serverErr.getSrc());
			capErr.setTransformTask(transform);

		} else {
			transform = new SimulateTask();
			SimulateTask simulateTask = (SimulateTask) transform;

			capIn = new CaptureInTask(serverIn.getDst(), null);
			capIn.setTransformTask(simulateTask);

			capOut = new CaptureOutTask(simulateTask.getOutDst(),
					serverOut.getSrc());
			capOut.setTransformTask(null);

			capErr = new CaptureErrTask(simulateTask.getErrDst(),
					serverErr.getSrc());
			capErr.setTransformTask(null);
		}

	}

	public InputStream getClientIn() {
		return clientIn.getDst();
	}

	public OutputStream getClientOut() {
		return clientOut.getSrc();
	}

	public OutputStream getClientErr() {
		return clientErr.getSrc();
	}

	public OutputStream getServerIn() {
		return serverIn.getSrc();
	}

	public InputStream getServerOut() {
		return serverOut.getDst();
	}

	public InputStream getServerErr() {
		return serverErr.getDst();
	}

	public void setChannel(ClientChannel channel) {
		if (capIn != null) {
			capIn.setChannel(channel);
		}
		if (capOut != null) {
			capOut.setChannel(channel);
		}
		if (capErr != null) {
			capErr.setChannel(channel);
		}
		if (transform != null) {
			transform.setChannel(channel);
		}
	}

	public void execute() {
		if (transform != null) {
			executor.execute(transform);
		}
		if (capIn != null) {
			executor.execute(capIn);
		}
		if (capOut != null) {
			executor.execute(capOut);
		}
		if (capErr != null) {
			executor.execute(capErr);
		}
	}

	public boolean isAlive() {
		boolean result = true;
		if (transform != null) {
			result = result & transform.isActive();
		}
		if (capIn != null) {
			result = result & capIn.isActive();
		}
		if (capOut != null) {
			result = result & capOut.isActive();
		}
		if (capErr != null) {
			result = result & capErr.isActive();
		}
		return result;
	}

	public void destroy() {
		if (transform != null) {
			transform.setActive(false);
		}
		if (capIn != null) {
			capIn.setActive(false);
		}
		if (capOut != null) {
			capOut.setActive(false);
		}
		if (capErr != null) {
			capErr.setActive(false);
		}
	}

}
