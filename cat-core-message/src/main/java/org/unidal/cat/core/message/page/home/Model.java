package org.unidal.cat.core.message.page.home;

import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.report.page.CoreReportModel;
import org.unidal.cat.spi.Report;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public class Model extends CoreReportModel<MessagePage, Action, Context> {
   private MessageId m_messageId;

   private MessageTree m_messageTree;

   public Model(Context ctx) {
      super("", ctx);
   }

   @Override
   public Action getDefaultAction() {
      return Action.VIEW;
   }

   @Override
   public String getDomain() {
      return null;
   }

   public MessageId getMessageId() {
      return m_messageId;
   }

   public MessageTree getMessageTree() {
      return m_messageTree;
   }

   @Override
   public Report getReport() {
      return null;
   }

   public void setMessageId(MessageId messageId) {
      m_messageId = messageId;
   }

   public void setMessageTree(MessageTree messageTree) {
      m_messageTree = messageTree;
   }
}
