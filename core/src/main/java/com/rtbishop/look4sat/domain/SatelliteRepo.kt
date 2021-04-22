package com.rtbishop.look4sat.domain

import com.rtbishop.look4sat.domain.model.SatItem
import com.rtbishop.look4sat.domain.model.Transmitter
import com.rtbishop.look4sat.predict4kotlin.Satellite
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface SatelliteRepo {

    fun getSatItems(): Flow<List<SatItem>>

    fun getTransmittersForSat(catNum: Int): Flow<List<Transmitter>>

    suspend fun getSelectedSatellites(): List<Satellite>

    suspend fun importDataFromFile(stream: InputStream)

    suspend fun importDataFromWeb(sources: List<String>)

    suspend fun updateEntriesSelection(catNums: List<Int>, isSelected: Boolean)
}