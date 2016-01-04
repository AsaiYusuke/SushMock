package com.github.AsaiYusuke.SushMock.task.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.bouncycastle.util.Arrays;

import com.github.AsaiYusuke.SushMock.exception.ExecutionModeMismatch;
import com.github.AsaiYusuke.SushMock.exception.LineNotFound;
import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;
import com.github.AsaiYusuke.SushMock.exception.TaskSleepRequired;
import com.github.AsaiYusuke.SushMock.ext.SimulateTransformer;
import com.github.AsaiYusuke.SushMock.record.Record;
import com.github.AsaiYusuke.SushMock.record.Sequence;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.PipedStream;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class SimulateTask extends AbstractTransformTask {

	private PipedStream out;
	private PipedStream err;

	private String dataDirStr;

	private HistoryBuffer historyBuffer;

	private Record record;

	private static Map<String, SimulateTransformer> exts;

	public SimulateTask() throws IOException, ExecutionModeMismatch {
		super();

		if (Constants.Option.getExecutionType() != ExecutionType.Simulate) {
			throw new ExecutionModeMismatch();
		}

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

		StreamType type = sequence.getType();

		record.setNextSequence();

		byte[] buf = sequence.getByteArray();
		for (SimulateTransformer trans : exts.values()) {
			buf = trans.transform(buf, type);
		}

		if (type == StreamType.Output) {
			out.getSrc().write(buf);
		} else if (type == StreamType.Error) {
			err.getSrc().write(buf);
		}
	}

	private void inputTask(Sequence sequence)
			throws SequenceNotFound, LineNotFound {
		File file = sequence.getFile();
		try {
			lock();
			if (historyBuffer.getPosition() >= file.length()) {
				record.setNextSequence();
				byte[] partBuffer = Arrays.copyOfRange(
						historyBuffer.getStream((int) file.length()), 0,
						(int) file.length());

				if (!sequence.checkFile(partBuffer)) {
					record.setNextLine();
				} else {
					if (historyBuffer.getPosition() == file.length()) {
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
		File file = sequence.getFile();
		try {
			lock();
			if (historyBuffer.getPosition() >= file.length()) {
				record.setNextSequence();
				byte[] partBuffer = Arrays.copyOfRange(
						historyBuffer.getStream((int) file.length()), 0,
						(int) file.length());

				if (!sequence.checkFile(partBuffer)) {
					record.setNextLine();
				} else {
					byte[] buf = sequence.getByteArray();
					StreamType type = sequence.getType();

					for (SimulateTransformer trans : exts.values()) {
						buf = trans.transform(buf, type);
					}
					out.getSrc().write(buf);

					if (historyBuffer.getPosition() == file.length()) {
						historyBuffer.rewind();
					}
				}
			}
		} finally {
			unlock();
		}
	}
}
