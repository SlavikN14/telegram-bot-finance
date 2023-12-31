package com.ajaxproject.telegrambot.service

import com.ajaxproject.telegrambot.enums.ConversationState
import com.ajaxproject.telegrambot.enums.Localization
import com.ajaxproject.telegrambot.service.updatemodels.UpdateSession
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class UserSessionService {

    private val updateSessionMap: MutableMap<Long, UpdateSession> = mutableMapOf()

    fun getSession(chatId: Long): UpdateSession {
        return updateSessionMap.getOrDefault(chatId, UpdateSession(chatId = chatId))
    }

    fun updateSession(state: ConversationState, chatId: Long, localization: Localization): Mono<UpdateSession> {
        return Mono.fromSupplier {
            UpdateSession(state = state, chatId = chatId, localization = localization)
        }
            .doOnNext { newSession -> updateSessionMap[chatId] = newSession }
            .subscribeOn(Schedulers.boundedElastic())
    }
}
