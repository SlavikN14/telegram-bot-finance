package com.ajaxproject.telegrambot.service

import com.ajaxproject.telegrambot.enums.Localization
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.charset.StandardCharsets
import java.util.Properties

@Service
class TextService {

    private val textMap: MutableMap<String, Map<String, String>> = mutableMapOf()

    @PostConstruct
    fun readTextFromFile() {
        Flux.fromIterable(listOf("/texts/texts-en.properties", "/texts/texts-ua.properties"))
            .flatMap { localization ->
                Mono.fromSupplier {
                    val properties = ClassPathResource(localization).inputStream.use {
                        Properties().apply {
                            it.reader(StandardCharsets.UTF_8).use { reader -> load(reader) }
                        }
                    }
                    localization to properties.entries.associate { it.key.toString() to it.value.toString() }
                }
            }
            .collectMap({ it.first }, { it.second })
            .doOnNext { resultMap ->
                textMap.putAll(resultMap)
            }
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorMap { FileNameNotFoundException("File not found for some Localization") }
            .subscribe()
    }

    fun getText(localization: Localization, textPropertyName: String): String {
        return textMap[localization.path]?.get(textPropertyName)
            ?: throw TextNotFoundException("Text not found")
    }
}

fun Update.isTextMessage(): Boolean = hasMessage() && message.hasText()

class TextNotFoundException(message: String) : Exception(message)

class FileNameNotFoundException(message: String) : Exception(message)
