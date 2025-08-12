package com.splitpaisa.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import com.splitpaisa.data.repo.CategorySlice
import com.splitpaisa.data.repo.TxFilter
import com.splitpaisa.data.repo.lastNMonthsBounds
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel, onNavigate: (TxFilter) -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
        }
    }
    val showStart = remember { mutableStateOf(false) }
    val showEnd = remember { mutableStateOf(false) }
    val tempStart = remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::setThisMonth, enabled = state.selection != RangeSelection.THIS_MONTH) { Text("This Month") }
                Button(onClick = viewModel::setLastSixMonths, enabled = state.selection != RangeSelection.LAST_SIX_MONTHS) { Text("Last 6") }
                Button(onClick = { showStart.value = true }, enabled = state.selection != RangeSelection.CUSTOM) { Text("Custom") }
            }
        }
        item {
            Text("Spend by Category", style = MaterialTheme.typography.titleMedium)
            if (state.spendByCategory.isEmpty()) {
                Text("No expenses in this range.")
            } else {
                SpendByCategoryChart(state.spendByCategory, formatter) { cat ->
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

    if (showStart.value) {
        val dpState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showStart.value = false }, confirmButton = {
            TextButton(onClick = {
                val sel = dpState.selectedDateMillis
                if (sel != null) {
                    tempStart.value = sel
                    showStart.value = false
                    showEnd.value = true
                }
            }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showStart.value = false }) { Text("Cancel") }
        }) {
            DatePicker(state = dpState)
        }
    }
    if (showEnd.value) {
        val dpState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showEnd.value = false }, confirmButton = {
            TextButton(onClick = {
                val sel = dpState.selectedDateMillis
                val start = tempStart.value
                if (sel != null && start != null) {
                    val endExclusive = sel + 24L * 60 * 60 * 1000
                    viewModel.setCustom(start, endExclusive)
                    showEnd.value = false
                }
            }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showEnd.value = false }) { Text("Cancel") }
        }) {
            DatePicker(state = dpState)
        }
    }
}

@Composable
private fun SpendByCategoryChart(data: List<CategorySlice>, formatter: NumberFormat, onSlice: (String?) -> Unit) {
    val total = data.sumOf { it.spendPaise }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            angle = (angle + 450f) % 360f
                            var start = 0f
                            data.forEach { slice ->
                                val sweep = if (total == 0L) 0f else (slice.spendPaise.toFloat() / total) * 360f
                                if (angle >= start && angle < start + sweep) {
                                    onSlice(slice.categoryId)
                                    return@detectTapGestures
                                }
                                start += sweep
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f)
                data.forEach { slice ->
                    val sweep = if (total == 0L) 0f else (slice.spendPaise.toFloat() / total) * 360f
                    drawArc(
                        color = Color(android.graphics.Color.parseColor(slice.color)),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = stroke,
                    )
                    startAngle += sweep
                }
            }
            Text(formatter.format(total / 100.0))
        }
        Spacer(Modifier.height(8.dp))
        data.forEach { slice ->
            val percent = if (total == 0L) 0 else (slice.spendPaise * 100 / total).toInt()
            Row(
                modifier = Modifier
                    .clickable { onSlice(slice.categoryId) }
                    .semantics { contentDescription = "${slice.name}, ${formatter.format(slice.spendPaise / 100.0)}, ${percent}%" }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(Color(android.graphics.Color.parseColor(slice.color)))
                )
                Text("${slice.name} (${percent}%)", modifier = Modifier.padding(start = 8.dp))
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

