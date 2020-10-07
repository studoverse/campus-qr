package com.studo.campusqr

import com.studo.campusqr.database.initialDatabaseSetup
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)

@Suite.SuiteClasses(
    AutomaticDeletionTest::class,
    AutomaticCheckOutTest::class,
    ContactTracingTest::class,
)

object CampusQrTestSuite {
  @BeforeClass
  @JvmStatic
  fun setup() {
    runBlocking { initialDatabaseSetup() }
  }
}