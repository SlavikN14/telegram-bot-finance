package com.ajaxproject.telegrambot.bot.handlers.impl.financehandlers

import com.ajaxproject.financemodels.enums.Finance
import com.ajaxproject.telegrambot.bot.dto.toFinanceResponse
import com.ajaxproject.telegrambot.bot.enums.Commands.GET_EXPENSES
import com.ajaxproject.telegrambot.bot.enums.Commands.GET_INCOMES
import com.ajaxproject.telegrambot.bot.enums.ConversationState.CONVERSATION_STARTED
import com.ajaxproject.telegrambot.bot.handlers.UserRequestHandler
import com.ajaxproject.telegrambot.bot.service.FinanceClient
import com.ajaxproject.telegrambot.bot.service.TelegramService
import com.ajaxproject.telegrambot.bot.service.UserSessionService
import com.ajaxproject.telegrambot.bot.service.updatemodels.UpdateRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class GetFinancesModelsHandler(
    private val telegramService: TelegramService,
    private val financeClient: FinanceClient,
    private val userSessionService: UserSessionService,
) : UserRequestHandler {

    override fun isApplicable(request: UpdateRequest): Boolean {
        return isCommand(request.update, GET_INCOMES.command, GET_EXPENSES.command)
    }

    override fun handle(dispatchRequest: UpdateRequest): Mono<Unit> {
        return financeClient.getAllFinancesByUserId(
            dispatchRequest.chatId,
            dispatchRequest.update.callbackQuery.data.checkCommandIncomeOrExpense()
        )
            .flatMapMany { list ->
                Flux.fromIterable(list)
                    .map { it.toFinanceResponse() }
            }
            .flatMap { financeResponse ->
                telegramService.sendMessage(
                    chatId = dispatchRequest.chatId,
                    text = financeResponse.toString()
                )
            }
            .flatMap {
                userSessionService.updateSession(
                    CONVERSATION_STARTED,
                    dispatchRequest.chatId,
                    dispatchRequest.updateSession.localization
                )
            }
            .then(telegramService.returnToMainMenu(dispatchRequest))
            .thenReturn(Unit)
    }
}

private fun String.checkCommandIncomeOrExpense(): Finance {
    return when (this) {
        GET_INCOMES.command -> Finance.INCOME
        GET_EXPENSES.command -> Finance.EXPENSE
        else -> throw IllegalArgumentException("Unknown finance type")
    }
}
