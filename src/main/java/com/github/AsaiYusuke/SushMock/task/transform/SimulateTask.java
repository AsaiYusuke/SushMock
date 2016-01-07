package com.github.AsaiYusuke.SushMock.task.transform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.AsaiYusuke.SushMock.exception.LineNotFound;
import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;
import com.github.AsaiYusuke.SushMock.exception.TaskSleepRequired;
import com.github.AsaiYusuke.SushMock.ext.SimulateTransformer;
import com.github.AsaiYusuke.SushMock.record.Record;
import com.github.AsaiYusuke.SushMock.record.Sequence;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;
import com.github.AsaiYusuke.SushMock.util.PipedStream;

public class SimulateTask extends AbstractTransformTask {

	private PipedStream out;
	private PipedStream err;

	private String dataDirStr;

	private HistoryBuffer historyBuffer;

	private Record record;

	private static Map<String, SimulateTransformer> exts;

	public SimulateTask() throws IOException {
		super();

		out = new PipedStream();
		err = new PipedStream();

		dataDirStr = Constants.Option.getDataDir();
		record = new Record(dataDirStr);

		historyBuffer = new HistoryBuffer(StreamType.Input);

		// SimulateTransformer Extensions
		if (exts == null) {
			exts = Constants.Option
					.getExtensionsOfType(SimulateTransformer.class);
		}
	}

	@Override
	public void addStream(StreamType type, byte[] buf) {
		if (Constants.Option.getExecutionType() != ExecutionType.Simulate) {
			return;
		}

		if (type != StreamType.Input) {
			return;
		}

		if (buf != null && buf.length > 0) {

			try {
				lock();
				historyBuffer.addStream(buf);
			} finally {
				unlock();
			}
		}

	}

	@Override
	public void flushStream() {

	}

	@Override
	protected void task() throws TaskSleepRequired {
		try {
			// sequenceの指す対象は現在の位置。setNextLine後の初期値はnull。
			Sequence sequence = record.getNextSequence();

			switch (sequence.getType()) {
			case Output:
			case Error:
				outputTask(sequence);
				break;
			case Input:
				inputTask(sequence);
				break;
			case InputEcho:
				inputEchoTask(sequence);
				break;
			default:
				break;
			}

			throw new TaskSleepRequired();

		} catch (IOException e) {
			// TODO ここはエラー
			e.printStackTrace();
			setActive(false);
		} catch (LineNotFound e) {
			// TODO ここはエラー
			e.printStackTrace();
			setActive(false);
		} catch (SequenceNotFound e) {
			// Normal Exit
			System.out.println("completed.");
			setActive(false);
		}
	}

	@Override
	protected void finalTask() {
	}

	public InputStream getOutDst() {
		return out.getDst();
	}

	public InputStream getErrDst() {
		return err.getDst();
	}

	private void outputTask(Sequence sequence)
			throws SequenceNotFound, IOException {

		record.setNextSequence();

		byte[] buf = simulateTransform(sequence);
		StreamType type = sequence.getType();
		if (type == StreamType.Output) {
			out.getSrc().write(buf);
		} else if (type == StreamType.Error) {
			err.getSrc().write(buf);
		}
	}

	private void inputTask(Sequence sequence)
			throws SequenceNotFound, LineNotFound, IOException {
		Path path = sequence.getPath();
		try {
			lock();
			int bufLen = historyBuffer.getLength();
			long fileLen = Files.size(path);
			if (bufLen >= fileLen) {
				record.setNextSequence();
				byte[] partBuffer = historyBuffer.getStream((int) fileLen);

				if (!sequence.checkFile(partBuffer)) {
					record.setNextLine();
				} else {
					if (bufLen == fileLen) {
						historyBuffer.rewind();
					}
				}
			}
		} finally {
			unlock();
		}
	}

	private void inputEchoTask(Sequence sequence)
			throws SequenceNotFound, LineNotFound, IOException {
		// 重複コード・・・
		Path path = sequence.getPath();
		try {
			lock();
			int bufLen = historyBuffer.getLength();
			long fileLen = Files.size(path);
			if (bufLen >= fileLen) {
				record.setNextSequence();
				byte[] partBuffer = historyBuffer.getStream((int) fileLen);

				if (!sequence.checkFile(partBuffer)) {
					record.setNextLine();
				} else {
					byte[] buf = simulateTransform(sequence);
					out.getSrc().write(buf);

					if (bufLen == fileLen) {
						historyBuffer.rewind();
					}
				}
			}
		} finally {
			unlock();
		}
	}

	private byte[] simulateTransform(Sequence sequence) {
		byte[] buf = sequence.getByteArray();
		StreamType type = sequence.getType();
		for (SimulateTransformer trans : exts.values()) {
			buf = trans.transform(buf, type);
		}
		return buf;
	}
}
