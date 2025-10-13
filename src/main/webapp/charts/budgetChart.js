function loadBudgetSeries(budgetId) {
  const base = window.BB_CTX || '';
  fetch(`${base}/budgets/${budgetId}`)
    .then(r => r.json())
    .then(json => {
      const ctx = document.getElementById('budgetChart').getContext('2d');
      if (window.__budgetChart) window.__budgetChart.destroy();
      window.__budgetChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: json.labels,
          datasets: [{
            label: 'Chi tiêu theo ngày',
            data: json.data,
            borderColor: '#4caf50',
            fill: false,
            tension: 0.1
          }]
        },
        options: {
          responsive: true,
          scales: { y: { beginAtZero: true } }
        }
      });
    });
}


