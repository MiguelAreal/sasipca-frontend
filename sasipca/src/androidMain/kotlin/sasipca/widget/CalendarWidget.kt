package sasipca.widget

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.layout.Alignment
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sasipca.MainActivity
import sasipca.models.Delivery
import sasipca.models.DeliveryGet
import sasipca.network.ApiClient
import sasipca.storage.SessionManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.datastore.preferences.core.booleanPreferencesKey


class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}

class CalendarWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        SessionManager.init(com.russhwolf.settings.SharedPreferencesSettings(prefs))

        provideContent {
            val prefsGlance = currentState<Preferences>()

            val isLoggedIn = prefsGlance[booleanPreferencesKey("is_logged_in")] ?: false
            val isAdmin = prefsGlance[booleanPreferencesKey("is_admin")] ?: false

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(16.dp)
                ) {
                    if (!isLoggedIn) {
                        MessageView("Inicie sessão na app SasIPCA.", "🔐", false)
                    } else if (!isAdmin) {
                        MessageView("Acesso reservado a Admins.", "🚫", true)
                    } else {
                        CalendarContent()
                    }
                }
            }
        }
    }

    @Composable
    fun MessageView(message: String, iconText: String, isError: Boolean) {
        val textColor = if (isError) GlanceTheme.colors.error else GlanceTheme.colors.onSurface
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable(actionStartActivity(MainActivity::class.java)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = iconText, style = TextStyle(fontSize = 32.sp))
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = message,
                style = TextStyle(color = textColor, fontSize = 14.sp, textAlign = TextAlign.Center)
            )
        }
    }

    @Composable
    fun CalendarContent() {
        var currentMonth by remember { mutableStateOf(YearMonth.now()) }
        var deliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }

        val prefs = currentState<Preferences>()
        val lastUpdate = prefs[longPreferencesKey("last_update_timestamp")] ?: 0L

        LaunchedEffect(currentMonth, lastUpdate) {
            withContext(Dispatchers.IO) {
                try {
                    val start = currentMonth.atDay(1).toString()
                    val end = currentMonth.atEndOfMonth().toString()
                    val result = ApiClient.deliveryRepository.getDeliveries(
                        DeliveryGet(dateFrom = start, dateTo = end)
                    )
                    deliveries = result.filter { it.statusId == 1 }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Header(
                currentMonth = currentMonth,
                onPrevClick = { currentMonth = currentMonth.minusMonths(1) },
                onNextClick = { currentMonth = currentMonth.plusMonths(1) }
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            DaysOfWeekHeader()
            Spacer(modifier = GlanceModifier.height(4.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .clickable(
                        actionStartActivity(
                            activity = MainActivity::class.java,
                            parameters = actionParametersOf(ActionParameters.Key<Boolean>("OPEN_CALENDAR") to true)
                        )
                    )
            ) {
                CalendarGrid(yearMonth = currentMonth, deliveries = deliveries)
            }
        }
    }

    @Composable
    fun Header(currentMonth: YearMonth, onPrevClick: () -> Unit, onNextClick: () -> Unit) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = GlanceModifier.size(40.dp).clickable { onPrevClick() }, contentAlignment = Alignment.Center) {
                Text("‹", style = TextStyle(fontSize = 28.sp, color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold))
            }

            val monthName = currentMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.forLanguageTag("PT"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            Text(
                text = "$monthName ${currentMonth.year}",
                modifier = GlanceModifier.padding(horizontal = 8.dp),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
            )

            Box(modifier = GlanceModifier.size(40.dp).clickable { onNextClick() }, contentAlignment = Alignment.Center) {
                Text("›", style = TextStyle(fontSize = 28.sp, color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold))
            }
        }
    }

    @Composable
    fun DaysOfWeekHeader() {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            val days = (1..7).map { dayOfWeek ->
                DayOfWeek.of(dayOfWeek).getDisplayName(JavaTextStyle.NARROW, Locale.forLanguageTag("PT"))
            }
            days.forEach { day ->
                Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }

    @Composable
    fun CalendarGrid(yearMonth: YearMonth, deliveries: List<Delivery>) {
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
        val offset = firstDayOfWeek - 1
        val rows = 6

        Column(modifier = GlanceModifier.fillMaxSize()) {
            var dayCounter = 1
            for (row in 0 until rows) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until 7) {
                        if ((row == 0 && col < offset) || dayCounter > daysInMonth) {
                            Box(modifier = GlanceModifier.defaultWeight()) {}
                        } else {
                            val date = yearMonth.atDay(dayCounter)
                            val hasDelivery = deliveries.any {
                                try { LocalDate.parse(it.scheduledDate) == date } catch (_: Exception) { false }
                            }
                            val isToday = date == LocalDate.now()

                            DayCell(
                                day = dayCounter,
                                hasDelivery = hasDelivery,
                                isToday = isToday
                            )
                            dayCounter++
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RowScope.DayCell(day: Int, hasDelivery: Boolean, isToday: Boolean) {
        // Definimos as cores e estilos base
        val backgroundColor = when {
            isToday -> GlanceTheme.colors.primary
            hasDelivery -> GlanceTheme.colors.tertiary
            else -> null
        }

        val textColor = when {
            isToday -> GlanceTheme.colors.onPrimary
            hasDelivery -> GlanceTheme.colors.onTertiary
            else -> GlanceTheme.colors.onSurface
        }

        val fontWeight = if (isToday || hasDelivery) FontWeight.Bold else FontWeight.Normal

        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Se houver fundo, desenhamos o círculo
            backgroundColor?.let { color ->
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .background(color)
                        .cornerRadius(14.dp)
                ) {}
            }

            // O texto é desenhado por cima de tudo
            Text(
                text = day.toString(),
                style = TextStyle(
                    color = textColor,
                    fontWeight = fontWeight,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )

            if (isToday && hasDelivery) {
                Column(modifier = GlanceModifier.fillMaxHeight(), verticalAlignment = Alignment.Bottom) {
                    Box(
                        modifier = GlanceModifier
                            .size(4.dp)
                            .background(GlanceTheme.colors.onPrimary)
                            .cornerRadius(2.dp)
                    ) {}
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }
            }
        }
    }
}