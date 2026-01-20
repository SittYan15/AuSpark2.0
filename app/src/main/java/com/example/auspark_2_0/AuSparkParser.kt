package com.example.auspark_2_0

import java.util.regex.Pattern

class AuSparkParser {
    fun parse(rawText: String): StudentPrasedResult {
        // --- 1. Extract Profile Info ---

        // Find ID (7 digits usually appearing before "Student Id" or near the top)
        val idRegex = Regex("(\\d{7})")
        val idMatch = idRegex.find(rawText)
        val studentId = idMatch?.groupValues?.get(1) ?: "Unknown"

        // Find GPA (Number followed by G.P.A.)
        val gpaRegex = Regex("(\\d\\.\\d{2}) G\\.P\\.A\\.")
        val gpaMatch = gpaRegex.find(rawText)
        val gpa = gpaMatch?.groupValues?.get(1) ?: "0.00"

        // Find Credits (Number followed by Credit)
        val creditRegex = Regex("(\\d+) Credit")
        val creditMatch = creditRegex.find(rawText)
        val credits = creditMatch?.groupValues?.get(1) ?: "0"

        // Find Name & Major (Logic: Text between 'AU SPARK' and 'ENGINEERING')
        // This is a bit "fuzzy" because names vary, but the structure is usually:
        // "AU SPARK [NAME] [MAJOR] ENGINEERING..."
        val headerRegex = Regex("AU SPARK\\s+(.*?)\\s+(COMPUTER SCIENCE|INFORMATION TECHNOLOGY|ENGINEERING)")
        val headerMatch = headerRegex.find(rawText)
        val name = headerMatch?.groupValues?.get(1) ?: "Unknown"
        val major = headerMatch?.groupValues?.get(2) ?: "Unknown"

        val profile = StudentProfile(name, studentId, major, gpa, credits)

        // --- 2. Extract Schedule ---

        val scheduleList = mutableListOf<StudentSchedule>()
        var currentDay = "Unknown"

        // This Regex looks for EITHER a Day name OR a Class Entry
        // Group 1: Day Name (Monday|Tuesday...)
        // Group 2-9: Class Details (Time, Code, Name, Room...)
        val combinedRegex = Pattern.compile(
            "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)|" + // Group 1: Day
                    "(\\d{2}:\\d{2})\\s+(\\d{2}:\\d{2})\\s+" +       // Group 2,3: Start/End Time
                    "([A-Z]{2,4}\\d{4,5})\\s+\\((\\d+)\\)\\s+" +     // Group 4,5: Code, Section
                    "(.*?)\\s+" +                                    // Group 6: Course Name (Lazy match)
                    "([A-Z]+\\d{3,4})\\s+" +                         // Group 7: Room (e.g. VMES1004)
                    "(.*?)\\s+" +                                    // Group 8: Instructor
                    "\\[(.*?)\\]"                                    // Group 9: Campus
        )

        val matcher = combinedRegex.matcher(rawText)

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // We found a Day header (e.g., "Monday")
                currentDay = matcher.group(1)
            } else if (matcher.group(2) != null) {
                // We found a Class Entry
                val startTime = matcher.group(2)
                val endTime = matcher.group(3)
                val courseCode = matcher.group(4)
                val section = matcher.group(5)
                val courseName = matcher.group(6)
                val room = matcher.group(7)
                val instructor = matcher.group(8)
                val campus = matcher.group(9)

                scheduleList.add(
                    StudentSchedule(
                        day = currentDay,
                        startTime = startTime,
                        endTime = endTime,
                        courseCode = courseCode,
                        section = section,
                        courseName = courseName.trim(),
                        room = room,
                        instructor = instructor.trim(),
                        campus = campus,
                        ethicSeminar = false
                    )
                )
            }
        }

        return StudentPrasedResult(profile, scheduleList)
    }
}