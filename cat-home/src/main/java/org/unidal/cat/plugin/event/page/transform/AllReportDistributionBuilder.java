package org.unidal.cat.plugin.event.page.transform;

import com.dianping.cat.consumer.event.model.entity.*;
import com.dianping.cat.report.page.event.transform.DistributionDetailVisitor;
import org.unidal.cat.plugin.event.page.Model;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named(type = AllReportDistributionBuilder.class)
public class AllReportDistributionBuilder {

    public void buildAllReportDistributionInfo(Model model, String type, String name, String ip, EventReport report) {
        List<DistributionDetailVisitor.DistributionDetail> distributionDetails = new ArrayList<DistributionDetailVisitor.DistributionDetail>();
        if (name == null || name.length() == 0) {
            if (report.getDistributionInTypes().size() > 0) {
                DistributionInType distributionInType = report.findDistributionInType(type);
                if (distributionInType != null) {
                    Bu bu = distributionInType.findBu(ip);
                    addDistributionDetail(distributionDetails, bu);
                }
            }
        } else {
            if (report.getDistributionInTypes().size() > 0) {
                DistributionInType distributionInType = report.findDistributionInType(type);
                if (distributionInType != null) {
                    DistributionInName nameDomain = distributionInType.findDistributionInName(name);
                    if (nameDomain != null) {
                        Bu bu = nameDomain.findBu(ip);
                        addDistributionDetail(distributionDetails, bu);
                    }
                }
            }
        }

        Collections.sort(distributionDetails, new Comparator<DistributionDetailVisitor.DistributionDetail>() {
            @Override
            public int compare(DistributionDetailVisitor.DistributionDetail o1, DistributionDetailVisitor.DistributionDetail o2) {
                long gap = o2.getTotalCount() - o1.getTotalCount();

                if (gap > 0) {
                    return 1;
                } else if (gap < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        model.setDistributionDetails(distributionDetails);
    }

    private void addDistributionDetail(List<DistributionDetailVisitor.DistributionDetail> distributionDetails, Bu bu) {
        if (bu != null) {
            for (DomainStat domainStat : bu.getTypeDomainCounts().values()) {
                DistributionDetailVisitor.DistributionDetail distributionDetail = new DistributionDetailVisitor.DistributionDetail();
                distributionDetail.setIp(domainStat.getDomain());
                distributionDetail.setTotalCount(domainStat.getTotalCount());
                distributionDetail.setFailCount(domainStat.getFailCount());
                distributionDetails.add(distributionDetail);
            }
        }
    }
}
