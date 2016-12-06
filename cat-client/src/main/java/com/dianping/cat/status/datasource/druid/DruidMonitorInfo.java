package com.dianping.cat.status.datasource.druid;

public class DruidMonitorInfo {

	/**
	 * jdbc connection
	 */
	private String m_jdbcUrl;

	/**
	 * 使用中在用的连接
	 */
	private int m_activeCount;

	/**
	 * 连接池中可用的连接
	 */
	private int m_poolingCount;

	/**
	 * 最大连接池个数
	 */
	private int m_maxActive;

	/**
	 * 允许的最大空闲连接个数
	 */
	private int m_maxIdle;

	/**
	 * 最大打开的PreparedStatements
	 */
	private int m_maxOpenPreparedStatements;

	/**
	 * 每个连接最大打开的PreparedStatements
	 */
	private int m_maxPoolPreparedStatementPerConnectionSize;

	/**
	 * 允许的最大等待个数
	 */
	private long m_maxWait;

	/**
	 * 允许的最大等待线程个数
	 */
	private int m_maxWaitThreadCount;

	/**
	 * 最小空闲个数
	 */
	private int m_minIdle;

	/**
	 * 查询超时时间
	 */
	private int m_queryTimeout;

	/**
	 * 平均每次获取连接等待的时间，实时计算出来，不需要字段保存 private int notEmptyAvgWaitMillis;
	 */

	/**
	 * 连接池非空时等待的线程的个数
	 */
	private int m_notEmptyWaitThreadCount;

	/**
	 * 所有的连接池等待的线程的个数
	 */
	private int m_waitThreadCount;

	/**
	 * 所有的连接池等待的次数，是累加值
	 */
	private long m_notEmptyWaitCount;

	/**
	 * 所有的连接池等待的毫秒数，是累加值
	 */
	private long m_notEmptyWaitMillis;

	/**
	 * 开启事务的个数
	 */
	private long m_startTransactionCount;

	/**
	 * 复用连接的个数
	 */
	private long m_recycleCount;

	/**
	 * 事务回滚的个数
	 */
	private long m_rollbackCount;

	/**
	 * 连接物理关闭的个数
	 */
	private long m_closeCount;

	/**
	 * 关闭PreparedStatement的个数
	 */
	private long m_closedPreparedStatementCount;

	/**
	 * 事务提交的个数
	 */
	private long m_commitCount;

	/**
	 * 获取连接请求的个数
	 */
	private long m_connectCount;

	/**
	 * 获取连接请求出错的个数
	 */
	private long m_connectErrorCount;

	/**
	 * 创建物理连接请求的个数
	 */
	private long m_createCount;

	/**
	 * 创建物理连接请求出错的个数
	 */
	private long m_createErrorCount;

	/**
	 * 销毁物理连接请求的个数
	 */
	private long m_destroyCount;

	/**
	 * 废弃物理连接请求的个数
	 */
	private long m_discardCount;

	/**
	 * 重复关闭连接请求的个数
	 */
	private long m_dupCloseCount;

	/**
	 * 出错的个数
	 */
	private long m_errorCount;

	/**
	 * 连接池初始大小的个数
	 */
	private long m_initialSize;

	/**
	 * 锁队列的长度
	 */
	private int m_lockQueueLength;

	/**
	 * 创建物理连接的耗时
	 */
	private long m_createTimespanMillis;

	/**
	 * 删除错误连接的个数
	 */
	private long m_removeAbandonedCount;

	public int getActiveCount() {
		return m_activeCount;
	}

	public long getAvgCreateTimespanMillis() {
		if (m_createCount > 0) {
			return m_createTimespanMillis / m_createCount;
		} else {
			return 0;
		}
	}

	public long getAvgNotEmptyWaitMillis() {
		if (m_notEmptyWaitCount > 0) {
			return m_notEmptyWaitMillis / m_notEmptyWaitCount;
		} else {
			return 0;
		}
	}

	public long getCloseCount() {
		return m_closeCount;
	}

	public long getClosedPreparedStatementCount() {
		return m_closedPreparedStatementCount;
	}

