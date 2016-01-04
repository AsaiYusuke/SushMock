package com.github.AsaiYusuke.SushMock.record;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.AsaiYusuke.SushMock.exception.LineNotFound;
import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class Record {
	private String dataDirStr;

	private ArrayList<Line> lines;

	private Line currentLine;
	private Sequence currentSequence;

	public Record(String dataDirStr) {
		this.dataDirStr = dataDirStr;

		File dataDir = new File(dataDirStr);
		if (!dataDir.exists()) {
			dataDir.mkdir();
		}

		reload();
	}

	public void reload() {
		if (lines == null) {
			lines = new ArrayList<>();
		}
		lines.clear();

		Pattern p = Pattern
				.compile("([0-9]+)_([0-9]+)_([^_]+)(_([0-9]+)){0,1}");

		File dataDir = new File(dataDirStr);

		for (File fileEntry : dataDir.listFiles()) {
			try {
				Matcher m = p.matcher(fileEntry.getName());
				if (!m.find()) {
					throw new FileNotFoundException();
				}

				int lineNum = Integer.parseInt(m.group(1));
				int sequenceNum = Integer.parseInt(m.group(2));
				String fileTypeStr = m.group(3);
				StreamType type = StreamType.valueOf(fileTypeStr);

				Sequence sequence = new Sequence();
				sequence.setFile(fileEntry);
				sequence.setId(lineNum, sequenceNum);
				sequence.setType(type);

				while (lineNum >= lines.size()) {
					lines.add(new Line());
				}
				Line line = lines.get(lineNum);
				line.setSequence(sequenceNum, sequence);

				if (m.group(4) != null && m.group(5) != null) {
					int nextLineNum = Integer.parseInt(m.group(5));
					while (nextLineNum >= lines.size()) {
						lines.add(new Line());
					}
					Line nextLine = lines.get(nextLineNum);

					sequence.setNextLine(nextLine);
				}

			} catch (FileNotFoundException e) {
			}
		}

		for (Line line : lines) {
			Sequence prevSequence = null;
			for (Sequence sequence : line.getSequences()) {
				if (prevSequence != null) {
					prevSequence.setNextSequence(sequence);
				}
				prevSequence = sequence;
			}
		}

		if (lines.size() == 0) {
			lines.add(new Line());
		}
		setLine(lines.get(0));
	}

	public Sequence getSequence() throws SequenceNotFound {
		if (currentSequence == null) {
			throw new SequenceNotFound();
		}
		return currentSequence;
	}

	private void setLine(Line line) {
		currentLine = line;
		currentSequence = null;
	}

	public void setNextLine() throws LineNotFound {
		Line nextLine = currentSequence.getNextLine();
		if (nextLine == null) {
			throw new LineNotFound();
		}
		setLine(nextLine);
	}

	public void createNextLine() throws SequenceNotFound {
		Sequence sequence = getSequence();

		Line line = new Line();
		lines.add(line);

		String oldFileName = getFileName();
		sequence.setNextLine(line);
		String newFileName = getFileName();

		// TODO JavaDocによるとFiles.move()のほうがPF非依存でよいらしい
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);
		oldFile.renameTo(newFile);

		try {
			this.setNextLine();
		} catch (LineNotFound e) {
		}
	}

	public Sequence getNextSequence() throws SequenceNotFound {
		Sequence nextSequence;
		if (currentSequence != null) {
			nextSequence = currentSequence.getNextSequence();
		} else {
			nextSequence = currentLine.getSequence(0);
		}
		if (nextSequence == null) {
			throw new SequenceNotFound();
		}
		return nextSequence;
	}

	public void setNextSequence() throws SequenceNotFound {
		Sequence nextSequence = getNextSequence();
		currentSequence = nextSequence;
	}

	public Sequence createNextSequence(StreamType type) {
		try {
			Sequence sequence = new Sequence();
			sequence.setType(type);

			int lineNum = lines.indexOf(currentLine);
			int sequenceNum = currentLine.size();
			sequence.setId(lineNum, sequenceNum);

			currentLine.addSequence(sequence);

			if (currentSequence != null) {
				currentSequence.setNextSequence(sequence);
			}
			currentSequence = sequence;

			File file = new File(getFileName());
			sequence.setFile(file);

		} catch (SequenceNotFound e) {
			e.printStackTrace();
		}

		return currentSequence;
	}

	private String getFileName() throws SequenceNotFound {
		Sequence sequence = getSequence();

		int lineNum = lines.indexOf(currentLine);
		int sequenceNum = currentLine.getIndexOf(sequence);
		if (sequenceNum < 0) {
			sequenceNum = currentLine.size();
		}

		StreamType type = sequence.getType();

		String filename = String.format(Constants.DefaultFileFormat, lineNum,
				sequenceNum, type);

		if (sequence.getNextLine() != null) {
			int nextLineNum = lines.indexOf(currentSequence.getNextLine());
			filename = String.format("%1$s_%2$02d", filename, nextLineNum);
		}

		return dataDirStr + "\\" + filename;
	}

}
