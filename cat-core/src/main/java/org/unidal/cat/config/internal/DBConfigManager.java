package org.unidal.cat.config.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.helper.TimeHelper;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.cat.config.ConfigManager;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Named(type = ConfigManager.class)
public class DBConfigManager implements Initializable, ConfigManager {
    private static final long REFRESH_PERIOD = 5*TimeHelper.ONE_MINUTE;

    @Inject
    private ConfigDao m_configDao;

    private Map<String, Config> configMap = new ConcurrentHashMap<String, Config>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private void refreshConfig() {
        try {
            rwl.writeLock().lock();
            configMap.clear();
            List<Config> configList = m_configDao.findAllConfig(ConfigEntity.READSET_FULL);
            for (Config config : configList) {
                configMap.put(config.getName(), config);
            }
        } catch (DalException ex) {
            throw new RuntimeException(ex);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void initialize() {
        refreshConfig();
        Threads.forGroup("cat").start(new ConfigReloadTask());
    }

    @Override
    public String getConfigString(String key) {
        try {
            rwl.readLock().lock();
            Config config = configMap.get(key);
            return config == null ? null : config.getContent();
        } finally {
            rwl.readLock().unlock();
        }
    }

    public class ConfigReloadTask implements Threads.Task {

        @Override
        public String getName() {
            return "CAT2-Config-Reload";
        }

        @Override
        public void run() {
            boolean active = true;
            while (active) {
                try {
                    refreshConfig();
                } catch (Exception e) {
                    e.printStackTrace();
                    Cat.logError(e);
                }
                try {
                    Thread.sleep(REFRESH_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    active = false;
                }
            }
        }

        @Override
        public void shutdown() {
        }
    }
}