	public long getCommitCount() {
		return m_commitCount;
	}

	public long getConnectCount() {
		return m_connectCount;
	}

	public long getConnectErrorCount() {
		return m_connectErrorCount;
	}

	public long getCreateCount() {
		return m_createCount;
	}

	public long getCreateErrorCount() {
		return m_createErrorCount;
	}

	public long getCreateTimespanMillis() {
		return m_createTimespanMillis;
	}

	public long getDestroyCount() {
		return m_destroyCount;
	}

	public long getDiscardCount() {
		return m_discardCount;
	}

	public long getDupCloseCount() {
		return m_dupCloseCount;
	}

	public long getErrorCount() {
		return m_errorCount;
	}

	public long getInitialSize() {
		return m_initialSize;
	}

	public String getJdbcUrl() {
		return m_jdbcUrl;
	}

	public int getLockQueueLength() {
		return m_lockQueueLength;
	}

	public int getMaxActive() {
		return m_maxActive;
	}

	public int getMaxIdle() {
		return m_maxIdle;
	}

	public int getMaxOpenPreparedStatements() {
		return m_maxOpenPreparedStatements;
	}

	public int getMaxPoolPreparedStatementPerConnectionSize() {
		return m_maxPoolPreparedStatementPerConnectionSize;
	}

	public long getMaxWait() {
		return m_maxWait;
	}

	public int getMaxWaitThreadCount() {
		return m_maxWaitThreadCount;
	}

	public int getMinIdle() {
		return m_minIdle;
	}

	public long getNotEmptyWaitCount() {
		return m_notEmptyWaitCount;
	}

	public long getNotEmptyWaitMillis() {
		return m_notEmptyWaitMillis;
	}

	public int getNotEmptyWaitThreadCount() {
		return m_notEmptyWaitThreadCount;
	}

	public int getPoolingCount() {
		return m_poolingCount;
	}

	public int getQueryTimeout() {
		return m_queryTimeout;
	}

	public long getRecycleCount() {
		return m_recycleCount;
	}

	public long getRemoveAbandonedCount() {
		return m_removeAbandonedCount;
	}

	public long getRollbackCount() {
		return m_rollbackCount;
	}

	public long getStartTransactionCount() {
		return m_startTransactionCount;
	}

	public int getWaitThreadCount() {
		return m_waitThreadCount;
	}

	public void setActiveCount(int activeCount) {
		m_activeCount = activeCount;
	}

	public void setCloseCount(long closeCount) {
		m_closeCount = closeCount;
	}

	public void setClosedPreparedStatementCount(long closedPreparedStatementCount) {
		m_closedPreparedStatementCount = closedPreparedStatementCount;
	}

	public void setCommitCount(long commitCount) {
		m_commitCount = commitCount;
	}

	public void setConnectCount(long connectCount) {
		m_connectCount = connectCount;
	}

	public void setConnectErrorCount(long connectErrorCount) {
		m_connectErrorCount = connectErrorCount;
	}

	public void setCreateCount(long createCount) {
		m_createCount = createCount;
	}

	public void setCreateErrorCount(long createErrorCount) {
		m_createErrorCount = createErrorCount;
	}

	public void setCreateTimespanMillis(long createTimespanMillis) {
		m_createTimespanMillis = createTimespanMillis;
	}

	public void setDestroyCount(long destroyCount) {
		m_destroyCount = destroyCount;
	}

	public void setDiscardCount(long discardCount) {
		m_discardCount = discardCount;
	}

	public void setDupCloseCount(long dupCloseCount) {
		m_dupCloseCount = dupCloseCount;
	}

	public void setErrorCount(long errorCount) {
		m_errorCount = errorCount;
	}

	public void setInitialSize(long initialSize) {
		m_initialSize = initialSize;
	}

	public void setJdbcUrl(String jdbcUrl) {
		m_jdbcUrl = jdbcUrl;
	}

	public void setLockQueueLength(int lockQueueLength) {
		m_lockQueueLength = lockQueueLength;
	}

