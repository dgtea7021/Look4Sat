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

package com.rtbishop.look4sat.dagger

import android.content.Context
import com.rtbishop.look4sat.dagger.modules.*
import com.rtbishop.look4sat.ui.MainActivity
import com.rtbishop.look4sat.ui.fragments.MapOsmFragment
import com.rtbishop.look4sat.ui.fragments.MapViewFragment
import com.rtbishop.look4sat.ui.fragments.PassListFragment
import com.rtbishop.look4sat.ui.fragments.PolarViewFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [NetworkModule::class, PersistenceModule::class,
        RepoModule::class, UtilityModule::class, ViewModelModule::class]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(mainActivity: MainActivity)
    fun inject(mapViewFragment: MapViewFragment)
    fun inject(passListFragment: PassListFragment)
    fun inject(polarViewFragment: PolarViewFragment)
    fun inject(mapOsmFragment: MapOsmFragment)
}
