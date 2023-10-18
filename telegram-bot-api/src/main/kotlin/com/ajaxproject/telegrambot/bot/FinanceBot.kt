package com.ajaxproject.telegrambot.bot

import com.ajaxproject.telegrambot.bot.properties.BotProperties
import com.ajaxproject.telegrambot.bot.service.UserSessionService
import com.ajaxproject.telegrambot.bot.service.updatemodels.UpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class FinanceBot(
    val telegramUpdateDispatcher: TelegramUpdateDispatcher,
    val userSessionService: UserSessionService,
    val botProperties: BotProperties,
) : TelegramLongPollingBot(botProperties.token) {

    override fun getBotUsername(): String = botProperties.username

    override fun onUpdateReceived(update: Update) {
        if ((!update.hasMessage() || !update.message.hasText()) && !update.hasCallbackQuery()) {
            log.warn("Unexpected update from user")
            return
        }

        val chatId = when {
            update.message != null -> update.message.chatId
            update.callbackQuery != null -> update.callbackQuery.message.chatId
            else -> return
        }

        val updateRequest = UpdateRequest(
            update = update,
            updateSession = userSessionService.getSession(chatId),
            chatId = chatId
        )

        val isDispatched = telegramUpdateDispatcher.dispatch(updateRequest)

        if (!isDispatched) {
            log.warn(
                "Received unexpected update from user: userId={}, updateDetails={}",
                update.message.from.id,
                update.message.text
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FinanceBot::class.java)
    }
}