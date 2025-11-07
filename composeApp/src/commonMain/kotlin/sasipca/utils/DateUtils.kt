package sasipca.utils

import kotlinx.datetime.DayOfWeek
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime



/**
 * Retorna data atual no formato
 * Terça-Feira, 28 Out. 2025
 */
@OptIn(ExperimentalTime::class)
fun getFormattedDatePt(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${convertWeekdayPt(today.dayOfWeek)}, ${today.day} ${convertMonthPt(today.month.number,true)}. ${today.year}"
}

/**
 * Retorna mês atual, em português
 * Terça-Feira, 28 Out. 2025
 */
@OptIn(ExperimentalTime::class)
fun getCurrentMonthPt(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return convertMonthPt(today.month.number)
}

/**
 * Retorna uma saudação com base na hora atual do sistema.
 * - 06:00 - 12:00 => "Bom dia"
 * - 12:00 - 19:00 => "Boa tarde"
 * - 19:00 - 06:00 => "Boa noite"
 */
@OptIn(ExperimentalTime::class)
fun getGreetingPt(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

    return when (hour) {
        in 6..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
}

/**
 * Retorna apenas o mês atual em português.
 * @param month número do mês (1-12)
 * @param short se true, retorna os 3 primeiros caracteres do mês
 */
fun convertMonthPt(month: Int, short: Boolean = false): String {
    val monthPt = when (month) {
        1 -> "Janeiro"
        2 -> "Fevereiro"
        3 -> "Março"
        4 -> "Abril"
        5 -> "Maio"
        6 -> "Junho"
        7 -> "Julho"
        8 -> "Agosto"
        9 -> "Setembro"
        10 -> "Outubro"
        11 -> "Novembro"
        12 -> "Dezembro"
        else -> ""
    }

    return if (short && monthPt.length >= 3) monthPt.substring(0, 3) else monthPt
}

/**
 * Retorna apenas o mês atual em português.
 * @param dayofWeek Enum dia da semana
 */
private fun convertWeekdayPt(dayofWeek: DayOfWeek): String {
    val dayofWeekPt = when (dayofWeek) {
        DayOfWeek.MONDAY -> "Segunda-Feira"
        DayOfWeek.TUESDAY -> "Terça-Feira"
        DayOfWeek.WEDNESDAY -> "Quarta-Feira"
        DayOfWeek.THURSDAY -> "Quinta-Feira"
        DayOfWeek.FRIDAY -> "Sexta-Feira"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }

    return dayofWeekPt
}

