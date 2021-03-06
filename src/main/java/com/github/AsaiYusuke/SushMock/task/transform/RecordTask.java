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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import com.github.AsaiYusuke.SushMock.exception.LineNotFound;
import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;
import com.github.AsaiYusuke.SushMock.exception.TaskSleepRequired;
import com.github.AsaiYusuke.SushMock.ext.RecordTransformer;
import com.github.AsaiYusuke.SushMock.record.Record;
import com.github.AsaiYusuke.SushMock.record.Sequence;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;
import com.google.common.collect.Lists;

public class RecordTask extends AbstractTransformTask {

	private LinkedList<HistoryBuffer> historyList;

	private String dataDirStr;
	private HistoryBuffer streamBuffer;

	private Record record;

	private static Map<String, RecordTransformer> exts;

	public RecordTask() throws IOException {
		super();

		historyList = Lists.newLinkedList();
		dataDirStr = Constants.Option.getDataDir();

		record = new Record(dataDirStr);

		// RecordTransformer Extensions
		if (exts == null) {
			exts = Constants.Option
					.getExtensionsOfType(RecordTransformer.class);
		}
	}

	@Override
	public void addStream(StreamType type, byte[] buf) {
		if (Constants.Option.getExecutionType() != ExecutionType.RECORD) {
			return;
		}

		if (buf != null && buf.length > 0) {
			try {
				lock();
				if (streamBuffer == null) {
					streamBuffer = new HistoryBuffer(type);
				}

				if (streamBuffer.getType() != type) {
					historyList.offer(streamBuffer);
					streamBuffer = new HistoryBuffer(type);
				}

				if (!streamBuffer.checkBufferSize(buf)) {
					historyList.offer(streamBuffer);
					streamBuffer = new HistoryBuffer(type);
				}

				streamBuffer.addStream(buf);

			} finally {
				unlock();
			}
		}
	}

	@Override
	public void flushStream() {
		try {
			lock();
			if (streamBuffer != null && streamBuffer.getPosition() > 0) {
				historyList.offer(streamBuffer);
			}
		} finally {
			unlock();
		}
	}

	@Override
	public void task() throws TaskSleepRequired {
		try {
			lock();
			convertInputEchoStream();
			concatStream();
			recordTask();
		} catch (IOException e) {
			// TODO File in/out error
			e.printStackTrace();
			setActive(false);
		} finally {
			unlock();
		}
	}

	@Override
	protected void finalTask() {
		while (historyList.size() > 0) {
			try {
				task();
			} catch (TaskSleepRequired e) {
				break;
			}
		}
	}

	private void convertInputEchoStream() {
		if (historyList.size() >= 2) {
			LinkedList<HistoryBuffer> tempHistoryList = new LinkedList<>();

			tempHistoryList.addAll(historyList);
			boolean loop = true;
			while (loop) {
				loop = false;

				LinkedList<HistoryBuffer> loopHistoryList = new LinkedList<>();
				loopHistoryList.addAll(tempHistoryList);
				tempHistoryList.clear();

				HistoryBuffer prevHistory = loopHistoryList.poll();
				for (HistoryBuffer curHistory : loopHistoryList) {
					if (prevHistory != null) {
						if (prevHistory.getType().equals(StreamType.IN)
								&& curHistory.getType()
										.equals(StreamType.OUT)) {

							int prevLen = prevHistory.getLength();
							int curLen = curHistory.getLength();
							int compSize = curLen > prevLen ? prevLen : curLen;

							byte[] partPrevBuf = prevHistory
									.cutStream(compSize);
							byte[] partCurBuf = curHistory.cutStream(compSize);

							if (Arrays.equals(partPrevBuf, partCurBuf)) {
								HistoryBuffer history = new HistoryBuffer(
										StreamType.IN_ECHO);
								history.addStream(partPrevBuf);
								tempHistoryList.add(history);

								if (prevLen > compSize) {
									byte[] restBuf = prevHistory.cutStream();
									history = new HistoryBuffer(
											prevHistory.getType());
									history.addStream(restBuf);
									tempHistoryList.add(history);
								}

								if (curLen > compSize) {
									byte[] restBuf = curHistory.cutStream();
									history = new HistoryBuffer(
											curHistory.getType());
									history.addStream(restBuf);
									prevHistory = history;
								} else {
									prevHistory = null;
								}

								loop = true;
								continue;

							} else {
								prevHistory.resetMark();
								curHistory.resetMark();
							}
						}
						tempHistoryList.add(prevHistory);
					}
					prevHistory = curHistory;
				}
				if (prevHistory != null) {
					tempHistoryList.add(prevHistory);
				}
			}
			historyList.clear();
			historyList.addAll(tempHistoryList);
		}
	}

