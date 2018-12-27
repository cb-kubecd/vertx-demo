package com.github.jenkinsx.quickstarts.vertx.rest.prometheus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static io.vertx.core.Vertx.vertx;

public class DemoVerticle extends AbstractVerticle {

    public static final String CONTENT_TYPE = "application/json";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        exposeHelloWorldEndpoint(router);
        exposeHealthEndpoint(router);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        super.start(startFuture);
    }

    private void exposeHelloWorldEndpoint(Router router) {
        router.route("/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", CONTENT_TYPE);
            response.end(new JsonObject().put("Goodbye", "Cruel World".toLowerCase()).toBuffer());
        });
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
        vertx().deployVerticle(new DemoVerticle());
    }

}
