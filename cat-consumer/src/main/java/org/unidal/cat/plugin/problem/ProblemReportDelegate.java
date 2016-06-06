package org.unidal.cat.plugin.problem;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

@Named(type = ReportDelegate.class, value = ProblemConstants.NAME)
public class ProblemReportDelegate implements ReportDelegate<ProblemReport> {
    @Inject(ProblemConstants.NAME)
    private ReportAggregator<ProblemReport> m_aggregator;

    @Override
    public ProblemReport aggregate(ReportPeriod period, Collection<ProblemReport> reports) {
        return m_aggregator.aggregate(period, reports);
    }

    @Override
    public ProblemReport makeAll(ReportPeriod period, Collection<ProblemReport> reports) {
        return null;
    }

    @Override
    public String buildXml(ProblemReport report) {
        String xml = new DefaultXmlBuilder().buildXml(report);

        return xml;
    }

    @Override
    public String getName() {
        return ProblemConstants.NAME;
    }

    @Override
    public ProblemReport parseXml(String xml) {
        try {
            return DefaultSaxParser.parse(xml);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
        }
    }

    @Override
    public ProblemReport readStream(InputStream in) throws IOException {
        ProblemReport report = DefaultNativeParser.parse(in);

        if (report.getDomain() == null) {
            return null;
        } else {
            return report;
        }
    }

    @Override
    public void writeStream(OutputStream out, ProblemReport report) throws IOException {
        DefaultNativeBuilder.build(report, out);
    }

    @Override
    public ProblemReport createLocal(ReportPeriod period, String domain, Date startTime) {
        return new ProblemReport(domain).setPeriod(period).setStartTime(startTime);
    }
}
