package ru.vasil;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.vasil.config.PropertyPlaceholderConfig;
import ru.vasil.config.QuartzConfigation;

/**
 * vasil: 30.08.15
 */
public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) throws Exception {

        try {
            log.info("Loading beans");
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
            try {
                ctx.register(PropertyPlaceholderConfig.class);
                ctx.register(QuartzConfigation.class);
                ctx.refresh();
                ctx.registerShutdownHook();
            } catch (Throwable t) {
                ctx.close();
                Throwables.propagateIfPossible(t, Exception.class);
            }
        } catch (Exception e) {
            log.error("Unexpected initialization exception", e);
            throw e;
        }

    }
}
