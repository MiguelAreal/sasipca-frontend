package sasipca.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import sasipca.models.ChartDataPoint
import sasipca.storage.ScreenSizeManager.isLargeScreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. GRÁFICO DE LINHAS INTERATIVO (Bezier + Tooltip)
// ==========================================
@Composable
fun InteractiveLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) { animationProgress.animateTo(1f, tween(1500)) }

    // Estado do Tooltip
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                // Suporte para Tap (Clique) e Drag (Arrastar o dedo)
                detectTapGestures(
                    onPress = { offset ->
                        selectedIndex = calculateIndexFromX(offset.x, size.width.toFloat(), data.size)
                    },
                    onTap = { selectedIndex = null } // Opcional: Desselecionar ao clicar
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { selectedIndex = null },
                    onDragCancel = { selectedIndex = null }
                ) { change, _ ->
                    selectedIndex = calculateIndexFromX(change.position.x, size.width.toFloat(), data.size)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxVal = (data.maxOfOrNull { it.value } ?: 1.0) * 1.2

            // Pontos calculados
            val points = data.mapIndexed { index, point ->
                val x = (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * width
                val y = height - ((point.value.toFloat() / maxVal.toFloat()) * height * animationProgress.value)
                Offset(x, y)
            }

            // 1. Desenhar Sombra (Fill)
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cp1 = Offset((p1.x + p2.x) / 2f, p1.y)
                        val cp2 = Offset((p1.x + p2.x) / 2f, p2.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.0f))
                )
            )

            // 2. Desenhar Linha Principal
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Indicador de Seleção (Crosshair vertical + Ponto)
            if (selectedIndex != null && selectedIndex in points.indices) {
                val point = points[selectedIndex!!]

                // Linha vertical tracejada
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Círculo de destaque
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                drawCircle(color = lineColor, radius = 6.dp.toPx(), center = point, style = Stroke(3.dp.toPx()))
            }
        }

        // 4. Renderizar o Tooltip (Overlay Composable)
        if (selectedIndex != null && selectedIndex in data.indices) {
            val dataPoint = data[selectedIndex!!]

            // Lógica de posicionamento para não sair do ecrã
            val relativeX = selectedIndex!!.toFloat() / (data.size - 1).coerceAtLeast(1)
            val alignment = when {
                relativeX < 0.2f -> Alignment.TopStart
                relativeX > 0.8f -> Alignment.TopEnd
                else -> Alignment.TopCenter
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp),
                contentAlignment = alignment
            ) {
                ChartTooltip(
                    title = dataPoint.label,
                    value = "${dataPoint.value.toInt()}",
                    subtitle = dataPoint.series
                )
            }
        }
    }
}

// Helper corrigido para aceitar Float no width
private fun calculateIndexFromX(touchX: Float, width: Float, listSize: Int): Int {
    if (listSize <= 1) return 0
    val step = width / (listSize - 1)
    return ((touchX + (step / 2)) / step).toInt().coerceIn(0, listSize - 1)
}

// ==========================================
// 2. GRÁFICO DE DONUT INTERATIVO
// ==========================================
@Composable
fun InteractiveDonutChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.inversePrimary
    )

    val total = data.sumOf { it.value }
    val safeTotal = if (total == 0.0) 1.0 else total
    val proportions = data.map { (it.value / safeTotal).toFloat() }

    // Pre-calcular ângulos para hit-testing
    val slices = remember(data) {
        val list = mutableListOf<Pair<Float, Float>>() // startAngle, sweepAngle
        var start = -90f
        proportions.forEach { p ->
            val sweep = p * 360f
            list.add(start to sweep)
            start += sweep
        }
        list
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) { animProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }

    val isLarge = isLargeScreen()

    if (isLarge) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            DonutCanvas(proportions, colors, selectedIndex, animProgress.value) { angle ->
                selectedIndex = getSliceIndexFromAngle(angle, slices)
            }
            Spacer(Modifier.width(32.dp))
            DonutLegend(data, colors, selectedIndex) { selectedIndex = it }
        }
    } else {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            DonutCanvas(proportions, colors, selectedIndex, animProgress.value) { angle ->
                selectedIndex = getSliceIndexFromAngle(angle, slices)
            }
            Spacer(Modifier.height(24.dp))
            DonutLegend(data, colors, selectedIndex) { selectedIndex = it }
        }
    }
}

