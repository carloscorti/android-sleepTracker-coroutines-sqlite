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

package com.example.android.trackmysleepquality

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class SleepDatabaseTest {

    private lateinit var sleepDao: SleepDatabaseDao
    private lateinit var db: SleepDatabase

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        Log.i("cacTest", "enter before")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        sleepDao = db.sleepDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetNight() {
        val night = SleepNight()
        sleepDao.insert(night)
        val tonight = sleepDao.getTonight()
        assertEquals(-1, tonight?.sleepQuality)
    }

    @Test
    @Throws(Exception::class)
    fun insertUpdateAndGet() {
        var night = SleepNight()
        sleepDao.insert(night)
        night = sleepDao.getTonight() ?: night
        val nightSleepQualityBeforeUpdate = night.sleepQuality
        night.sleepQuality = 4
        sleepDao.update(night)
        val updatedNight = sleepDao.get(night.nightId)
        assertEquals(night.nightId, updatedNight?.nightId)
        assertNotEquals(nightSleepQualityBeforeUpdate, updatedNight?.sleepQuality)
        assertEquals(4, updatedNight?.sleepQuality)
    }

    @Test
    @Throws(Exception::class)
    fun getAll() {
        val nights : List<SleepNight> = listOf(SleepNight(), SleepNight(), SleepNight(), SleepNight())
        nights.forEach { night -> sleepDao.insert(night) }
        val dataBaseNights = sleepDao.getAllNights().blockingObserve()
        assertEquals(4, dataBaseNights?.size)
    }

    @Test
    @Throws(Exception::class)
    fun clear() {
        val nights : List<SleepNight> = listOf(SleepNight(), SleepNight(), SleepNight(), SleepNight())
        nights.forEach { night -> sleepDao.insert(night) }
        sleepDao.clear()
        val emptyTable = sleepDao.getAllNights().blockingObserve()
        assertEquals(0, emptyTable?.size)
    }
}

// LiveData extension to block threat via observer and CountDownLatch to await for LiveData value
// -->androidTestImplementation "androidx.arch.core:core-testing:2.1.0"<-- on gradle dependencies
// to execute observeForever outside the main threat
private fun <T> LiveData<T>.blockingObserve(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> { t ->
        value = t
        latch.countDown()
    }

    observeForever(observer)

    latch.await(2, TimeUnit.SECONDS)
    return value
}

