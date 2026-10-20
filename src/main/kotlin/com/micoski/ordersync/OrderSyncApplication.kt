package com.micoski.ordersync

import com.micoski.ordersync.config.AppProperties
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties::class)
@Import(SqsBootstrapConfiguration::class)
class OrderSyncApplication

fun main(args: Array<String>) {
    runApplication<OrderSyncApplication>(*args)
}