// Helper Matemático para detetar clique no Donut
private fun getSliceIndexFromAngle(touchAngle: Float, slices: List<Pair<Float, Float>>): Int? {
    // Normalizar touchAngle para corresponder ao sistema de coordenadas do drawArc (-90 start)
    // O nosso cálculo de atan2 retorna -180 a 180.
    // O drawArc começa em -90 e vai somando.

    // Simplificação: Normalizar tudo para 0..360, onde 0 é o topo (-90 graus visual)
    var normalizedTouch = touchAngle + 90f
    if (normalizedTouch < 0) normalizedTouch += 360f

    // Verificar em que fatia cai
    var currentStart = 0f
    slices.forEachIndexed { index, pair ->
        val sweep = pair.second
        if (normalizedTouch >= currentStart && normalizedTouch <= currentStart + sweep) {
            return index
        }
        currentStart += sweep
    }
    return null
}

@Composable
private fun DonutCanvas(
    proportions: List<Float>,
    colors: List<Color>,
    selectedIndex: Int?,
    progress: Float,
    onAngleTouch: (Float) -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val centerX = size.width.toFloat() / 2
                        val centerY = size.height.toFloat() / 2

                        val dx = offset.x - centerX
                        val dy = offset.y - centerY

                        val angleRad = atan2(dy, dx)
                        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

                        onAngleTouch(angleDeg)
                    }
                }
        ) {
            val strokeWidth = 40.dp.toPx()
            var startAngle = -90f

            proportions.forEachIndexed { index, fraction ->
                val sweepAngle = fraction * 360f * progress
                val isSelected = index == selectedIndex

                // Destaque visual: Escala ligeiramente se selecionado
                val currentStroke = if (isSelected) strokeWidth * 1.3f else strokeWidth
                val color = colors[index % colors.size]

                // Diminuir opacidade dos não selecionados se houver um selecionado
                val finalColor = if (selectedIndex != null && !isSelected) color.copy(alpha = 0.3f) else color

                drawArc(
                    color = finalColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = currentStroke, cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }

        // Texto Central Dinâmico
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (selectedIndex != null) "${(proportions[selectedIndex] * 100).toInt()}%" else "Total",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DonutLegend(
    data: List<ChartDataPoint>,
    colors: List<Color>,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEachIndexed { index, point ->
            val isSelected = index == selectedIndex
            val color = colors[index % colors.size]

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(if (isSelected) null else index) }
                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        point.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${point.value.toInt()} uni.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. TOOLTIP COMPONENTE (Balão de Informação)
// ==========================================
@Composable
fun ChartTooltip(
    title: String,
    value: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (subtitle != null) {
            Text(subtitle, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

// ==========================================
// 4. BARRAS INTERATIVAS (Top Produtos)
// ==========================================
@Composable
fun InteractiveBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.value } ?: 1.0
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data.forEachIndexed { index, point ->
            val isSelected = selectedBarIndex == index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { selectedBarIndex = if (isSelected) null else index }
                        )
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nome do Produto
                    Text(
                        text = point.label,
                        modifier = Modifier.width(100.dp),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Barra
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp) // Barra mais alta para toque fácil
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val fraction = (point.value / maxVal).toFloat().coerceIn(0.01f, 1f)

                        // Fundo da barra
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )

                        // Barra Cheia
                        Box(
                            Modifier
                                .fillMaxHeight(0.8f) // Um pouco menor que o container
                                .fillMaxWidth(fraction)
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        )
                    }

                    // Valor sempre visível
                    Text(
                        text = point.value.toInt().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Tooltip Flutuante (Overlay)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-40).dp) // Move para cima da barra
                            .zIndex(1f)
                    ) {
                        ChartTooltip(
                            title = point.label,
                            value = "${point.value.toInt()} unidades"
                        )
                    }
                }
            }
        }
    }
}