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
        String typeName = type.getId();

        if (validateType(typeName)) {
            TransactionReport report = m_holder.getReport();
            Machine machine = report.findOrCreateMachine(m_currentBu);
            TransactionType result = machine.findOrCreateType(typeName);

            m_holder.setType(result);
            m_helper.mergeType(result, type);

            DomainCount domainCount = report.findOrCreateTypeDomain(typeName)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainCount(m_currentDomain);

            updateDomainCount(domainCount, type);

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
            DomainCount domainCount = report.findOrCreateTypeDomain(trType.getId()).findOrCreateNameDomain(nameId)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainCount(m_currentDomain);
            updateDomainCount(domainCount, name);
        }
    }


    private boolean validateName(String type, String name) {
        return "Service".equals(type);
    }

    private boolean validateType(String type) {
        return "Service".equals(type);
    }

    private void updateDomainCount(DomainCount domainCount, TransactionType type) {
        domainCount.setTotalCount(domainCount.getTotalCount() + type.getTotalCount());
        domainCount.setFailCount(domainCount.getFailCount() + type.getFailCount());

        if (type.getMin() < domainCount.getMin()) {
            domainCount.setMin(type.getMin());
        }
        if (type.getMax() > domainCount.getMax()) {
            domainCount.setMax(type.getMax());
        }
        domainCount.setSum(domainCount.getSum() + type.getSum());
        domainCount.setSum2(domainCount.getSum2() + type.getSum2());
        domainCount.setTps(domainCount.getTps() + type.getTps());
        if (domainCount.getTotalCount() > 0) {
            domainCount.setAvg(domainCount.getSum() / domainCount.getTotalCount());
        }
    }

    private void updateDomainCount(DomainCount domainCount, TransactionName name) {
        domainCount.setTotalCount(domainCount.getTotalCount() + name.getTotalCount());
        domainCount.setFailCount(domainCount.getFailCount() + name.getFailCount());

        if (name.getMin() < domainCount.getMin()) {
            domainCount.setMin(name.getMin());
        }
        if (name.getMax() > domainCount.getMax()) {
            domainCount.setMax(name.getMax());
        }
        domainCount.setSum(domainCount.getSum() + name.getSum());
        domainCount.setSum2(domainCount.getSum2() + name.getSum2());
        domainCount.setTps(domainCount.getTps() + name.getTps());
        if (domainCount.getTotalCount() > 0) {
            domainCount.setAvg(domainCount.getSum() / domainCount.getTotalCount());
        }
    }
}
