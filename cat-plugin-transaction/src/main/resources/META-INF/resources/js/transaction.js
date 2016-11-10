function hourlyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = '';
	cell.innerHTML = response;

	var data = $('#responseTrendMeta', cell).text();
	if (data) {
	   graphLineChart($('#responseTrend', cell)[0], eval('(' + data + ')'));
	}

	data = $('#hitTrendMeta', cell).text();
	if (data) {
	   graphLineChart($('#hitTrend', cell)[0], eval('(' + data + ')'));
	}

	data = $('#errorTrendMeta', cell).text();
	if (data) {
	   graphLineChart($('#errorTrend', cell)[0], eval('(' + data + ')'));
	}
	
	data = $('#distributionChartMeta', cell).text();
	if (data) {
	   graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
}

function selectByName(date, domain, ip, type) {
	var query = $("#query").val();
	window.location.href = "?domain=" + domain + "&type=" + type + "&date=" + date + "&query=" + query + "&ip=" + ip;
}

function selectGroupByName(date, domain, ip, type) {
	var query = $("#query").val();
	window.location.href = "?op=groupReport&domain=" + domain + "&type=" + type + "&date=" + date + "&query=" + query + "&ip=" + ip;
}



