package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.consumer.event.model.entity.*;
import org.unidal.lookup.annotation.Named;

import java.util.List;

@Named(type = EventReportHelper.class)
public class EventReportHelper {
    public void mergeMachine(Machine old, Machine other) {
        // nothing to do
    }

    public void mergeName(EventName dst, EventName src) {
        long totalCountSum = dst.getTotalCount() + src.getTotalCount();
        dst.setTotalCount(totalCountSum);
        dst.setFailCount(dst.getFailCount() + src.getFailCount());
        dst.setTps(dst.getTps() + src.getTps());

        if (dst.getTotalCount() > 0) {
            dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
        }

        if (dst.getSuccessMessageUrl() == null) {
            dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
        }

        if (dst.getFailMessageUrl() == null) {
            dst.setFailMessageUrl(src.getFailMessageUrl());
        }
    }

    public void mergeRanges(List<Range> dst, List<Range> src) {
        for (int i = 0; i < src.size(); i++) {
            Integer key = i;
            Range duration = src.get(i);
            Range oldRange = findOrCreateRange(dst, i);

            if (oldRange == null) {
                oldRange = new Range(duration.getValue());
                dst.set(key, oldRange);
            }

            oldRange.setCount(oldRange.getCount() + duration.getCount());
            oldRange.setFails(oldRange.getFails() + duration.getFails());
        }
    }

    public void mergeReport(EventReport dst, EventReport src) {
        dst.mergeAttributes(src);
        dst.getDomainNames().addAll(src.getDomainNames());
        dst.getIps().addAll(src.getIps());
    }

    public void mergeType(EventType dst, EventType src) {
        long totalCountSum = dst.getTotalCount() + src.getTotalCount();

        dst.setTotalCount(totalCountSum);
        dst.setFailCount(dst.getFailCount() + src.getFailCount());
        dst.setTps(dst.getTps() + src.getTps());

        if (dst.getTotalCount() > 0) {
            dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
        }

        if (dst.getSuccessMessageUrl() == null) {
            dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
        }

        if (dst.getFailMessageUrl() == null) {
            dst.setFailMessageUrl(src.getFailMessageUrl());
        }
    }

    private Range findOrCreateRange(List<Range> ranges, int min) {
        if (min > ranges.size() - 1) {
            synchronized (ranges) {
                if (min > ranges.size() - 1) {
                    for (int i = ranges.size(); i < 60; i++) {
                        ranges.add(new Range(i));
                    }
                }
            }
        }
        Range range = ranges.get(min);
        return range;
    }
}
