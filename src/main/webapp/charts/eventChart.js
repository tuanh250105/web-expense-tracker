function loadEventSeries(eventId){
  const base = window.BB_CTX || '';
  fetch(`${base}/events/${eventId}/series`)
    .then(r=>r.json())
    .then(json=>{
      const ctx = document.getElementById('eventChart').getContext('2d');
      if (window.__eventChart) window.__eventChart.destroy();
      window.__eventChart = new Chart(ctx, {
        type:'line',
        data:{ labels: json.labels, datasets:[{ label:'Chi tiêu sự kiện', data: json.data, borderColor:'#ff9800', fill:false, tension:0.1 }]},
        options:{ responsive:true, scales:{ y:{ beginAtZero:true }}}
      });
    });
}


