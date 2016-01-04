package com.github.AsaiYusuke.SushMock.ext;

import com.github.AsaiYusuke.SushMock.util.Constants.StreamType;

public abstract class Extension {

	public abstract String name();

	public abstract byte[] transform(byte[] stream, StreamType type);

}
