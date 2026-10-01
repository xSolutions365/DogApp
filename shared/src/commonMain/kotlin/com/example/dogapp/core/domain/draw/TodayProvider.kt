package com.example.dogapp.core.domain.draw

import kotlinx.coroutines.flow.Flow

interface TodayProvider {
    fun today(): DrawDay

    fun observeToday(): Flow<DrawDay>
}
