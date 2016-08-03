<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="sidebar" class="sidebar responsive">
   <script type="text/javascript">
      try{ace.settings.check('sidebar', 'fixed')}catch(e){}
   </script>
   
   <div class="sidebar-shortcuts" id="sidebar-shortcuts">
      <div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
         <c:forEach var="group" items="${ctx.menuGroups}">
            <a class="${group.backgroundStyleClasses}" id="tab_${group.id}" href="${model.webapp}${group.link}">
               <i class="ace-icon ${group.styleClasses}"></i>&nbsp;${group.title}
            </a>
         </c:forEach>
      </div>
      <div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
         <c:forEach var="group" items="${ctx.menuGroups}">
            <span class="${group.backgroundStyleClasses}"></span>
         </c:forEach>
      </div>
   </div>
   
   <ul class="nav nav-list" style="top: 0px;">
      <c:forEach var="menu" items="${ctx.menus}">
         <li id="${menu.id}" class="${menu.id eq model.id ? 'active open' : ''}">
            <a href="${menu.link}">
               <i class="menu-icon ${menu.styleClasses}"></i>
               <span class="menu-text">${menu.title}</span>
            </a>
         </li>
      </c:forEach>
   </ul>
   
   <!-- #section:basics/sidebar.layout.minimize -->
   <div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
      <i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
   </div>
   <!-- /section:basics/sidebar.layout.minimize -->

   <script type="text/javascript">
      try{ace.settings.check('sidebar' , 'collapsed')}catch(e){}
   </script>
</div>

