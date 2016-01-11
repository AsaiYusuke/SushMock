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
package com.github.AsaiYusuke.SushMock.task.capture;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.AsaiYusuke.SushMock.task.transform.AbstractTransformTask;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class CaptureOutTask extends AbstractStreamTask {

	public CaptureOutTask(InputStream input, OutputStream output) {
		super(input, output);
	}

	@Override
	protected void task(byte[] buffer) {
		AbstractTransformTask transformTask = getTransformTask();
		if (transformTask != null) {
			transformTask.addStream(StreamType.OUT, buffer);
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
