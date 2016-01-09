package com.github.AsaiYusuke.SushMock.task.transform;

import java.io.IOException;
import java.io.InputStream;
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
			case Input:
			case InputEcho:
				inputTask(sequence);
				break;
			case Output:
			case Error:
				outputTask(sequence);
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

	private void inputTask(Sequence sequence)
			throws SequenceNotFound, LineNotFound, IOException {
		try {
			lock();
			int cmpLen = sequence.compareLength(historyBuffer);
			if (cmpLen >= 0) {
				record.setNextSequence();

				if (sequence.compareByte(historyBuffer)) {
					int fileLen = sequence.getLength();
					historyBuffer.cutStream(fileLen);

					if (cmpLen == 0) {
						historyBuffer.rewind();
					}

					if (sequence.getType() == StreamType.InputEcho) {
						writeBuffer(sequence);
					}

				} else {
					record.setNextLine();
				}
			}
		} finally {
			unlock();
		}
	}

	private void outputTask(Sequence sequence)
			throws SequenceNotFound, IOException {

		record.setNextSequence();

		writeBuffer(sequence);
	}

	private void writeBuffer(Sequence sequence) throws IOException {
		byte[] buf = simulateTransform(sequence);
		switch (sequence.getType()) {
		case Output:
		case InputEcho:
			out.getSrc().write(buf);
			break;
		case Error:
			err.getSrc().write(buf);
			break;
		default:
			break;
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
