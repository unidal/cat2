package org.unidal.cat.plugin.transaction;


import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.transaction.filter.TransactionHolder;
import org.unidal.cat.plugin.transaction.filter.TransactionReportHelper;
import org.unidal.lookup.annotation.Named;

@Named(type = TransactionAllReportMaker.class)
public class TransactionAllReportMaker extends BaseVisitor {

    private TransactionHolder m_holder = new TransactionHolder();

    private String m_currentBu;

    private String m_currentType;

    private String m_currentDomain;

    private ProjectService m_projectService;

    private TransactionReportHelper m_helper;

    public TransactionAllReportMaker(TransactionReport report, ProjectService service, TransactionReportHelper helper) {
        m_holder.setReport(report);
        m_projectService = service;
        m_helper = helper;
    }

    public TransactionReport getReport() {
        return m_holder.getReport();
    }

    @Override
    public void visitTransactionReport(TransactionReport transactionReport) {
        m_currentDomain = transactionReport.getDomain();
        m_currentBu = m_projectService.findBu(transactionReport.getDomain());
        m_holder.getReport().addIp(m_currentBu);
        super.visitTransactionReport(transactionReport);
    }

    @Override
    public void visitType(TransactionType type) {
        m_currentType = type.getId();

        if (validateType(m_currentType)) {
            TransactionReport report = m_holder.getReport();
            Machine machine = report.findOrCreateMachine(m_currentBu);
            TransactionType result = machine.findOrCreateType(m_currentType);

            m_holder.setType(result);
            m_helper.mergeType(result, type);

            DomainStat domainStat = report.findOrCreateDistributionInType(m_currentType)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainStat(m_currentDomain);

            updateDomainStat(domainStat, type);

            super.visitType(type);
        }
    }

    @Override
    public void visitName(TransactionName name) {
        String nameId = name.getId();

        if (validateName(m_currentType, nameId)) {
            TransactionType trType = m_holder.getType();
            TransactionName trName = trType.findOrCreateName(nameId);

            m_helper.mergeName(trName, name);
            m_helper.mergeDurations(trName.getDurations(), name.getDurations());
            m_helper.mergeRanges(trName.getRanges(), name.getRanges());

            TransactionReport report = m_holder.getReport();
            DomainStat DomainStat = report.findOrCreateDistributionInType(trType.getId())
                    .findOrCreateDistributionInName(nameId)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainStat(m_currentDomain);
            updateDomainStat(DomainStat, name);
        }
    }


    private boolean validateName(String type, String name) {
        // TODO make it configurable
        return "Service".equals(type);
    }

    private boolean validateType(String type) {
        // TODO make it configurable
        return "Service".equals(type);
    }

    private void updateDomainStat(DomainStat domainStat, TransactionType type) {
        domainStat.setTotalCount(domainStat.getTotalCount() + type.getTotalCount());
        domainStat.setFailCount(domainStat.getFailCount() + type.getFailCount());

        if (type.getMin() < domainStat.getMin()) {
            domainStat.setMin(type.getMin());
        }
        if (type.getMax() > domainStat.getMax()) {
            domainStat.setMax(type.getMax());
        }
        domainStat.setSum(domainStat.getSum() + type.getSum());
        domainStat.setSum2(domainStat.getSum2() + type.getSum2());
        domainStat.setTps(domainStat.getTps() + type.getTps());
        if (domainStat.getTotalCount() > 0) {
            domainStat.setAvg(domainStat.getSum() / domainStat.getTotalCount());
        }
    }

    private void updateDomainStat(DomainStat DomainStat, TransactionName name) {
        DomainStat.setTotalCount(DomainStat.getTotalCount() + name.getTotalCount());
        DomainStat.setFailCount(DomainStat.getFailCount() + name.getFailCount());

        if (name.getMin() < DomainStat.getMin()) {
            DomainStat.setMin(name.getMin());
        }
        if (name.getMax() > DomainStat.getMax()) {
            DomainStat.setMax(name.getMax());
        }
        DomainStat.setSum(DomainStat.getSum() + name.getSum());
        DomainStat.setSum2(DomainStat.getSum2() + name.getSum2());
        DomainStat.setTps(DomainStat.getTps() + name.getTps());
        if (DomainStat.getTotalCount() > 0) {
            DomainStat.setAvg(DomainStat.getSum() / DomainStat.getTotalCount());
        }
    }
}
