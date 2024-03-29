package com.codehumane.reactor.performance.controller

import com.codehumane.reactor.performance.pipeline.*
import com.codehumane.reactor.performance.pipeline.advanced.GroupByAndGroupByPipeline
import com.codehumane.reactor.performance.pipeline.advanced.GroupByAndRunnablePipeline
import com.codehumane.reactor.performance.pipeline.advanced.PublishAndPublishPipeline
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
class DefaultController(
    private val topicPipeline: TopicProcessorPipeline,
    private val nonTopicPipeline: NonTopicProcessorPipeline,
    private val stepMinifiedNonTopicProcessorPipeline: StepMinifiedNonTopicProcessorPipeline,
    private val efficientStepMinifiedNonTopicProcessorPipeline: EfficientStepMinifiedNonTopicProcessorPipeline,
    private val orderingStepMinifiedNonTopicProcessorPipeline: OrderingStepMinifiedNonTopicProcessorPipeline,
    private val nonFanOutPipeline: NonFanOutPipeline,
    private val groupByPipeline: GroupByPipeline,
    private val groupByAndRunnablePipeline: GroupByAndRunnablePipeline,
    private val groupByAndGroupByPipeline: GroupByAndGroupByPipeline,
    private val publishAndPublishPipeline: PublishAndPublishPipeline
) {

    @GetMapping("/pipeline/topic/start")
    fun startTopicPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        topicPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/nontopic/start")
    fun startNonTopicPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        nonTopicPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/stepminifiednontopic/start")
    fun startNonTopicStepMinifiedPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        stepMinifiedNonTopicProcessorPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/orderingstepminifiednontopic/start")
    fun startOrderingNonTopicStepMinifiedPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        orderingStepMinifiedNonTopicProcessorPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/efficientstepminifiednontopic/start")
    fun startEfficientNonTopicStepMinifiedPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        efficientStepMinifiedNonTopicProcessorPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/nonfanout/start")
    fun nonFanOutTopicPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        nonFanOutPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/groupby/start")
    fun groupByPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        groupByPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/advanced/groupbyandrunnable/start")
    fun groupByAndRunnablePipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        groupByAndRunnablePipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/advanced/groupbyandgroupby/start")
    fun groupByAndGroupByPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        groupByAndGroupByPipeline.start(publishItemCount)
        return Mono.just("started")
    }

    @GetMapping("/pipeline/advanced/publishandpublish/start")
    fun publishAndPublishPipeline(@RequestParam("count") publishItemCount: Int): Mono<String> {
        publishAndPublishPipeline.start(publishItemCount)
        return Mono.just("started")
    }

}
