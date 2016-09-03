<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ attribute name="withSearchBox" %>

<!-- #section:basics/navbar.layout -->
<div id="navbar" class="navbar navbar-default">
   <script src="${model.webapp}/js/report-header.js"></script>
   
   <script type="text/javascript">
      try{ace.settings.check('navbar' , 'fixed')}catch(e){}
   </script>

   <div class="navbar-container" id="navbar-container">
      <!-- #section:basics/sidebar.mobile.toggle -->
      <button type="button" class="navbar-toggle menu-toggler pull-left" id="menu-toggler">
         <span class="sr-only">Toggle sidebar</span>
         <span class="icon-bar"></span>
         <span class="icon-bar"></span>
         <span class="icon-bar"></span>
      </button>
      <!-- /section:basics/sidebar.mobile.toggle -->

      <div class="navbar-header pull-left">
         <!-- #section:basics/navbar.layout.brand -->
         <a href="${model.webapp}/r/home"  class="navbar-brand">
            <span>CAT</span>
            <small style="font-size:65%">(Central Application Tracking)</small>
         </a>
      </div>
      
      <!-- #section:basics/navbar.dropdown -->
      <div class="navbar-buttons navbar-header pull-right" role="navigation">
      <ul class="nav ace-nav" style="height:auto;">
         <li class="light-blue">
            <a href="http://github.com/unidal/cat2" target="_blank">
               <i class="ace-icon glyphicon glyphicon-star"></i>
               <span>Stars</span>
            </a>
         </li>
         <li class="light-blue" >
            <a data-toggle="dropdown" href="#" class="dropdown-toggle">
               <span class="user-info">
                  <span id="loginInfo"></span>
               </span>
               <i class="ace-icon fa fa-caret-down"></i>
            </a>
            <ul class="user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close">
               <li>
                  <a href="${model.webapp}/s/login?op=logout" ><i class="ace-icon fa fa-power-off"></i>注销</a>
               </li>
            </ul>
         </li>
      </ul>
      </div> 
   </div>
</div>
