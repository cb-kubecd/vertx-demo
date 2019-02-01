package com.github.jenkinsx.quickstarts.vertx.rest.prometheus;

import static io.vertx.core.Vertx.vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DemoVerticleTest {

    @Test
    public void stuff(TestContext context) {
        DemoVerticle.main(null);
        HttpClient client = vertx().createHttpClient();
        assert200(client, context, "/hello");
        assert200(client, context, "/actuator/health");
    }
    
    private void assert200(HttpClient client, TestContext context, String uri) {
        Async async = context.async();
        HttpClientRequest req = client.get(8080, "localhost", uri);
        req.exceptionHandler(err -> context.fail(err.getMessage()));
        req.handler(resp -> {
            context.assertEquals(200, resp.statusCode());
            async.complete();
        });
        req.end();
    }

    @Test
    public void flaky(TestContext context) {
        assert Math.random() < 0.2 : "oops";
    }

}
