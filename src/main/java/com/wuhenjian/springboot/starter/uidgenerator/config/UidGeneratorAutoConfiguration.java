package com.wuhenjian.springboot.starter.uidgenerator.config;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.buffer.RejectedPutBufferHandler;
import com.baidu.fsg.uid.buffer.RejectedTakeBufferHandler;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mayuhan
 * @date 2019/7/10 15:19
 */
@Configuration
@ConditionalOnClass(UidGenerator.class)
@EnableConfigurationProperties(UidGeneratorProperties.class)
@Slf4j
public class UidGeneratorAutoConfiguration {

	private final UidGeneratorProperties uidGeneratorProperties;

	@Autowired
	public UidGeneratorAutoConfiguration(UidGeneratorProperties uidGeneratorProperties) {
		this.uidGeneratorProperties = uidGeneratorProperties;
	}

	@Bean
	@ConditionalOnMissingBean(WorkerIdAssigner.class)
	public WorkerIdAssigner workerIdAssigner() {
		return new DisposableWorkerIdAssigner();
	}

	@Bean
	@ConditionalOnMissingBean(RejectedPutBufferHandler.class)
	public RejectedPutBufferHandler rejectedPutBufferHandler() {
		return (ringBuffer, uid) -> log.warn("Rejected putting buffer for uid:{}. {}", uid, ringBuffer);
	}

	@Bean
	@ConditionalOnMissingBean(RejectedTakeBufferHandler.class)
	public RejectedTakeBufferHandler rejectedTakeBufferHandler() {
		return (ringBuffer) -> {
			log.warn("Rejected take buffer. {}", ringBuffer);
			throw new RuntimeException("Rejected take buffer. " + ringBuffer);
		};
	}

	@Bean
	@ConditionalOnMissingBean(UidGenerator.class)
	public UidGenerator uidGenerator(WorkerIdAssigner workerIdAssigner,
	                                 RejectedPutBufferHandler rejectedPutBufferHandler,
	                                 RejectedTakeBufferHandler rejectedTakeBufferHandler) {
		// 使用缓冲生成器
		CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
		// 设置初始化workerId生成方式
		cachedUidGenerator.setWorkerIdAssigner(workerIdAssigner);
		// 配置参数
		cachedUidGenerator.setTimeBits(uidGeneratorProperties.getTimeBits());
		cachedUidGenerator.setWorkerBits(uidGeneratorProperties.getWorkerBits());
		cachedUidGenerator.setSeqBits(uidGeneratorProperties.getSeqBits());
		cachedUidGenerator.setEpochStr(uidGeneratorProperties.getEpochStr());
		cachedUidGenerator.setBoostPower(uidGeneratorProperties.getBoostPower());
		cachedUidGenerator.setPaddingFactor(uidGeneratorProperties.getPaddingFactor());
		if (uidGeneratorProperties.getScheduleInterval() != null) {
			cachedUidGenerator.setScheduleInterval(uidGeneratorProperties.getScheduleInterval());
		}
		// 拒绝策略: 当环已满, 无法继续填充时
		cachedUidGenerator.setRejectedPutBufferHandler(rejectedPutBufferHandler);
		// 拒绝策略: 当环已空, 无法继续获取时
		cachedUidGenerator.setRejectedTakeBufferHandler(rejectedTakeBufferHandler);

		return cachedUidGenerator;
	}
}
