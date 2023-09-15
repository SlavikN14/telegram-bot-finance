package com.ajaxproject.telegrambot.bot.service

import com.ajaxproject.telegrambot.bot.sender.BankInfoBotSender
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

@Component
class TelegramService(
    val botSender: BankInfoBotSender,
) {

    fun sendMessage(chatId: Long, text: String) {
        sendMessage(chatId, text, null)
    }

    fun sendMessage(chatId: Long, text: String, replyKeyboard: ReplyKeyboard?) {
        val sendMessage = SendMessage.builder().also { msg ->
            msg.text(text)
            msg.chatId(chatId)
            msg.replyMarkup(replyKeyboard)
        }.build()
        execute(sendMessage)
    }

    private fun execute(botApiMethod: BotApiMethodMessage) {
        botApiMethod.runCatching { botSender.execute(botApiMethod) }
            .onFailure { log.error("Failed to execute bot method={}", botApiMethod, it) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TelegramService::class.java)
    }
}
