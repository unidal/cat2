package org.unidal.cat.plugin.transaction;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.transaction.filter.TransactionHolder;
import org.unidal.cat.plugin.transaction.filter.TransactionReportHelper;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = TransactionAllReportMaker.class)
public class TransactionAllReportMaker extends BaseVisitor {

    private TransactionHolder m_holder = new TransactionHolder();

    public String m_currentBu;

    public String m_currentType;

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
        }
    }


    private boolean validateName(String type, String name) {
        return true;
    }

    private boolean validateType(String type) {
        return true;
    }
}
