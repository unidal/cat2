package org.unidal.cat.plugin.event;


import com.dianping.cat.consumer.event.model.entity.*;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.event.filter.EventHolder;
import org.unidal.cat.plugin.event.filter.EventReportHelper;
import org.unidal.lookup.annotation.Named;

@Named(type = EventAllReportMaker.class)
public class EventAllReportMaker extends BaseVisitor {

    private EventHolder m_holder = new EventHolder();

    private String m_currentBu;

    private String m_currentType;

    private String m_currentDomain;

    private ProjectService m_projectService;

    private EventReportHelper m_helper;

    public EventAllReportMaker(EventReport report, ProjectService service, EventReportHelper helper) {
        m_holder.setReport(report);
        m_projectService = service;
        m_helper = helper;
    }

    public EventReport getReport() {
        return m_holder.getReport();
    }

    @Override
    public void visitEventReport(EventReport eventReport) {
        m_currentDomain = eventReport.getDomain();
        m_currentBu = m_projectService.findBu(eventReport.getDomain());
        m_holder.getReport().addIp(m_currentBu);
        super.visitEventReport(eventReport);
    }

    @Override
    public void visitType(EventType type) {
        m_currentType = type.getId();

        if (validateType(m_currentType)) {
            EventReport report = m_holder.getReport();
            Machine machine = report.findOrCreateMachine(m_currentBu);
            EventType result = machine.findOrCreateType(m_currentType);

            m_holder.setType(result);
            m_helper.mergeType(result, type);

            DomainStat domainStat = report.findOrCreateDistributionInType(m_currentType)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainStat(m_currentDomain);

            updateDomainStat(domainStat, type);

            super.visitType(type);
        }
    }

    @Override
    public void visitName(EventName name) {
        String nameId = name.getId();

        if (validateName(m_currentType, nameId)) {
            EventType eventType = m_holder.getType();
            EventName eventName = eventType.findOrCreateName(nameId);

            m_helper.mergeName(eventName, name);
            m_helper.mergeRanges(eventName.getRanges(), name.getRanges());

            EventReport report = m_holder.getReport();
            DomainStat DomainStat = report.findOrCreateDistributionInType(eventType.getId())
                    .findOrCreateDistributionInName(nameId)
                    .findOrCreateBu(m_currentBu).findOrCreateDomainStat(m_currentDomain);
            updateDomainStat(DomainStat, name);
        }
    }


    private boolean validateName(String type, String name) {
        // TODO make it configurable
        return "Service".equals(type) || "URL".equals(type);
    }

    private boolean validateType(String type) {
        // TODO make it configurable
        return "Service".equals(type) || "URL".equals(type);
    }

    private void updateDomainStat(DomainStat domainStat, EventType type) {
        domainStat.setTotalCount(domainStat.getTotalCount() + type.getTotalCount());
        domainStat.setFailCount(domainStat.getFailCount() + type.getFailCount());

        domainStat.setTps(domainStat.getTps() + type.getTps());
        if (domainStat.getTotalCount() > 0) {
            domainStat.setAvg(domainStat.getSum() / domainStat.getTotalCount());
        }
    }

    private void updateDomainStat(DomainStat DomainStat, EventName name) {
        DomainStat.setTotalCount(DomainStat.getTotalCount() + name.getTotalCount());
        DomainStat.setFailCount(DomainStat.getFailCount() + name.getFailCount());

        DomainStat.setTps(DomainStat.getTps() + name.getTps());
        if (DomainStat.getTotalCount() > 0) {
            DomainStat.setAvg(DomainStat.getSum() / DomainStat.getTotalCount());
        }
    }
}
