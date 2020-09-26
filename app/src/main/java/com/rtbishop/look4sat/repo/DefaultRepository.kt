/*
 * Look4Sat. Amateur radio and weather satellite tracker and passes predictor for Android.
 * Copyright (C) 2019, 2020 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.rtbishop.look4sat.repo

import android.content.ContentResolver
import android.net.Uri
import com.github.amsacode.predict4java.TLE
import com.rtbishop.look4sat.data.SatEntry
import com.rtbishop.look4sat.data.SatTrans
import com.rtbishop.look4sat.data.TleSource
import com.rtbishop.look4sat.repo.local.EntriesDao
import com.rtbishop.look4sat.repo.local.SourcesDao
import com.rtbishop.look4sat.repo.local.TransDao
import com.rtbishop.look4sat.repo.remote.TransApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import javax.inject.Inject

class DefaultRepository @Inject constructor(
    private val resolver: ContentResolver,
    private val client: OkHttpClient,
    private val api: TransApi,
    private val entriesDao: EntriesDao,
    private val sourcesDao: SourcesDao,
    private val transDao: TransDao
) : Repository {

    override suspend fun updateEntriesFromFile(tleUri: Uri) {
        withContext(Dispatchers.IO) {
            resolver.openInputStream(tleUri)?.use { stream ->
                val tleList = TLE.importSat(stream)
                val entries = tleList.map { SatEntry(it) }
                clearEntries()
                insertEntries(entries)
            }
        }
    }

    override suspend fun updateEntriesFromUrl(urlList: List<TleSource>) {
        withContext(Dispatchers.IO) {
            val streams = fetchTleStreams(urlList)
            val entries = mutableListOf<SatEntry>()
            streams.forEach {
                val list = TLE.importSat(it).map { tle -> SatEntry(tle) }
                entries.addAll(list)
            }
            clearEntries()
            insertEntries(entries)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun fetchTleStreams(urlList: List<TleSource>): List<InputStream> =
        withContext(Dispatchers.IO) {
            val streams = mutableListOf<InputStream>()
            urlList.withIndex().forEach {
                val request = Request.Builder().url(it.value.url).build()
                val stream = client.newCall(request).execute().body?.byteStream()
                stream?.let { inputStream -> streams.add(inputStream) }
            }
            return@withContext streams
        }

    override suspend fun fetchTransmitters(): List<SatTrans> {
        return api.fetchTransList()
    }

    override suspend fun insertEntries(entries: List<SatEntry>) {
        entriesDao.insertEntries(entries)
    }

    override suspend fun getAllEntries(): List<SatEntry> {
        return entriesDao.getAllEntries()
    }

    override suspend fun getSelectedEntries(): List<SatEntry> {
        return entriesDao.getSelectedEntries()
    }

    override suspend fun updateEntriesSelection(catNumList: List<Int>) {
        entriesDao.clearEntriesSelection()
        catNumList.forEach { entriesDao.updateEntrySelection(it) }
    }

    override suspend fun clearEntries() {
        entriesDao.clearEntries()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun updateTransmitters() {
    }

    override suspend fun getTransmittersByCatNum(catNum: Int): List<SatTrans> {
        return transDao.getTransmittersForCatNum(catNum)
    }

    override suspend fun clearTransmitters() {
        transDao.clearTransmitters()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun updateSources(sources: List<TleSource>) {
        clearSources()
        sourcesDao.insertSources(sources)
    }

    override suspend fun getSources(): List<TleSource> {
        return sourcesDao.getSources()
    }

    override suspend fun clearSources() {
        sourcesDao.clearSources()
    }
}
