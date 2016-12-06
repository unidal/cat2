package com.dianping.cat.status.datasource.c3p0;

public class C3P0MonitorInfo {

	/**
	 * jdbc url
	 */
	private String m_jdbcUrl;

	/**
	 * 使用中的连接
	 */
	private int m_numBusyConnections;

	/**
	 * 全部连接数，包含idle连接
	 */
	private int m_numConnections;

	/**
	 * 添加空闲连接数
	 */
	private int m_numIdleConnections;

	/**
	 * 获取连接出错的个数
	 */
	private long m_numFailedCheckouts;

	/**
	 * 归还连接出错的个数
	 */
	private long m_numFailedCheckins;

	/**
	 * 测试连接出错的个数
	 */
	private long m_numFailedIdleTests;

	public String getJdbcUrl() {
		return m_jdbcUrl;
	}

	public int getNumBusyConnections() {
		return m_numBusyConnections;
	}

	public int getNumConnections() {
		return m_numConnections;
	}

	public long getNumFailedCheckins() {
		return m_numFailedCheckins;
	}

	public long getNumFailedCheckouts() {
		return m_numFailedCheckouts;
	}

	public long getNumFailedIdleTests() {
		return m_numFailedIdleTests;
	}

	public int getNumIdleConnections() {
		return m_numIdleConnections;
	}

	public void setJdbcUrl(String jdbcUrl) {
		m_jdbcUrl = jdbcUrl;
	}

	public void setNumBusyConnections(int numBusyConnections) {
		m_numBusyConnections = numBusyConnections;
	}

	public void setNumConnections(int numConnections) {
		m_numConnections = numConnections;
	}

	public void setNumFailedCheckins(long numFailedCheckins) {
		m_numFailedCheckins = numFailedCheckins;
	}

	public void setNumFailedCheckouts(long numFailedCheckouts) {
		m_numFailedCheckouts = numFailedCheckouts;
	}

	public void setNumFailedIdleTests(long numFailedIdleTests) {
		m_numFailedIdleTests = numFailedIdleTests;
	}

	public void setNumIdleConnections(int numIdleConnections) {
		m_numIdleConnections = numIdleConnections;
	}

	@Override
	public String toString() {
		return "C3P0MonitorInfo [m_jdbcUrl=" + m_jdbcUrl + ", m_numBusyConnections=" + m_numBusyConnections
		      + ", m_numConnections=" + m_numConnections + ", m_numIdleConnections=" + m_numIdleConnections
		      + ", m_numFailedCheckouts=" + m_numFailedCheckouts + ", m_numFailedCheckins=" + m_numFailedCheckins
		      + ", m_numFailedIdleTests=" + m_numFailedIdleTests + "]";
	}

}
