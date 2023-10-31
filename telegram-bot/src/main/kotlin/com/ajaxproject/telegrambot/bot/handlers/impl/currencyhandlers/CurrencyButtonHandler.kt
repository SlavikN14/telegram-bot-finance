package com.ajaxproject.telegrambot.bot.handlers.impl.currencyhandlers

import com.ajaxproject.telegrambot.bot.enums.Buttons.GET_CURRENCY_EUR_BUTTON
import com.ajaxproject.telegrambot.bot.enums.Buttons.GET_CURRENCY_USD_BUTTON
import com.ajaxproject.telegrambot.bot.enums.Commands
import com.ajaxproject.telegrambot.bot.enums.Currency.EUR
import com.ajaxproject.telegrambot.bot.enums.Currency.USD
import com.ajaxproject.telegrambot.bot.enums.TextPropertyName.CURRENCY_TEXT
import com.ajaxproject.telegrambot.bot.handlers.UserRequestHandler
import com.ajaxproject.telegrambot.bot.service.TelegramService
import com.ajaxproject.telegrambot.bot.service.TextService
import com.ajaxproject.telegrambot.bot.service.updatemodels.UpdateRequest
import com.ajaxproject.telegrambot.bot.utils.KeyboardUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CurrencyButtonHandler(
    private val telegramService: TelegramService,
    private val textService: TextService,
) : UserRequestHandler {

    override fun isApplicable(request: UpdateRequest): Boolean {
        return isCommand(request.update, Commands.CURRENCY.command)
    }

    override fun handle(dispatchRequest: UpdateRequest): Mono<Unit> {
        val localizationText = textService.textMap[dispatchRequest.updateSession.localization]
        return telegramService.sendMessage(
            chatId = dispatchRequest.chatId,
            text = localizationText?.get(CURRENCY_TEXT.name)
                .toString(),
            replyKeyboard = KeyboardUtils.run {
                inlineKeyboardInOneRow(
                    inlineButton(
                        localizationText?.get(GET_CURRENCY_USD_BUTTON.name).toString(),
                        USD.code.toString()
                    ),
                    inlineButton(
                        localizationText?.get(GET_CURRENCY_EUR_BUTTON.name).toString(),
                        EUR.code.toString()
                    )
                )
            }
        ).thenReturn(Unit)
    }
}
