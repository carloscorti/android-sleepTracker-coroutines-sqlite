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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val _navigateToSleepDetail = MutableLiveData<Long>()
    val navigateToSleepDetail
        get() = _navigateToSleepDetail

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    private var tonight = MutableLiveData<SleepNight?>()

//    private var _allNights: LiveData<List<SleepNight>> = database.getAllNights()

    var allNights: LiveData<List<SleepNight>> = database.getAllNights()


//    var allNights = Transformations.map(_allNights) { nightsList ->
//        formatNights(nightsList, application.resources)
//    }

    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }
//    val clearButtonVisible = Transformations.map(_allNights) {
    val clearButtonVisible = Transformations.map(allNights) {
        it.isNotEmpty()
    }

    private var _showSnackBarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackBarEvent

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        coroutineScope.launch {
            tonight.value = getTonight()
        }
    }

    fun startSleepTrack() {
        coroutineScope.launch {
            insert(SleepNight())
            tonight.value = getTonight()
        }
    }

    fun stopSleepTrack() {
        coroutineScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    fun doneNavigatingQuality() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackBarEvent.value = false
    }

    fun clearSleepsNights() {
        coroutineScope.launch {
            clearDataBaseNights()
            tonight.value = null
            _showSnackBarEvent.value = true
        }
    }

    fun onSleepNightClicked(id: Long){
        _navigateToSleepDetail.value = id
    }

    fun doneNavigatingDetail() {
        _navigateToSleepDetail.value = null
    }

    private suspend fun getTonight(): SleepNight? =
            withContext(Dispatchers.IO) {
                var toNight = database.getTonight()
                if (toNight?.startTimeMilli != toNight?.endTimeMilli) {
                    toNight = null
                }
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

    private suspend fun clearDataBaseNights() =
            withContext(Dispatchers.IO) {
                database.clear()
            }
    
    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }




}



