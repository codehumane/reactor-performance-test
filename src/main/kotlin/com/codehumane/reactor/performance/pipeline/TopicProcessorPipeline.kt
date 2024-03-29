package com.codehumane.reactor.performance.pipeline

import com.codehumane.reactor.performance.item.*
import com.codehumane.reactor.performance.metric.TPSCollector
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.FluxSink.OverflowStrategy.BUFFER
import reactor.core.publisher.Mono
import reactor.core.publisher.TopicProcessor
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.util.concurrent.WaitStrategy
import java.util.concurrent.CompletableFuture
import kotlin.math.pow
import kotlin.system.exitProcess

/**
 * binary log event를 받아서 replicate item으로 변환하고 각 샤드 DB로 복제하는 일련의 파이프라인을 구성
 *
 * - topic processor 이용
 *
<1,000,000건 실행 후, 20초 뒤 성능>
start tps: 2888.1111111111113, detail: 3114, 2814, 2950, 2820, 2839, 2915, 2832, 2949, 2760
step1 tps: 2963.0, detail: 3784, 2808, 2945, 2802, 2861, 2928, 2810, 2948, 2781
step2 tps: 2961.4444444444443, detail: 3778, 2817, 2943, 2806, 2851, 2934, 2813, 2943, 2768
pipeline_final_0 tps: 185.11111111111111, detail: 236, 177, 183, 177, 177, 183, 176, 183, 174
pipeline_final_1 tps: 185.22222222222223, detail: 237, 176, 185, 175, 177, 183, 178, 182, 174
pipeline_final_2 tps: 185.11111111111111, detail: 236, 176, 184, 176, 178, 182, 177, 184, 173
pipeline_final_3 tps: 185.0, detail: 236, 177, 183, 176, 179, 181, 177, 183, 173
pipeline_final_4 tps: 185.0, detail: 236, 176, 182, 178, 178, 182, 176, 184, 173
pipeline_final_5 tps: 185.22222222222223, detail: 237, 176, 183, 177, 178, 182, 176, 184, 174
pipeline_final_6 tps: 185.0, detail: 237, 177, 182, 177, 178, 182, 178, 182, 172
pipeline_final_7 tps: 185.33333333333334, detail: 238, 176, 184, 177, 177, 182, 177, 183, 174
pipeline_final_8 tps: 185.22222222222223, detail: 237, 175, 185, 176, 179, 181, 175, 185, 174
pipeline_final_9 tps: 185.22222222222223, detail: 237, 176, 184, 176, 177, 183, 176, 184, 174
pipeline_final_10 tps: 185.0, detail: 236, 175, 185, 176, 178, 182, 177, 182, 174
pipeline_final_11 tps: 185.11111111111111, detail: 236, 177, 184, 176, 178, 182, 175, 186, 172
pipeline_final_12 tps: 185.0, detail: 235, 176, 184, 177, 177, 183, 176, 184, 173
pipeline_final_13 tps: 185.33333333333334, detail: 238, 176, 185, 176, 177, 183, 177, 183, 173
pipeline_final_14 tps: 185.22222222222223, detail: 237, 178, 183, 176, 177, 183, 176, 183, 174
pipeline_final_15 tps: 185.11111111111111, detail: 237, 177, 183, 177, 177, 183, 176, 183, 173

<1,000,000건 실행 후, 끝날 무렵 성능이 아니고... 너무 오래 걸려서 중간 정도의 성능>
start tps: 2312.4444444444443, detail: 2418, 2410, 2370, 2273, 2365, 2326, 2388, 2340, 1922
step1 tps: 2310.8888888888887, detail: 2419, 2404, 2381, 2262, 2385, 2326, 2375, 2326, 1920
step2 tps: 2311.0, detail: 2420, 2403, 2373, 2273, 2370, 2341, 2376, 2326, 1917
pipeline_final_0 tps: 144.44444444444446, detail: 151, 150, 148, 143, 148, 145, 150, 146, 119
pipeline_final_1 tps: 144.55555555555554, detail: 152, 150, 148, 142, 148, 146, 147, 147, 121
pipeline_final_2 tps: 144.55555555555554, detail: 150, 152, 147, 143, 148, 146, 148, 146, 121
pipeline_final_3 tps: 144.55555555555554, detail: 152, 151, 146, 144, 148, 145, 150, 145, 120
pipeline_final_4 tps: 144.44444444444446, detail: 151, 151, 148, 142, 148, 145, 148, 148, 119
pipeline_final_5 tps: 144.44444444444446, detail: 151, 150, 148, 143, 147, 146, 148, 147, 120
pipeline_final_6 tps: 144.44444444444446, detail: 150, 149, 150, 143, 147, 146, 147, 148, 120
pipeline_final_7 tps: 144.44444444444446, detail: 151, 150, 148, 142, 148, 147, 148, 145, 121
pipeline_final_8 tps: 144.44444444444446, detail: 151, 150, 148, 143, 147, 147, 147, 146, 121
pipeline_final_9 tps: 144.44444444444446, detail: 151, 150, 147, 144, 147, 146, 148, 147, 120
pipeline_final_10 tps: 144.33333333333334, detail: 150, 152, 147, 143, 147, 146, 148, 145, 121
pipeline_final_11 tps: 144.44444444444446, detail: 151, 151, 148, 143, 147, 147, 147, 147, 119
pipeline_final_12 tps: 144.55555555555554, detail: 151, 151, 147, 144, 147, 146, 148, 147, 120
pipeline_final_13 tps: 144.33333333333334, detail: 151, 150, 147, 143, 148, 145, 150, 145, 120
pipeline_final_14 tps: 144.33333333333334, detail: 151, 150, 148, 142, 148, 145, 150, 146, 119
pipeline_final_15 tps: 144.55555555555554, detail: 152, 150, 148, 142, 148, 146, 148, 146, 121
 */
