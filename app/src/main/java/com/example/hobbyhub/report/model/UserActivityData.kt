package com.example.hobbyhub.report.model

data class UserActivityData(
    val username: String,
    val createdAt: String,
    val appUsageTime: Long // in seconds
)
