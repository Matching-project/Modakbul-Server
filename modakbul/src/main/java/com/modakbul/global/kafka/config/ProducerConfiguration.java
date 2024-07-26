package com.modakbul.global.kafka.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.google.common.collect.ImmutableMap;
import com.modakbul.domain.chat.chatmessage.dto.MessageDto;

@EnableKafka
@Configuration
public class ProducerConfiguration {
	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaServerUrl;

	//Kafka ProduceFactory를 생성하는 Bean 메서드
	@Bean
	public ProducerFactory<String, MessageDto> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigurations());
	}

	// Kafka Producer 구성을 위한 설정값들을 포함한 맵을 반환하는 메서드
	@Bean
	public Map<String, Object> producerConfigurations() {
		return ImmutableMap.<String, Object>builder()
			.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerUrl)
			.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
			.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)
			.build();
	}

	// KafkaTemplate을 생성하는 Bean 메서드
	@Bean
	public KafkaTemplate<String, MessageDto> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

}
