// พาธ: com.kku.emergencystaff/common/utils/DateUtils.kt
package com.example.sosstaff.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val thaiLocale = Locale("th", "TH")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", thaiLocale)
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", thaiLocale)
    private val timeFormat = SimpleDateFormat("HH:mm", thaiLocale)

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    // Add overload for Date type
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    // Add overload for long type without conversion
    fun formatDate(timestamp: Long?): String {
        return if (timestamp != null) dateFormat.format(Date(timestamp)) else ""
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    // Add overload for Date type
    fun formatDateTime(date: Date?): String {
        return if (date != null) dateTimeFormat.format(date) else ""
    }

    // Add overload for Long type without conversion
    fun formatDateTime(timestamp: Long?): String {
        return if (timestamp != null) dateTimeFormat.format(Date(timestamp)) else ""
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    // Add overload for Date type
    fun formatTime(date: Date?): String {
        return if (date != null) timeFormat.format(date) else ""
    }

    // Add overload for Long type without conversion
    fun formatTime(timestamp: Long?): String {
        return if (timestamp != null) timeFormat.format(Date(timestamp)) else ""
    }

    fun getRelativeTimeSpan(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "เมื่อสักครู่"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} นาทีที่แล้ว"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} ชั่วโมงที่แล้ว"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} วันที่แล้ว"
            else -> formatDate(timestamp)
        }
    }

    fun isToday(timestamp: Long): Boolean {
        val timestampCal = Calendar.getInstance()
        timestampCal.timeInMillis = timestamp

        val todayCal = Calendar.getInstance()

        return timestampCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                timestampCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(timestamp: Long): Boolean {
        val timestampCal = Calendar.getInstance()
        timestampCal.timeInMillis = timestamp

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)

        return timestampCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                timestampCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDateForHeader(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "วันนี้"
            isYesterday(timestamp) -> "เมื่อวาน"
            else -> formatDate(timestamp)
        }
    }

    fun calculateDuration(startTime: Long, endTime: Long): String {
        val durationMillis = endTime - startTime
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

        return when {
            hours > 0 -> "$hours ชั่วโมง $minutes นาที"
            else -> "$minutes นาที"
        }
    }

    // Add overload for Date parameters
    fun calculateDuration(startDate: Date, endDate: Date?): String {
        if (endDate == null) return ""

        val startTime = startDate.time
        val endTime = endDate.time

        return calculateDuration(startTime, endTime)
    }

    // Add overload for long parameters where nulls are possible
    fun calculateDuration(startTime: Long?, endTime: Long?): String {
        if (startTime == null || endTime == null) return ""

        return calculateDuration(startTime, endTime)
    }
}