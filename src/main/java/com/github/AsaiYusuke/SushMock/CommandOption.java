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
package com.github.AsaiYusuke.SushMock;

import static com.google.common.collect.Maps.*;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.AsaiYusuke.SushMock.ext.Extension;
import com.github.AsaiYusuke.SushMock.ext.ExtensionLoader;
import com.github.AsaiYusuke.SushMock.ext.defaultExt.DateMaskCompareTransformer;
import com.github.AsaiYusuke.SushMock.util.Constants;
import com.github.AsaiYusuke.SushMock.util.Constants.ExecutionType;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class CommandOption {
	private ExecutionType executionType;
	private int listenPort;
	private String remoteHost;
	private int remotePort;
	private String dataDir;
	private String fileFormat;
	private int historyBufferSize;
	private String[] extensionClasses;
	private boolean isVerbose;

	private Map<String, Extension> extensions = newLinkedHashMap();

	public CommandOption() {
		executionType = ExecutionType.Unknown;
		listenPort = Constants.DefaultListenPort;
		remoteHost = Constants.DefaultRemoteHost;
		remotePort = Constants.DefaultRemotePort;
		dataDir = Constants.DefaultDataDir;
		fileFormat = Constants.DefaultFileFormat;
		historyBufferSize = Constants.DefaultHistoryBufferSize;

		Constants.Option = this;

	}

	public void parse(String[] args) {
		String header = "[options]";
		String footer = "";

		Options options = new Options();

		options.addOption(Option.builder() //
				.longOpt("mode") //
				.desc("set the execution mode ["
						+ Constants.DefaultExecutionType.toString() + "]") //
				.hasArg().argName("mode") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("data") //
				.desc("set the mock data directory [" + Constants.DefaultDataDir
						+ "]") //
				.hasArg().argName("data-dir") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("format") //
				.desc("set the mock data file format ["
						+ Constants.DefaultFileFormat + "]") //
				.hasArg().argName("format") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("buffer-size") //
				.desc("set the history buffer size ["
						+ Constants.DefaultHistoryBufferSize + "]") //
				.hasArg().argName("size") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("listen-port") //
				.desc("set the listen port number ["
						+ Constants.DefaultListenPort + "]") //
				.hasArg().argName("port") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("remote-host") //
				.desc("set the remote server hostname or ip address ["
						+ Constants.DefaultRemoteHost + "]") //
				.hasArg().argName("host") //
				.build());
		options.addOption(Option.builder() //
				.longOpt("remote-port") //
				.desc("set the remote server port number ["
						+ Constants.DefaultRemotePort + "]") //
				.hasArg().argName("port") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("extensions") //
				.desc("set the extension class names") //
				.hasArgs().argName("classes") //
				.build());

		options.addOption(Option.builder("h") //
				.longOpt("help") //
				.desc("display this help") //
				.build());

		options.addOption(Option.builder() //
				.longOpt("verbose") //
				.desc("enables extra verbose output") //
				.build());

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("mode")) {
				ExecutionType type = ExecutionType
						.valueOf(line.getOptionValue("mode"));
				switch (type) {
				case Simulate:
				case Record:
					executionType = type;
					break;
				default:
					executionType = ExecutionType.Help;
					break;
				}
			}

			if (line.hasOption("data")) {
				dataDir = line.getOptionValue("data");
			}

			if (line.hasOption("format")) {
				fileFormat = line.getOptionValue("format");
			}

			if (line.hasOption("buffer-size")) {
				historyBufferSize = Integer
						.parseInt(line.getOptionValue("buffer-size"));
			}

			if (line.hasOption("listen-port")) {
				listenPort = Integer
						.parseInt(line.getOptionValue("listen-port"));
			}

			if (line.hasOption("remote-host")) {
				remoteHost = line.getOptionValue("remote-host");
			}
			if (line.hasOption("remote-port")) {
				remotePort = Integer
						.parseInt(line.getOptionValue("remote-port"));
			}

			if (line.hasOption("extensions")) {
				extensionClasses = line.getOptionValues("extensions");

				extensions.putAll(
						ExtensionLoader.loadExtension(extensionClasses));

			} else {
				extensionClasses = new String[] {
						DateMaskCompareTransformer.class.getName() };
				extensions.putAll(
						ExtensionLoader.loadExtension(extensionClasses));

			}

			if (line.hasOption("help")) {
				executionType = ExecutionType.Help;
			}
			if (line.hasOption("verbose")) {
				isVerbose = true;
			}

		} catch (ParseException e) {
			executionType = ExecutionType.Help;
			footer = e.getMessage();
		}

		if (executionType == ExecutionType.Help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					SushMockServer.class.getSimpleName() + " [options]", header,
					options, "\n" + footer);
		}
	}

	public ExecutionType getExecutionType() {
		return executionType;
	}

	public String getDataDir() {
		return dataDir;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public int getHistoryBufferSize() {
		return historyBufferSize;
	}

	public int getListenPort() {
		return listenPort;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	@SuppressWarnings("unchecked")
	public <T extends Extension> Map<String, T> getExtensionsOfType(
			final Class<T> extensionType) {
		return (Map<String, T>) Maps.filterEntries(extensions,
				new Predicate<Map.Entry<String, Extension>>() {
					public boolean apply(Map.Entry<String, Extension> input) {
						return extensionType
								.isAssignableFrom(input.getValue().getClass());
					}
				});
	}

	public boolean isVerbose() {
		return isVerbose;
	}

}
