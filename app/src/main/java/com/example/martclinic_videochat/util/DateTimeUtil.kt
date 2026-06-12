package com.example.martclinic_videochat.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object DateTimeUtil {

    private val KST_ZONE = ZoneId.of("Asia/Seoul")

    /**
     * Parses a timestamp string from PostgreSQL/Supabase and formats it into KST (Asia/Seoul) timezone.
     * Handles formats like "2026-06-12T02:17:46.123+00:00", "2026-06-12T02:17:46Z",
     * or space-separated "2026-06-12 02:17:46+09".
     */
    fun formatTimestampToKst(timestampStr: String?): String {
        if (timestampStr.isNullOrBlank()) return ""

        try {
            // Standardize format by replacing space with 'T' (e.g. "2026-06-12 02:17:46" -> "2026-06-12T02:17:46")
            var normalized = timestampStr.replace(" ", "T")
            
            // If it doesn't contain offset or timezone info, append 'Z' as a fallback assumption,
            // or if it ends with +00 or similar offsets.
            if (!normalized.contains("+") && !normalized.endsWith("Z") && normalized.count { it == '-' } == 2) {
                normalized += "Z"
            }

            val zonedDateTime = try {
                val instant = Instant.parse(normalized)
                instant.atZone(KST_ZONE)
            } catch (e: Exception) {
                try {
                    ZonedDateTime.parse(normalized).withZoneSameInstant(KST_ZONE)
                } catch (e2: Exception) {
                    val formatter = DateTimeFormatter.ISO_DATE_TIME
                    ZonedDateTime.parse(normalized, formatter).withZoneSameInstant(KST_ZONE)
                }
            }

            val displayFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
            return zonedDateTime.format(displayFormatter)
        } catch (e: Exception) {
            e.printStackTrace()
            return timestampStr // Fallback to raw string if parsing fails
        }
    }

    /**
     * Formats a naive ISO date string (e.g. "2026-06-12") to Korean format.
     */
    fun formatDateToKorean(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val localDate = LocalDate.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
            localDate.format(formatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Formats a naive ISO time string (e.g. "09:00:00") to Korean format.
     */
    fun formatTimeToKorean(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        return try {
            val localTime = LocalTime.parse(timeStr)
            val formatter = DateTimeFormatter.ofPattern("a h시 m분", Locale.KOREAN)
            localTime.format(formatter)
        } catch (e: Exception) {
            timeStr
        }
    }
}
