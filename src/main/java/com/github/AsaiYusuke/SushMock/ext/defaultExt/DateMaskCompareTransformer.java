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
