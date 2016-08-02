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
      
      <c:if test="${not(withSearchBox eq 'false')}">
          <div class="navbar-header pull-left position" style="width:450px;padding-top:5px;">
         <form id="wrap_search" style="margin-bottom:0px;">
            <div class="input-group">
               <span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button"  id="switch">全部</button></span>
               <span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button"  id="frequent">常用</button></span>
               <span class="input-icon" style="width:300px;">
               <input id="search" type="text" value="${model.domain}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
               <i class="ace-icon fa fa-search nav-search-icon"></i>
               </span>
               <span class="input-group-btn">
                  <button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button> 
               </span>
            </div>
         </form>
          </div>
      </c:if>
      
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

<div>
   <div class="domainNavbar" style="display:none;font-size:small">
      <table border="1" rules="all">
         <c:forEach var="item" items="${model.domainGroups}">
            <tr>
               <c:set var="detail" value="${item.value}" />
               <td class="department" rowspan="${w:size(detail.projectLines)}">${item.key}</td>
               <c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
                     <c:if test="${index.index != 0}"> <tr></c:if>
                     <td class="department">${productline.key}</td>
                     <td>
                        <div class="domain">
                           <c:forEach var="domain" items="${productline.value.lineDomains}">&nbsp;
                              <a class='domainItem' href="?${ctx.query.domain[domain]}" class="${model.domain eq domain ? 'current' : ''}">[&nbsp;${domain}&nbsp;]</a>
                           </c:forEach>
                        </div>
                     </td>
                     <c:if test="${index.index != 0}"></tr></c:if>
               </c:forEach>
            </tr>
         </c:forEach>
      </table>
   </div>
   <div class="frequentNavbar" style="display:none;font-size:small">
      <table class="table table-striped table-hover table-bordered table-condensed" border="1" rules="all">
         <tr>
            <td class="domain" style="word-break:break-all" id="frequentNavbar"></td>
         <tr>
      </table>
   </div>

   <script type="text/javascript">
   $(document).ready(function() {
      var ct = getCookie("ct");
      if (ct != "") {
         var length = ct.length;
         var realName = ct.split("|");
         var temp = realName[0];
         
         if(temp.charAt(0)=='"'){
            temp =temp.substring(1,temp.length);
         }
         var name = decodeURI(temp);
         var loginInfo=document.getElementById('loginInfo');
         loginInfo.innerHTML ='欢迎，'+name;
      } else{
         var loginInfo=document.getElementById('loginInfo');
         loginInfo.innerHTML ='<a href="${model.webapp}/s/login" data-toggle="modal">登录</a>';
      }
      var page = '${model.page.title}';
      $('#'+page+"_report").addClass("active open");
      
      //custom autocomplete (category selection)
      $.widget("custom.catcomplete", $.ui.autocomplete, {
         _renderMenu: function( ul, items ) {
            var that = this,
            currentCategory = "";
            $.each( items, function( index, item ) {
               if ( item.category != currentCategory ) {
                  ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
                  currentCategory = item.category;
               }
               that._renderItemData( ul, item );
            });
         }
      });
      
      var data = [];
      <c:forEach var="item" items="${model.domainGroups}">
         <c:set var="detail" value="${item.value}" />
         <c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
            <c:forEach var="domain" items="${productline.value.lineDomains}">
                  var item = {};
                  item['label'] = '${domain}';
                  item['category'] ='${productline.key}';
                  
                  data.push(item);
            </c:forEach>
         </c:forEach>
      </c:forEach>
      
      $( "#search" ).catcomplete({
         delay: 0,
         source: data
      });
   });
   </script>
</div>