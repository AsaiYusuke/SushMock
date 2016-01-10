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
package com.github.AsaiYusuke.SushMock.ext.defaultExt;

import java.io.UnsupportedEncodingException;

import com.github.AsaiYusuke.SushMock.ext.CompareTransformer;
import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public class DateMaskCompareTransformer extends CompareTransformer {

	private static final String weekNames = "(Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
	private static final String monthNames = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
	private static final String encoding = "UTF-8";
	private static final String replaceStr = "@@DATE@@";
	private static String regex;

	@Override
	public String name() {
		return this.getClass().getSimpleName();
	}

	@Override
	public byte[] transform(byte[] stream, StreamType type) {
		if (regex == null) {
			regex = String.format("%s +%s +\\d+ +\\d+:\\d+:\\d+ +\\d+",
					weekNames, monthNames);
		}

		try {
			String trans = new String(stream, encoding);

			trans = trans.replaceAll(regex, replaceStr);

			return trans.getBytes(encoding);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return stream;
	}

}
