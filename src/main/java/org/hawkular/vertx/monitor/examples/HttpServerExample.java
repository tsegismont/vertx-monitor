/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.vertx.monitor.examples;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public class HttpServerExample {

    public static void main(String[] args) {
        VertxMonitorOptions vertxMonitorOptions = new VertxMonitorOptions();
        vertxMonitorOptions.setEnabled(true);

        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setMetricsOptions(vertxMonitorOptions);

        Vertx vertx = Vertx.vertx(vertxOptions);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(req -> {
            req.response().setChunked(true).putHeader("Content-Type", "text/plain").write("some text").end();
        });
        httpServer.listen(8080);
    }
}
