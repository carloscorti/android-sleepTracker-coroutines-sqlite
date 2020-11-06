/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var _allNights: LiveData<List<SleepNight>> = database.getAllNights()

    var allNights = Transformations.map(_allNights) { nightsList ->
        formatNights(nightsList, application.resources)
    }

    fun startSleepTrack() {
//        viewModelScope.launch {
//            insert(SleepNight())
//        }
        coroutineScope.launch {
            val night = withContext(this.coroutineContext) { getTonight() }
            if (night == null || (night.startTimeMilli != night.endTimeMilli)) insert(SleepNight())
        }
    }


    fun stopSleepTrack() {
        coroutineScope.launch {
//            val nightReq = async { requireNotNull(getTonight()) }
//            val night = nightReq.await()
            // here getTonight is run in Default Threat
//            val night = withContext(Dispatchers.Default) { requireNotNull(getTonight()) }
            // the above same as next line
            val night = withContext(this.coroutineContext) { getTonight() }

            if (night!=null && night.startTimeMilli == night.endTimeMilli) {
                night.endTimeMilli = System.currentTimeMillis()
                update(night)
            }
        }
    }


    private suspend fun getTonight(): SleepNight? =
            withContext(Dispatchers.IO) {
                val toNight = database.getTonight()
                toNight
            }

    private suspend fun insert(night: SleepNight) =
            withContext(Dispatchers.IO) {
                database.insert(night)
            }

    private suspend fun update(nigh: SleepNight) =
            withContext(Dispatchers.IO) {
                database.update(nigh)
            }


    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }




}



