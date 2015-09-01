package ru.vasil.config;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vasil.crontab.Crontab;
import ru.vasil.file.FileListener;

/**
 * vasil: 30.08.15
 */
@Configuration
public class QuartzConfigation {

    @Value("${crontab.file.name:main.crontab}")
    public String crontabFileName;

    @Value("${crontab.refresh.period.seconds:60}")
    public long refreshPeriodSeconds;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Scheduler getScheduler() throws Exception {
        return StdSchedulerFactory.getDefaultScheduler();
    }

    @Bean
    public Crontab getCrontab(Scheduler scheduler) {
        return new Crontab(scheduler);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public FileListener getFileListener(Crontab crontab) throws Exception {
        return new FileListener(crontabFileName, refreshPeriodSeconds, crontab);
    }
}
