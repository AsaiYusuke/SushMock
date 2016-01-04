package com.github.AsaiYusuke.SushMock.task;

import org.apache.sshd.client.channel.ClientChannel;

public abstract class AbstractShellSubTask {

	protected ClientChannel channel;

	protected boolean isActive;

	public AbstractShellSubTask() {
		isActive = true;
	}

	public void setChannel(ClientChannel channel) {
		this.channel = channel;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