	public void setMaxActive(int maxActive) {
		m_maxActive = maxActive;
	}

	public void setMaxIdle(int maxIdle) {
		m_maxIdle = maxIdle;
	}

	public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
		m_maxOpenPreparedStatements = maxOpenPreparedStatements;
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		m_maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
	}

	public void setMaxWait(long maxWait) {
		m_maxWait = maxWait;
	}

	public void setMaxWaitThreadCount(int maxWaitThreadCount) {
		m_maxWaitThreadCount = maxWaitThreadCount;
	}

	public void setMinIdle(int minIdle) {
		m_minIdle = minIdle;
	}

	public void setNotEmptyWaitCount(long notEmptyWaitCount) {
		m_notEmptyWaitCount = notEmptyWaitCount;
	}

	public void setNotEmptyWaitMillis(long notEmptyWaitMillis) {
		m_notEmptyWaitMillis = notEmptyWaitMillis;
	}

	public void setNotEmptyWaitThreadCount(int notEmptyWaitThreadCount) {
		m_notEmptyWaitThreadCount = notEmptyWaitThreadCount;
	}

	public void setPoolingCount(int poolingCount) {
		m_poolingCount = poolingCount;
	}

	public void setQueryTimeout(int queryTimeout) {
		m_queryTimeout = queryTimeout;
	}

	public void setRecycleCount(long recycleCount) {
		m_recycleCount = recycleCount;
	}

	public void setRemoveAbandonedCount(long removeAbandonedCount) {
		m_removeAbandonedCount = removeAbandonedCount;
	}

	public void setRollbackCount(long rollbackCount) {
		m_rollbackCount = rollbackCount;
	}

	public void setStartTransactionCount(long startTransactionCount) {
		m_startTransactionCount = startTransactionCount;
	}

	public void setWaitThreadCount(int waitThreadCount) {
		m_waitThreadCount = waitThreadCount;
	}

	@Override
	public String toString() {
		return "DruidMonitorInfo [m_jdbcUrl=" + m_jdbcUrl + ", m_activeCount=" + m_activeCount + ", m_poolingCount="
		      + m_poolingCount + ", m_maxActive=" + m_maxActive + ", m_maxIdle=" + m_maxIdle
		      + ", m_maxOpenPreparedStatements=" + m_maxOpenPreparedStatements
		      + ", m_maxPoolPreparedStatementPerConnectionSize=" + m_maxPoolPreparedStatementPerConnectionSize
		      + ", m_maxWait=" + m_maxWait + ", m_maxWaitThreadCount=" + m_maxWaitThreadCount + ", m_minIdle="
		      + m_minIdle + ", m_queryTimeout=" + m_queryTimeout + ", m_notEmptyWaitThreadCount="
		      + m_notEmptyWaitThreadCount + ", m_waitThreadCount=" + m_waitThreadCount + ", m_notEmptyWaitCount="
		      + m_notEmptyWaitCount + ", m_notEmptyWaitMillis=" + m_notEmptyWaitMillis + ", m_startTransactionCount="
		      + m_startTransactionCount + ", m_recycleCount=" + m_recycleCount + ", m_rollbackCount=" + m_rollbackCount
		      + ", m_closeCount=" + m_closeCount + ", m_closedPreparedStatementCount=" + m_closedPreparedStatementCount
		      + ", m_commitCount=" + m_commitCount + ", m_connectCount=" + m_connectCount + ", m_connectErrorCount="
		      + m_connectErrorCount + ", m_createCount=" + m_createCount + ", m_createErrorCount=" + m_createErrorCount
		      + ", m_destroyCount=" + m_destroyCount + ", m_discardCount=" + m_discardCount + ", m_dupCloseCount="
		      + m_dupCloseCount + ", m_errorCount=" + m_errorCount + ", m_initialSize=" + m_initialSize
		      + ", m_lockQueueLength=" + m_lockQueueLength + ", m_createTimespanMillis=" + m_createTimespanMillis
		      + ", m_removeAbandonedCount=" + m_removeAbandonedCount + "]";
	}

}
