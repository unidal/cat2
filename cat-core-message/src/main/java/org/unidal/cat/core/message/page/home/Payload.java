package org.unidal.cat.core.message.page.home;

import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.report.page.CoreReportPayload;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.message.internal.MessageId;

public class Payload extends CoreReportPayload<MessagePage, Action> {
   private MessagePage m_page;

   @FieldMeta("op")
   private Action m_action;

   @FieldMeta("header")
   private String m_header = "true";

   @FieldMeta("waterfall")
   private boolean m_waterfall;

   private String m_messageId;

   public Payload() {
      super(MessagePage.HOME);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   public String getMessageId() {
      return m_messageId;
   }

   public MessageId getId() {
      try {
         if (m_messageId != null) {
            MessageId id = MessageId.parse(m_messageId);

            return id;
         }
      } catch (Exception e) {
         // ignore it
      }

      return null;
   }

   @Override
   public MessagePage getPage() {
      return m_page;
   }

   /* used by message.jsp */
   public boolean isHeader() {
      return m_header == null || m_header.length() == 0 || m_header.equals("true");
   }

   public boolean isWaterfall() {
      return m_waterfall;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.DEFAULT);
   }

   @Override
   public void setPage(String page) {
      m_page = MessagePage.getByName(page, MessagePage.HOME);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      super.validate(ctx);

      if (m_action == null) {
         m_action = Action.DEFAULT;
      }

      m_messageId = ctx.getRequestContext().getAction();
   }
}
