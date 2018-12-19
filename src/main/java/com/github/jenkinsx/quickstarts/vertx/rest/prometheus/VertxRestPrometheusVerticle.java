package com.github.jenkinsx.quickstarts.vertx.rest.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static io.prometheus.client.Counter.build;
import static io.vertx.core.Vertx.vertx;

public class VertxRestPrometheusVerticle extends AbstractVerticle {

    private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    private final Counter helloCounter = build("hello_count", "Number of incoming requests to /hello endpoint.").
                    register(registry);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        exposeHelloWorldEndpoint(router);
        exposeMetricsEndpoint(router);
        exposeHealthEndpoint(router);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        super.start(startFuture);
    }

    private void exposeHelloWorldEndpoint(Router router) {
        router.route("/hello").handler(routingContext -> {
            helloCounter.inc();
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            response.end(new JsonObject().put("hello", "world").toBuffer());
        });
    }

    private void exposeMetricsEndpoint(Router router) {
        DefaultExports.initialize();
        router.route("/metrics").handler(new MetricsHandler(registry));
    }

    private void exposeHealthEndpoint(Router router) {
        router.route("/actuator/health").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");
            response.end("OK");
        });
    }

    // IDE testing helper

    public static void main(String[] args) {
        vertx().deployVerticle(new VertxRestPrometheusVerticle());
    }

}