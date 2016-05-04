package org.f0w.k2i.core.handler;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import lombok.val;
import org.f0w.k2i.core.model.entity.ImportProgress;
import org.f0w.k2i.core.util.HttpUtils;

import java.util.List;

public final class ConnectionCheckHandler extends MovieHandler {
    private final Config config;

    @Inject
    public ConnectionCheckHandler(Config config) {
        this.config = config;
    }

    /**
     * Repeatably sleeps the thread for specific timeout, until connection to IMDB site is available.
     *
     * @param importProgress Entity to handle
     * @param errors         List which fill with errors if occured
     */
    @Override
    protected void handleMovie(ImportProgress importProgress, List<Error> errors) {
        val url = "www.imdb.com";
        val port = 80;
        val timeout = config.getInt("timeout");

        while (!HttpUtils.isReachable(url, port, timeout)) {
            LOG.info("IMDB url is not reachable, retrying...");

            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignore) {
                return;
            }
        }

        LOG.info("IMDB url is successfully reached!");
    }
}
