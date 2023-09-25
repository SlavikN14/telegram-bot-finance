package com.ajaxproject.telegrambot.bot

import com.ajaxproject.telegrambot.bot.handlers.UserRequestHandler
import com.ajaxproject.telegrambot.bot.models.user.UserRequest
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class Dispatcher(
    private val handlers: List<UserRequestHandler>,
) {

    @PostConstruct
    fun init() {
        handlers.sortedWith(Comparator.comparing(UserRequestHandler::isGlobal).reversed())
            .toList()
    }

    fun dispatch(userRequest: UserRequest): Boolean {
        return handlers.firstOrNull { it.isApplicable(userRequest) }?.run {
            handle(userRequest)
            true
        } ?: false
    }
}
