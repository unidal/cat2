package com.dianping.cat.report.page.problem;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.task.HistoryProblemReportMerger;
import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

public class ProblemReportMergerTest {

	@Test
	public void testProblemReportMergeAll() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeAllResult.xml"),
		      "utf-8");
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), merger.getProblemReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is not changed!", newXml.replaceAll("\r", ""), reportNew.toString()
		      .replaceAll("\r", ""));
		Assert.assertEquals("Source report is not changed!", oldXml.replaceAll("\r", ""), reportOld.toString()
		      .replaceAll("\r", ""));
	}

	@Test
	public void testProblemReportMergerSize() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMobile.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		for (int i = 0; i < 24; i++) {
			reportOld.accept(merger);
		}
		ProblemReport problemReport = merger.getProblemReport();
		Assert.assertEquals(true, (double) problemReport.toString().length() / 1024 / 1024 < 1);
	}
}
