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
package com.github.AsaiYusuke.SushMock.record;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.AsaiYusuke.SushMock.exception.LineNotFound;
import com.github.AsaiYusuke.SushMock.exception.SequenceNotFound;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class Record {
	private String dataDirStr;

	private static ArrayList<Line> lines;
	private static Lock lock;

	private Line currentLine;
	private Sequence currentSequence;

	public Record(String dataDirStr) throws IOException {
		this.dataDirStr = dataDirStr;

		Path dataDirPath = Paths.get(dataDirStr);
		if (!Files.exists(dataDirPath,
				new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			Files.createDirectories(dataDirPath);
		}

		lock = new ReentrantLock();
		reload();
	}

	private void lock() {
		lock.lock();
	}

	private void unlock() {
		lock.unlock();
	}

	public void reload() {
		try {
			lock();

			if (lines == null || lines.size() == 0) {
				lines = new ArrayList<>();

				Pattern p = Pattern
						.compile("([0-9]+)_([0-9]+)_([^_]+)(_([0-9]+)){0,1}");

				Path dataDirPath = Paths.get(dataDirStr);

				try (DirectoryStream<Path> stream = Files
						.newDirectoryStream(dataDirPath)) {
					for (Path filePath : stream) {
						Matcher m = p
								.matcher(filePath.getFileName().toString());
						if (!m.find()) {
							continue;
						}

						int lineNum = Integer.parseInt(m.group(1));
						int sequenceNum = Integer.parseInt(m.group(2));
						String fileTypeStr = m.group(3);
						StreamType type = StreamType.valueOf(fileTypeStr);

						Sequence sequence = new Sequence();
						sequence.setPath(filePath);
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
					}
				} catch (IOException e) {
					e.printStackTrace();
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
			}
		} finally {
			unlock();
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

		try {
			lock();
			Line line = new Line();
			lines.add(line);

			String oldFileName = getFileName();
			sequence.setNextLine(line);
			String newFileName = getFileName();

			Path srcPath = Paths.get(oldFileName);
			Path dstPath = Paths.get(newFileName);
			Files.move(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unlock();
		}

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
			lock();

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

			Path path = Paths.get(getFileName());
			sequence.setPath(path);

		} catch (SequenceNotFound e) {
			e.printStackTrace();
		} finally {
			unlock();
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

		String filename = String.format("%1$02d_%2$04d_%3$s", lineNum,
				sequenceNum, type);

		if (sequence.getNextLine() != null) {
			int nextLineNum = lines.indexOf(currentSequence.getNextLine());
			filename = String.format("%1$s_%2$02d", filename, nextLineNum);
		}

		return dataDirStr + "\\" + filename;
	}

}
