package webcore.extensions

import kotlin.random.Random

fun Random.randomNumberString(length: Int = 5) = (0 until length)
  .map { this.nextInt(0, 10) }
  .joinToString(separator = "")