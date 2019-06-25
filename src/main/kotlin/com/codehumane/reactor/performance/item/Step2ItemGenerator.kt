package com.codehumane.reactor.performance.item

import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Step2ItemGenerator {

    private val id = AtomicInteger(0)
    private val random = Random()

    fun withDelay(source: Step1Item): Step2Item {
        Thread.sleep(0, random.nextInt(100_000)) // 최대 0.1ms
        return Step2Item(source, id.getAndIncrement())
    }

}