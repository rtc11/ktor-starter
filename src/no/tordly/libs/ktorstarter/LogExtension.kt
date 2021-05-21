package no.tordly.libs.ktorstarter

import org.slf4j.Logger
import org.slf4j.event.Level

inline fun <reified T : Any> List<T>.logToString(logger: Logger, level: Level = Level.INFO) = this.also { list ->
    list.map(Any::toString).map {
        it.log(logger, level)
    }
}

inline fun <reified T : Any> List<T>.logSize(logger: Logger, level: Level = Level.INFO) = this.also {
    "${T::class.java.simpleName}.size: ${it.size}"
        .log(logger, level)
}

fun String.log(log: Logger, level: Level = Level.INFO) = when (level) {
    Level.DEBUG -> log.debug(this)
    Level.WARN -> log.warn(this)
    Level.ERROR -> log.error(this)
    else -> log.info(this)
}
