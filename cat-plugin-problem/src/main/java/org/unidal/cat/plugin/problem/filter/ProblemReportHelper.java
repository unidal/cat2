package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import org.unidal.lookup.annotation.Named;

import java.util.List;
import java.util.Map;

@Named(type = ProblemReportHelper.class)
public class ProblemReportHelper {
   private static final int SIZE = 60;

   public void mergeDurations(Map<Integer, Duration> dst, Map<Integer, Duration> src) {
      for (Map.Entry<Integer, Duration> e : src.entrySet()) {
         Integer value = e.getKey();
         Duration duration = e.getValue();
         Duration oldDuration = dst.get(value);

         if (oldDuration == null) {
            oldDuration = new Duration();
            oldDuration.setValue(duration.getValue());
            dst.put(value, oldDuration);
         }

         oldDuration.setCount(oldDuration.getCount() + duration.getCount());
         mergeList(oldDuration.getMessages(), duration.getMessages(), SIZE);
      }
   }

   public void mergeEntity(Entity dst, Entity src) {
      dst.mergeAttributes(src);
   }

   public void mergeJavaThread(JavaThread dst, JavaThread src) {
      dst.mergeAttributes(src);
   }

   private List<String> mergeList(List<String> oldMessages, List<String> newMessages, int size) {
      int originalSize = oldMessages.size();

      if (originalSize < size) {
         int remainingSize = size - originalSize;

         if (remainingSize >= newMessages.size()) {
            oldMessages.addAll(newMessages);
         } else {
            oldMessages.addAll(newMessages.subList(0, remainingSize));
         }
      }
      return oldMessages;
   }

   public void mergeMachine(Machine dst, Machine src) {
      // nothing to do
   }

   public void mergeReport(ProblemReport dst, ProblemReport src) {
      dst.mergeAttributes(src);
      dst.getDomainNames().addAll(src.getDomainNames());
      dst.getIps().addAll(src.getIps());
   }

   public void mergeSegments(Map<Integer, Segment> dst, Map<Integer, Segment> src) {
      for (Map.Entry<Integer, Segment> e : src.entrySet()) {
         Integer id = e.getKey();
         Segment segment = e.getValue();
         Segment oldSegment = dst.get(id);

         if (oldSegment == null) {
            oldSegment = new Segment();
            oldSegment.setId(segment.getId());
            dst.put(id, oldSegment);
         }

         oldSegment.setCount(oldSegment.getCount() + segment.getCount());
         mergeList(oldSegment.getMessages(), segment.getMessages(), SIZE);
      }
   }
}
