function hourlyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
	cell.innerHTML = response;

	data = $('#hitTrendMeta', cell).text();
	if (data) {
	   graphLineChart($('#hitTrend', cell)[0], eval('(' + data + ')'));
	}

	data = $('#errorTrendMeta', cell).text();
	if (data) {
	   graphLineChart($('#errorTrend', cell)[0], eval('(' + data + ')'));
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



