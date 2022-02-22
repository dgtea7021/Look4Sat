/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2021 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rtbishop.look4sat.framework

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.rtbishop.look4sat.R
import com.rtbishop.look4sat.data.ISettingsHandler
import com.rtbishop.look4sat.domain.ILocationHandler
import com.rtbishop.look4sat.domain.QthConverter
import com.rtbishop.look4sat.domain.model.DataState
import com.rtbishop.look4sat.domain.predict.GeoPos
import com.rtbishop.look4sat.domain.round
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsHandler: ISettingsHandler,
) : LocationListener, ILocationHandler {

    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val providerDef = LocationManager.PASSIVE_PROVIDER
    private val providerNet = LocationManager.NETWORK_PROVIDER
    private val providerGps = LocationManager.GPS_PROVIDER
    private val locationCoarse = Manifest.permission.ACCESS_COARSE_LOCATION
    private val locationFine = Manifest.permission.ACCESS_FINE_LOCATION
    private val _stationPosition = MutableStateFlow<DataState<GeoPos>>(DataState.Handled)
    private var currentLocator = settingsHandler.loadStationLocator()
    private var currentPosition = settingsHandler.loadStationPosition()

    override val stationPosition: StateFlow<DataState<GeoPos>> = _stationPosition

    override fun getStationLocator(): String = currentLocator

    override fun getStationPosition(): GeoPos = currentPosition

    override fun setStationPosition(latitude: Double, longitude: Double) {
        if (QthConverter.isValidPosition(latitude, longitude)) {
            val tempLon = if (longitude > 180.0) longitude - 180 else longitude
            val newLat = latitude.round(4)
            val newLon = tempLon.round(4)
            QthConverter.positionToQth(newLat, newLon)?.let { locator ->
                currentLocator = locator
                currentPosition = GeoPos(newLat, newLon)
                settingsHandler.saveStationLocator(locator)
                settingsHandler.saveStationPosition(currentPosition)
                _stationPosition.value = DataState.Success(currentPosition)
            }
        } else {
            _stationPosition.value =
                DataState.Error(context.getString(R.string.pref_pos_manual_error))
        }
    }

    override fun setPositionFromGps() {
        val result = ContextCompat.checkSelfPermission(context, locationFine)
        if (manager.isProviderEnabled(providerGps) && result == PackageManager.PERMISSION_GRANTED) {
            val location = manager.getLastKnownLocation(providerDef)
            if (location != null) {
                setStationPosition(location.latitude, location.longitude)
            } else {
                _stationPosition.value = DataState.Loading
                manager.requestLocationUpdates(providerGps, 0L, 0f, this)
            }
        } else {
            _stationPosition.value = DataState.Error(context.getString(R.string.pref_pos_gps_null))
        }
    }

    override fun setPositionFromNet() {
        val result = ContextCompat.checkSelfPermission(context, locationCoarse)
        if (manager.isProviderEnabled(providerNet) && result == PackageManager.PERMISSION_GRANTED) {
            val location = manager.getLastKnownLocation(providerDef)
            if (location != null) {
                setStationPosition(location.latitude, location.longitude)
            } else {
                _stationPosition.value = DataState.Loading
                manager.requestLocationUpdates(providerNet, 0L, 0f, this)
            }
        } else {
            _stationPosition.value = DataState.Error(context.getString(R.string.pref_pos_gps_null))
        }
    }

    override fun setPositionFromQth(locator: String) {
        val position = QthConverter.qthToPosition(locator)
        if (position != null) {
            currentLocator = locator
            currentPosition = position
            settingsHandler.saveStationLocator(locator)
            settingsHandler.saveStationPosition(currentPosition)
            _stationPosition.value = DataState.Success(currentPosition)
        } else {
            _stationPosition.value = DataState.Error(context.getString(R.string.pref_pos_qth_error))
        }
    }

    override fun setPositionHandled() {
        _stationPosition.value = DataState.Handled
    }

    override fun onLocationChanged(location: Location) {
        manager.removeUpdates(this)
        setStationPosition(location.latitude, location.longitude)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}