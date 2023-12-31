package com.ajaxproject.telegrambot.handler.impl

import com.ajaxproject.telegrambot.enums.Commands.START
import com.ajaxproject.telegrambot.enums.ConversationState.WAITING_FOR_NUMBER
import com.ajaxproject.telegrambot.enums.Localization
import com.ajaxproject.telegrambot.enums.Localization.ENG_LOCALIZATION
import com.ajaxproject.telegrambot.enums.Localization.UKR_LOCALIZATION
import com.ajaxproject.telegrambot.enums.TextPropertyName.WELCOME_TEXT
import com.ajaxproject.telegrambot.handler.UserRequestHandler
import com.ajaxproject.telegrambot.service.telegram.TelegramMessageService
import com.ajaxproject.telegrambot.service.TextService
import com.ajaxproject.telegrambot.service.UserSessionService
import com.ajaxproject.telegrambot.service.updatemodels.UpdateRequest
import com.ajaxproject.telegrambot.util.KeyboardUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class StartCommandHandler(
    private val telegramService: TelegramMessageService,
) : UserRequestHandler {

    override fun isApplicable(request: UpdateRequest): Boolean {
        return isCommand(request.update, START.command)
    }

    override fun handle(dispatchRequest: UpdateRequest): Mono<Unit> {
        return telegramService.sendMessage(
            chatId = dispatchRequest.chatId,
            text = "Choose the language",
            replyKeyboard = KeyboardUtils.run {
                inlineKeyboardInOneRow(
                    inlineButton("ENG", ENG_LOCALIZATION.name),
                    inlineButton("UKR", UKR_LOCALIZATION.name)
                )
            }
        ).thenReturn(Unit)
    }
}

@Component
class LanguageButtonsHandler(
    private val textService: TextService,
    private val userSessionService: UserSessionService,
    private val telegramService: TelegramMessageService,
) : UserRequestHandler {

    override fun isApplicable(request: UpdateRequest): Boolean {
        return isCommand(
            update = request.update,
            command = arrayOf(ENG_LOCALIZATION.name, UKR_LOCALIZATION.name)
        )
    }

    override fun handle(dispatchRequest: UpdateRequest): Mono<Unit> {
        return Mono.just(Localization.valueOf(dispatchRequest.update.callbackQuery.data))
            .flatMap {
                userSessionService.updateSession(WAITING_FOR_NUMBER, dispatchRequest.chatId, it)
            }
            .flatMap { session ->
                telegramService.sendMessage(
                    chatId = dispatchRequest.chatId,
                    text = textService.getText(session.localization, WELCOME_TEXT.name)
                )
            }
            .thenReturn(Unit)
    }
}
