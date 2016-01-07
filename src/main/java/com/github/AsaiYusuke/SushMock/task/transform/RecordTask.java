package com.github.AsaiYusuke.SushMock.task.transform;

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

	public RecordTask() {
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
		if (Constants.Option.getExecutionType() != ExecutionType.Record) {
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
			boolean result = true;
			while (result) {
				result = false;

				LinkedList<HistoryBuffer> loopHistoryList = new LinkedList<>();
				loopHistoryList.addAll(tempHistoryList);
				tempHistoryList.clear();

				HistoryBuffer prevHistory = loopHistoryList.poll();
				for (HistoryBuffer curHistory : loopHistoryList) {
					if (prevHistory != null) {
						if (prevHistory.getType().equals(StreamType.Input)
								&& curHistory.getType()
										.equals(StreamType.Output)) {

							int prevLen = prevHistory.getLength();
							int curLen = curHistory.getLength();
							int compSize = curLen > prevLen ? prevLen : curLen;

							byte[] partPrevBuf = prevHistory
									.getStream(compSize);
							byte[] partCurBuf = curHistory.getStream(compSize);

							if (Arrays.equals(partPrevBuf, partCurBuf)) {
								HistoryBuffer history = new HistoryBuffer(
										StreamType.InputEcho);
								history.addStream(partPrevBuf);
								tempHistoryList.add(history);

								if (prevLen > compSize) {
									byte[] restBuf = prevHistory.getStream();
									history = new HistoryBuffer(
											prevHistory.getType());
									history.addStream(restBuf);
									tempHistoryList.add(history);
								}

								if (curLen > compSize) {
									byte[] restBuf = curHistory.getStream();
									history = new HistoryBuffer(
											curHistory.getType());
									history.addStream(restBuf);
									prevHistory = history;
								} else {
									prevHistory = null;
								}

								result = true;
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

					byte[] prevBuf = prevHistory.getStream();
					byte[] curBuf = curHistory.getStream();

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

	private void recordTask() throws TaskSleepRequired {
		if (historyList.size() > 0) {
			StreamType type0 = historyList.get(0).getType();
			if (historyList.size() == 1) {
				if (type0.equals(StreamType.Input)
						|| type0.equals(StreamType.InputEcho)) {
					throw new TaskSleepRequired();
				}
			} else {
				StreamType type1 = historyList.get(1).getType();
				if (type0.equals(StreamType.InputEcho)
						&& (type1.equals(StreamType.Input))) {
					throw new TaskSleepRequired();
				}
			}

			HistoryBuffer history = historyList.poll();

			StreamType type = history.getType();
			byte[] buf = history.getStream();

			for (RecordTransformer trans : exts.values()) {
				buf = trans.transform(buf, type);
			}

			try {
				// sequenceの指す対象は現在の位置。setNextLine後の初期値はnull。
				record.setNextSequence();

				Sequence sequence = record.getSequence();
				while (!sequence.checkFile(buf)) {
					record.setNextLine();
					record.setNextSequence();
					sequence = record.getSequence();
				}

			} catch (LineNotFound e) {
				try {
					record.createNextLine();
					Sequence sequence = record.createNextSequence(type);
					sequence.setByteArray(buf);

				} catch (SequenceNotFound e1) {
				}

			} catch (SequenceNotFound e) {
				Sequence sequence = record.createNextSequence(type);
				sequence.setByteArray(buf);
			}
		} else {
			throw new TaskSleepRequired();
		}
	}
}
