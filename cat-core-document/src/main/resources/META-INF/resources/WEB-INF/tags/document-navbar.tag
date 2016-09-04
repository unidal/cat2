<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="sidebar" class="sidebar responsive">
	<script type="text/javascript">
		try {
			ace.settings.check('sidebar', 'fixed')
		} catch (e) {
		}
	</script>
	<div class="sidebar-shortcuts" id="sidebar-shortcuts">
		<div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
			<button class="btn btn-success" id="tab_realtime">
				<i class="ace-icon fa fa-signal"></i>&nbsp;&nbsp;实时
			</button>
			<button class="btn btn-grey" id="tab_offtime">
				<i class="ace-icon fa fa-film"></i>&nbsp;&nbsp;离线
			</button>
			<!-- #section:basics/sidebar.layout.shortcuts -->
			<button class="btn btn-warning" id="tab_document">
				<i class="ace-icon fa fa-users"></i>&nbsp;&nbsp;文档
			</button>
			<button class="btn btn-danger" id="tab_config">
				<i class="ace-icon fa fa-cogs"></i>&nbsp;&nbsp;配置
			</button>
		</div>
		
		<div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
			<span class="btn btn-success"></span>
            <span class="btn btn-info"></span>
            <span class="btn btn-warning"></span>
            <span class="btn btn-danger"></span>
		</div>

		<ul class="nav nav-list" style="top:0px;text-align:left;">
			<c:forEach var="doc" items="${model.documents}">
				<li id="${doc.id}Menu">
					<a href="${model.webapp}/doc?${ctx.query.id[doc.id]}">
					  <i class="menu-icon ${doc.styleClasses}"></i>
					  <span class="menu-text">${doc.title}</span>
					</a>
					<b class="arrow"></b>
				</li>
			</c:forEach>
		</ul>
		
		<!-- #section:basics/sidebar.layout.minimize -->
		<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
			<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left"
				data-icon2="ace-icon fa fa-angle-double-right"></i>
		</div>

		<!-- /section:basics/sidebar.layout.minimize -->
		<script type="text/javascript">
			try {
				ace.settings.check('sidebar', 'collapsed')
			} catch (e) {
			}

			var li = $('#${payload.document.id}Menu');

			if (li.size() == 0) {
				li = $('#indexMenu');
			}

			li.addClass('active');
			
            $(document).ready(function() {
               $("#tab_realtime").click(function(){
                  window.location.href = "${model.webapp}/r/t?${ctx.query}";
               });
               $("#tab_offtime").click(function(){
                  window.location.href = "${model.webapp}/r/statistics?${ctx.query}";
               });
               $("#tab_document").click(function(){
                  window.location.href = "${model.webapp}/doc?${ctx.query}";
               });
               $("#tab_config").click(function(){
                  window.location.href = "${model.webapp}/config?${ctx.query.op['projects']}";
               });
            });
		</script>
	</div>
</div>


