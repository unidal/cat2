package org.unidal.cat.plugin.transaction.page.transform;

import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor;
import org.unidal.cat.plugin.transaction.page.Model;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor.DistributionDetail;

@Named(type = AllReportDistributionBuilder.class)
public class AllReportDistributionBuilder {

    public void buildAllReportDistributionInfo(Model model, String type, String name, String ip, TransactionReport report) {
        List<DistributionDetailVisitor.DistributionDetail> distributionDetails = new ArrayList<DistributionDetailVisitor.DistributionDetail>();
        if (name == null || name.length() == 0) {
            if (report.getTypeDomains().size() > 0) {
                TypeDomain typeDomain = report.findTypeDomain(type);
                if (typeDomain != null) {
                    Bu bu = typeDomain.findBu(ip);
                    addDistributionDetail(distributionDetails, bu);
                }
            }
        } else {
            if (report.getTypeDomains().size() > 0) {
                TypeDomain typeDomain = report.findTypeDomain(type);
                if (typeDomain != null) {
                    NameDomain nameDomain = typeDomain.findNameDomain(name);
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

    private void addDistributionDetail(List<DistributionDetail> distributionDetails, Bu bu) {
        if (bu != null) {
            for (DomainCount domainCount : bu.getTypeDomainCounts().values()) {
                DistributionDetail distributionDetail = new DistributionDetail();
                distributionDetail.setIp(domainCount.getDomain());
                distributionDetail.setTotalCount(domainCount.getTotalCount());
                distributionDetail.setFailCount(domainCount.getFailCount());
                distributionDetail.setMin(domainCount.getMin());
                distributionDetail.setMax(domainCount.getMax());
                distributionDetail.setAvg(domainCount.getAvg());
                distributionDetail.setQps(domainCount.getTps());
                distributionDetails.add(distributionDetail);
            }
        }
    }
}
