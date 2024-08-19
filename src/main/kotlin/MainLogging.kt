package ru.otus.otuskotlin.marketplace.app.tmp

import com.benasher44.uuid.uuid4
import com.otus.otuskotlin.marketplace.api.v1.models.AdLog
import com.otus.otuskotlin.marketplace.api.v1.models.CommonLogModel
import com.otus.otuskotlin.marketplace.api.v1.models.MkplLogModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds


suspend fun main() {
    val logger = LoggerFactory.getLogger("my-logging")
    var counter = 0
    while (true) {
        val log = CommonLogModel(
            messageTime = Clock.System.now().toString(),
            traceID = "tmp-request-${counter++}",
            spanID = uuid4().toString(),
            startTime = Clock.System.now().toString(),
            endTime = (Clock.System.now() + 10.milliseconds).toString(),
            serviceName = "test-service",
            operationName = "test-operation",
            ad = MkplLogModel(
                operation = MkplLogModel.Operation.INIT,
                requestAd = AdLog(
                    id = "test-ad-id",
                    title = "test-ad-title",
                    description = "test-ad-description",
                    adType = "demand",
                    visibility = "public",
                    ownerId = "some-user-id",
                ),
            ),
        )
        logger.debug(
            "tmp log string",
            StructuredArguments.fields(log)
        )
        delay(500)
    }
}
