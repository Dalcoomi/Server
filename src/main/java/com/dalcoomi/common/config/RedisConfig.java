package com.dalcoomi.common.config;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.CONSUMER_GROUP;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.CONSUMER_NAME;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.STREAM_KEY;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate() {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate() {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(redisConnectionFactory());

		return stringRedisTemplate;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> streamMessageListenerContainer(
		RedisConnectionFactory redisConnectionFactory,
		StreamListener<String, MapRecord<String, Object, Object>> receiptStreamListener
	) {
		StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
			StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofSeconds(1)).build();

		StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> container =
			(StreamMessageListenerContainer<String, MapRecord<String, Object, Object>>)
				(StreamMessageListenerContainer<?, ?>)StreamMessageListenerContainer.create(redisConnectionFactory,
					options);

		container.receive(Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
			StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()), receiptStreamListener);

		container.start();

		return container;
	}
}
