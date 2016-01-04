package com.github.AsaiYusuke.SushMock.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;

public class DummyClientChannel implements ClientChannel {

	boolean isOpen;

	public DummyClientChannel() {
		isOpen = false;
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void close() throws IOException {
		this.isOpen = false;
	}

	@Override
	public Streaming getStreaming() {
		return null;
	}

	@Override
	public void setStreaming(Streaming paramStreaming) {
	}

	@Override
	public IoOutputStream getAsyncIn() {
		return null;
	}

	@Override
	public IoInputStream getAsyncOut() {
		return null;
	}

	@Override
	public IoInputStream getAsyncErr() {
		return null;
	}

	@Override
	public OutputStream getInvertedIn() {
		return null;
	}

	@Override
	public InputStream getInvertedOut() {
		return null;
	}

	@Override
	public InputStream getInvertedErr() {
		return null;
	}

	@Override
	public void setIn(InputStream paramInputStream) {
	}

	@Override
	public void setOut(OutputStream paramOutputStream) {
	}

	@Override
	public void setErr(OutputStream paramOutputStream) {
	}

	@Override
	public OpenFuture open() throws IOException {
		this.isOpen = true;
		return null;
	}

	@Override
	public int waitFor(int paramInt, long paramLong) {
		return 0;
	}

	@Override
	public CloseFuture close(boolean paramBoolean) {
		this.isOpen = false;
		return null;
	}

	@Override
	public Integer getExitStatus() {
		return null;
	}

}
