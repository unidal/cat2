package org.unidal.cat.core.config;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

public abstract class CoreConfigModel<P extends Page, A extends Action, M extends CoreConfigContext<?>> extends
      ViewModel<P, A, M> {
   private transient String m_id;

   public CoreConfigModel(String id, M ctx) {
      super(ctx);

      m_id = id;
   }

   public String getId() {
      return m_id;
   }

   public String getMenuId() {
      return "config";
   }
}
