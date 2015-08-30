package ru.vasil.quartz;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * vasil: 30.08.15
 */
@Configuration
public class QuartzConfigation {

    @Value("${crontab.file.name:main.crontab}")
    public String crontabFileName;

    @Value("${crontab.refresh.period.millis:60000}")
    public long refreshPeriodMillis;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Scheduler getScheduler() throws Exception {
        return StdSchedulerFactory.getDefaultScheduler();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Crontab getCrontab() throws Exception {
        return new Crontab(crontabFileName, refreshPeriodMillis);
    }
}
