package org.unidal.cat.message.storage.local;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = BucketManager.class, value = "local")
public class LocalBucketManager extends ContainerHolder implements BucketManager, LogEnabled {
   @Inject("local")
   private PathBuilder m_bulider;

   private ConcurrentMap<Integer, ConcurrentMap<String, Bucket>> m_buckets = new ConcurrentHashMap<Integer, ConcurrentMap<String, Bucket>>();

   protected Logger m_logger;

   private boolean bucketFilesExsits(String domain, String ip, int hour) {
      long timestamp = hour * 3600 * 1000L;
      Date startTime = new Date(timestamp);
      File dataPath = new File(m_bulider.getPath(domain, startTime, ip, FileType.DATA));
      File indexPath = new File(m_bulider.getPath(domain, startTime, ip, FileType.INDEX));

      return dataPath.exists() && indexPath.exists();
   }

   @Override
   public void closeBuckets(int hour) {
      Set<Integer> removed = new HashSet<Integer>();

      for (Entry<Integer, ConcurrentMap<String, Bucket>> e : m_buckets.entrySet()) {
         int h = e.getKey().intValue();

         if (h <= hour) {
            removed.add(h);
         }
      }

      for (Integer h : removed) {
         ConcurrentMap<String, Bucket> buckets = m_buckets.remove(h);

         for (Bucket bucket : buckets.values()) {
            try {
               bucket.close();
            } catch (Exception e) {
               Cat.logError(e);
            } finally {
               super.release(bucket);
            }
         }
      }
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   private ConcurrentMap<String, Bucket> findOrCreateMap(ConcurrentMap<Integer, ConcurrentMap<String, Bucket>> map,
         int hour) {
      ConcurrentMap<String, Bucket> m = map.get(hour);

      if (m == null) {
         synchronized (map) {
            m = map.get(hour);

            if (m == null) {
               m = new ConcurrentHashMap<String, Bucket>();
               map.putIfAbsent(hour, m);
            }
         }
      }

      return m;
   }

   @Override
   public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
      ConcurrentMap<String, Bucket> map = findOrCreateMap(m_buckets, hour);
      Bucket bucket = map.get(domain);
      boolean shouldCreate = (createIfNotExists && bucket == null)
            || (!createIfNotExists && bucketFilesExsits(domain, ip, hour));

      if (shouldCreate) {
         synchronized (map) {
            bucket = map.get(domain);

            if (bucket == null) {
               bucket = lookup(Bucket.class, "local");
               bucket.initialize(domain, ip, hour);
               map.putIfAbsent(domain, bucket);
            }
         }
      }

      return bucket;
   }
}
