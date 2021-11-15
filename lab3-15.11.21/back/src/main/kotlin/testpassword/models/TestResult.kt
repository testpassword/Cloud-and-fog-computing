package testpassword.models

import kotlinx.serialization.Serializable

@Serializable data class TestResult(
    val bestIndex: String,
    val timeBefore: Double,
    val timeAfter: Double,
    val diff: Double
)