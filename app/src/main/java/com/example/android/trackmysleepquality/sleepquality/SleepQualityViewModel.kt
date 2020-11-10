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

package com.example.android.trackmysleepquality.sleepquality

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel(private val database: SleepDatabaseDao,
                            private val nightId: Long) : ViewModel() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker

    fun onSetSleepQuality(quality: Int) {
        coroutineScope.launch {
            val tonight = withContext(this.coroutineContext) { getTonight(nightId) } ?: return@launch
            tonight.sleepQuality = quality
            update(tonight)
            _navigateToSleepTracker.value = true
        }
    }


    private suspend fun getTonight(nightId: Long): SleepNight? =
            withContext(Dispatchers.IO) {
                database.get(nightId)
            }

    private suspend fun update(night: SleepNight) =
            withContext(Dispatchers.IO) {
                database.update(night)
            }

    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}