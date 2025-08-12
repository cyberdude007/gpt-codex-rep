package com.splitpaisa.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.splitpaisa.data.repo.CategorySlice
import com.splitpaisa.data.repo.TxFilter
import com.splitpaisa.data.repo.lastNMonthsBounds
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun StatsScreen(viewModel: StatsViewModel, onNavigate: (TxFilter) -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::setThisMonth, enabled = state.selection != RangeSelection.THIS_MONTH) { Text("This Month") }
                Button(onClick = viewModel::setLastSixMonths, enabled = state.selection != RangeSelection.LAST_SIX_MONTHS) { Text("Last 6") }
            }
        }
        item {
            Text("Spend by Category", style = MaterialTheme.typography.titleMedium)
            if (state.spendByCategory.isEmpty()) {
                Text("No expenses in this range.")
            } else {
                SpendByCategoryChart(state.spendByCategory) { cat ->
                    onNavigate(viewModel.filterForCategory(cat))
                }
            }
        }
        item {
            Text("Monthly Trend", style = MaterialTheme.typography.titleMedium)
            MonthlyTrendChart(state.monthlyTrend) { index ->
                val bounds = lastNMonthsBounds(state.monthlyTrend.size)
                val b = bounds[index]
                onNavigate(TxFilter(b.start, b.end))
            }
        }
        item {
            Text("Budget vs Actual", style = MaterialTheme.typography.titleMedium)
            if (state.budgetBars.isEmpty()) {
                Text("No budgets.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.budgetBars.forEach { bar ->
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(viewModel.filterForCategory(bar.categoryId)) }) {
                            Text(bar.name)
                            val progress = if (bar.budgetPaise == 0L) 0f else bar.actualPaise.toFloat() / bar.budgetPaise
                            Box(Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(progress.coerceAtMost(1f))
                                        .height(8.dp)
                                        .background(if (progress > 1f) Color.Red else Color.Green)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("Top Categories", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.topCategories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(viewModel.filterForCategory(cat.categoryId)) },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat.name)
                        Text(formatter.format(cat.spendPaise / 100.0))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendByCategoryChart(data: List<CategorySlice>, onSlice: (String?) -> Unit) {
    val total = data.sumOf { it.spendPaise }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var startAngle = -90f
            data.forEach { slice ->
                val sweep = if (total == 0L) 0f else (slice.spendPaise.toFloat() / total) * 360f
                drawArc(
                    color = Color(android.graphics.Color.parseColor(slice.color)),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                )
                startAngle += sweep
            }
        }
        Spacer(Modifier.height(8.dp))
        data.forEach { slice ->
            Row(
                modifier = Modifier
                    .clickable { onSlice(slice.categoryId) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(Color(android.graphics.Color.parseColor(slice.color)))
                )
                Text(slice.name, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun MonthlyTrendChart(points: List<com.splitpaisa.data.repo.MonthPoint>, onClick: (Int) -> Unit) {
    val maxVal = (points.maxOfOrNull { maxOf(it.spendPaise, it.incomePaise) } ?: 0).toFloat()
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            if (points.isEmpty() || maxVal == 0f) return@Canvas
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)
            var prevExpense: Offset? = null
            var prevIncome: Offset? = null
            points.forEachIndexed { index, p ->
                val x = stepX * index
                val spendY = height - (p.spendPaise / maxVal) * height
                val incomeY = height - (p.incomePaise / maxVal) * height
                val spendPt = Offset(x, spendY)
                val incomePt = Offset(x, incomeY)
                if (prevExpense != null) drawLine(Color.Red, prevExpense!!, spendPt)
                if (prevIncome != null) drawLine(Color.Green, prevIncome!!, incomePt)
                prevExpense = spendPt
                prevIncome = incomePt
            }
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            points.forEachIndexed { index, p ->
                Text(
                    "${p.month}/${p.year % 100}",
                    modifier = Modifier.clickable { onClick(index) },
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

