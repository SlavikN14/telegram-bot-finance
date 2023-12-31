package com.ajaxproject.telegrambot.configuration

import com.ajaxproject.internalapi.CurrencyEvent
import com.ajaxproject.internalapi.finance.output.pubsub.CurrencyUpdatedEvent
import com.google.protobuf.GeneratedMessageV3
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class KafkaConsumerConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String
) {
    @Bean
    fun kafkaReceiverUserUpdatedEvent(): KafkaReceiver<String, CurrencyUpdatedEvent> {
        val customProperties: MutableMap<String, Any> = mutableMapOf(
            KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE to CurrencyUpdatedEvent::class.java.name
        )
        return createKafkaReceiver(
            consumerProperties(customProperties),
            CurrencyEvent.createCurrencyEventKafkaTopic(CurrencyEvent.UPDATED),
            "ajax"
        )
    }

    private fun <T : GeneratedMessageV3> createKafkaReceiver(
        properties: MutableMap<String, Any>,
        topic: String,
        groupId: String
    ): KafkaReceiver<String, T> {
        properties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        val options =
            ReceiverOptions.create<String, T>(properties).subscription(setOf(topic))
        return KafkaReceiver.create(options)
    }

    private fun consumerProperties(
        customProperties: MutableMap<String, Any> = mutableMapOf()
    ): MutableMap<String, Any> {
        val baseProperties: MutableMap<String, Any> = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaProtobufDeserializer::class.java.name,
            "schema.registry.url" to schemaRegistryUrl
        )
        baseProperties.putAll(customProperties)
        return baseProperties
    }
}
