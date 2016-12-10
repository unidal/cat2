package com.dianping.cat.message.internal;

import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.*;

public enum NullMessage implements Transaction, Event, Metric, Trace, Heartbeat, ForkedTransaction, TaggedTransaction {
	TRANSACTION,

	EVENT,

	METRIC,

	TRACE,

	HEARTBEAT;

	private static String DEFAULT = "";

	@Override
	public Transaction addChild(Message message) {
		return this;
	}

	@Override
	public void addData(String keyValuePairs) {
	}

	@Override
	public void addData(String key, Object value) {
	}

	@Override
	public void bind(String tag, String childMessageId, String title) {
	}

	@Override
	public void complete() {
	}

	@Override
	public void fork() {
	}

	@Override
	public List<Message> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public Object getData() {
		return DEFAULT;
	}

	@Override
	public long getDurationInMicros() {
		return 0;
	}

	@Override
	public long getDurationInMillis() {
		return 0;
	}

	@Override
	public String getForkedMessageId() {
		return DEFAULT;
	}

	@Override
	public String getName() {
		return DEFAULT;
	}

	public String getParentMessageId() {
		return DEFAULT;
	}

	public String getRootMessageId() {
		return DEFAULT;
	}

	@Override
	public String getStatus() {
		return DEFAULT;
	}

	@Override
	public String getTag() {
		return DEFAULT;
	}

	@Override
	public long getTimestamp() {
		return 0;
	}

	@Override
	public String getType() {
		return DEFAULT;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public boolean isStandalone() {
		return true;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public void setDurationInMillis(long duration) {
	}

	@Override
	public void setStatus(String status) {
	}

	@Override
	public void setStatus(Throwable e) {
	}

	@Override
	public void setSuccessStatus() {
	}

	@Override
	public void start() {
	}

}