@Service
class TopicProcessorPipeline(private val meterRegistry: PrometheusMeterRegistry) {

    private val log = LoggerFactory.getLogger(TopicProcessorPipeline::class.java)

    private val step1ThreadCoreSize = 32
    private val step2ThreadCoreSize = 32
    private val finalItemThreadCoreSize = 4
    private val topicSubscriberCount = 16

    private val itemGenerator = StartItemGenerator()
    private val step1Generator = Step1ItemGenerator()
    private val step2Generator = Step2ItemGenerator()
    private val finalGenerators = (0 until topicSubscriberCount)
        .map { FinalItemGenerator(it.toString()) }

    private val step1Scheduler = scheduler(step1ThreadCoreSize, 32, "step1-")
    private val step2Scheduler = scheduler(step2ThreadCoreSize, 32, "step2-")
    private val finalSchedulers = (0 until topicSubscriberCount)
        .map { scheduler(finalItemThreadCoreSize, 32, "final-$it-") }

    private val startMetricTimer = meterRegistry.timer("pipeline_start")
    private val step1MetricTimer = meterRegistry.timer("pipeline_step1")
    private val step2MetricTimer = meterRegistry.timer("pipeline_step2")
    private val finalMetricTimers = (0 until topicSubscriberCount)
        .map { meterRegistry.timer("pipeline_final_$it") }

    /**
     * 파이프라인 실행 (구독)
     */
    fun start(publishItemCount: Int) {

        startTpsCollector()

        // topic prepare
        val topicProcessor = TopicProcessor
            .builder<Step2Item>()
            .name("final-topic")
            .bufferSize(2.toDouble().pow(13.toDouble()).toInt())
            .waitStrategy(WaitStrategy.busySpin())
            .build()

        // topic publish & intermediate transform
        Flux.create<StartItem>({ startItemPublishAsynchronously(it, publishItemCount) }, BUFFER)
            .flatMapSequential<Step1Item>({ generateStep1Item(it) }, step1ThreadCoreSize, 1)
            .flatMapSequential<Step2Item>({ generateStep2Item(it) }, step2ThreadCoreSize, 1)
            .doOnError { terminateOnUnrecoverableError(it) }
            .subscribe(topicProcessor)
//            .subscribe()

        // topic subscription & final transform
        (0 until topicSubscriberCount).forEach { index ->
            Flux.from(topicProcessor)
                .filter { (it.value % topicSubscriberCount) == index }
                .flatMap({ generateFinalItem(it, index) }, finalItemThreadCoreSize, 1)
                .doOnError { terminateOnUnrecoverableError(it) }
                .subscribe()
        }

    }

    private fun scheduler(corePoolSize: Int, queueCapacity: Int, namePrefix: String): Scheduler {
        val executor = executor(corePoolSize, queueCapacity, namePrefix)
        return Schedulers.fromExecutorService(executor.threadPoolExecutor)
    }

    private fun executor(corePoolSize: Int, queueCapacity: Int, namePrefix: String): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            this.corePoolSize = corePoolSize
            setQueueCapacity(queueCapacity)
            setThreadNamePrefix(namePrefix)
            initialize()
        }
    }

    private fun startItemPublishAsynchronously(sink: FluxSink<StartItem>, count: Int) {
        log.info("play ground publishing source")
        CompletableFuture.runAsync {

            (0 until count).forEach { index ->
                startMetricTimer.record {
                    sink.next(itemGenerator.withDelayCount(1_000))
                }

                if (index % 1000 == 0) {
                    log.info("$index items published.")
                }
            }

            log.info("item publishing finished.")
        }
    }

    private fun generateStep1Item(source: StartItem): Mono<Step1Item> {
        return Mono.create<Step1Item> {
            step1MetricTimer.record {
                it.success(step1Generator.withDelayMillis(source, 1))
            }
        }.subscribeOn(step1Scheduler)
    }

    private fun generateStep2Item(source: Step1Item): Mono<Step2Item> {
        return Mono.create<Step2Item> {
            step2MetricTimer.record {
                it.success(step2Generator.withDelayMillis(source, 5))
            }
        }.subscribeOn(step2Scheduler)
    }

    private fun generateFinalItem(source: Step2Item, index: Int): Mono<FinalItem> {
        val generator = finalGenerators[index]
        val scheduler = finalSchedulers[index]
        val timer = finalMetricTimers[index]

        return Mono.create<FinalItem> {
            timer.record {
                it.success(generator.withDelayMillis(source, 10))
            }
        }.subscribeOn(scheduler)
    }

    private fun terminateOnUnrecoverableError(it: Throwable?) {
        log.error("unrecoverable error. system exit", it)
        exitProcess(666)
    }

    private fun startTpsCollector() {
        val tpsCollectorSource = mutableMapOf(
            "start" to startMetricTimer,
            "step1" to step1MetricTimer,
            "step2" to step2MetricTimer
        )

        finalMetricTimers.forEach {
            tpsCollectorSource[it.id.name] = it
        }

        TPSCollector(10, tpsCollectorSource).start()
    }

}