	private void concatStream() {
		if (historyList.size() >= 2) {
			LinkedList<HistoryBuffer> tempHistoryList = new LinkedList<>();

			HistoryBuffer prevHistory = historyList.poll();
			for (HistoryBuffer curHistory : historyList) {
				if (prevHistory.getType().equals(curHistory.getType())) {

					byte[] prevBuf = prevHistory.cutStream();
					byte[] curBuf = curHistory.cutStream();

					int prevSize = prevBuf.length;
					int curSize = curBuf.length;

					byte[] resultBuf = new byte[prevSize + curSize];
					System.arraycopy(prevBuf, 0, resultBuf, 0, prevSize);
					System.arraycopy(curBuf, 0, resultBuf, prevSize, curSize);

					prevHistory.rewind();
					prevHistory.addStream(resultBuf);

				} else {
					tempHistoryList.add(prevHistory);
					prevHistory = curHistory;
				}
			}
			tempHistoryList.add(prevHistory);

			historyList.clear();
			historyList.addAll(tempHistoryList);
		}
	}

	private void recordTask() throws TaskSleepRequired, IOException {
		if (historyList.size() > 0) {
			StreamType type0 = historyList.get(0).getType();
			if (historyList.size() == 1) {
				if (type0.equals(StreamType.IN)
						|| type0.equals(StreamType.IN_ECHO)) {
					throw new TaskSleepRequired();
				}
			} else if (historyList.size() == 2) {
				StreamType type1 = historyList.get(1).getType();
				if (type0.equals(StreamType.IN_ECHO)
						&& (type1.equals(StreamType.IN))) {
					throw new TaskSleepRequired();
				}
			}

			HistoryBuffer historyBuffer = historyList.poll();

			try {
				record.setNextSequence();

				while (true) {
					Sequence sequence = record.getSequence();

					int cmpLen = sequence.compareLength(historyBuffer);
					if (cmpLen >= 0) {
						if (sequence.compareByte(historyBuffer)) {
							printLog(sequence);

							int fileLen = sequence.getLength();
							historyBuffer.cutStream(fileLen);
							if (cmpLen == 0) {
								break;
							}
							record.setNextSequence();
							continue;
						}
					}

					record.setNextLine();
					record.setNextSequence();
				}

			} catch (LineNotFound e) {
				try {
					record.createNextLine();
					StreamType type = historyBuffer.getType();
					byte[] buf = recordTransform(historyBuffer);

					Sequence sequence = record.createNextSequence(type);
					sequence.setByteArray(buf);

					printLog(sequence);

				} catch (SequenceNotFound e1) {
				}

			} catch (SequenceNotFound e) {
				StreamType type = historyBuffer.getType();
				byte[] buf = recordTransform(historyBuffer);

				Sequence sequence = record.createNextSequence(type);
				sequence.setByteArray(buf);

				printLog(sequence);

			}
		} else {
			throw new TaskSleepRequired();
		}
	}

	private byte[] recordTransform(HistoryBuffer historyBuffer) {
		byte[] buf = historyBuffer.cutStream();
		StreamType type = historyBuffer.getType();
		for (RecordTransformer trans : exts.values()) {
			buf = trans.transform(buf, type);
		}
		return buf;
	}
}
