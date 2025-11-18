package org.example.oddventure.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduling 설정
 * @Scheduled 어노테이션을 사용한 스케줄러 활성화
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}