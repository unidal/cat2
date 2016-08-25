package org.unidal.cat.plugin.transaction.reducer;

import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.filter.TransactionHelper;
import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.lookup.annotation.Inject;

public abstract class AbstractTransactionReducer implements ReportReducer<TransactionReport> {
   @Inject
   private TransactionHelper m_helper;

   protected abstract int getRangeValue(TransactionReport report, Range range);

   @Override
   public String getReportName() {
      return TransactionConstants.NAME;
   }

   @Override
   public TransactionReport reduce(List<TransactionReport> reports) {
      TransactionReport r = new TransactionReport();

      if (!reports.isEmpty()) {
         TransactionReport first = reports.get(0);
         Merger merger = new Merger(r);

         r.setDomain(first.getDomain());

         for (TransactionReport report : reports) {
            report.accept(merger.setMapping(new ValueMapping(report)));
         }

         ReportPeriod period = getPeriod();

         r.setPeriod(period);
         r.setStartTime(period.getStartTime(first.getStartTime()));
      }

      return r;
   }

   protected class Merger extends DefaultMerger {
      private RangeMapping m_mapping;

      public Merger(TransactionReport report) {
         super(report);
      }

      @Override
      protected void mergeDuration(Duration old, Duration other) {
         m_helper.mergeDuration(old, other);
      }

      @Override
      protected void mergeMachine(Machine old, Machine other) {
         m_helper.mergeMachine(old, other);
      }

      @Override
      protected void mergeName(TransactionName old, TransactionName other) {
         m_helper.mergeName(old, other);
      }

      @Override
      protected void mergeRange(Range old, Range other) {
         m_helper.mergeRange(old, other);
      }

      @Override
      protected void mergeType(TransactionType old, TransactionType other) {
         m_helper.mergeType(old, other);
      }

      public Merger setMapping(RangeMapping mapping) {
         m_mapping = mapping;
         return this;
      }

      protected void visitNameChildren(TransactionName to, TransactionName from) {
         for (Range source : from.getRanges()) {
            int value = m_mapping.getValue(source);
            Range r = to.findOrCreateRange(value);

            getObjects().push(r);
            source.accept(this);
            getObjects().pop();
         }

         for (Duration source : from.getDurations().values()) {
            Duration target = to.findDuration(source.getValue());

            if (target == null) {
               target = new Duration(source.getValue());
               to.addDuration(target);
            }

            getObjects().push(target);
            source.accept(this);
            getObjects().pop();
         }
      }
   }

   protected static interface RangeMapping {
      public int getValue(Range range);
   }

   class ValueMapping implements RangeMapping {
      private TransactionReport m_report;

      public ValueMapping(TransactionReport report) {
         m_report = report;
      }

      @Override
      public int getValue(Range range) {
         return getRangeValue(m_report, range);
      }
   }
}
