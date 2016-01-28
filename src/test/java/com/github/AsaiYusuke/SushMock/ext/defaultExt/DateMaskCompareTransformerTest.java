package com.github.AsaiYusuke.SushMock.ext.defaultExt;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DateMaskCompareTransformerTest {

	@Test
	public void testTransform() {
		String inStr = "Mon Jan 28 22:37:10 123";

		try {
			byte[] inStream = inStr.getBytes("UTF-8");
			DateMaskCompareTransformer trans = new DateMaskCompareTransformer();
			byte[] outStream = trans.transform(inStream, StreamType.IN);
			String outStr = new String(outStream, "UTF-8");

			String replaceStr = Deencapsulation.getField(trans, "replaceStr");

			assertEquals(replaceStr, outStr);

		} catch (UnsupportedEncodingException e) {
		}

	}

}
