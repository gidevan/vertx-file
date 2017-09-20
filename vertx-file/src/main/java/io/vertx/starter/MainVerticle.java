package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
    private static final String PATH = "/home/vano/projects/sandbox/vert.x/vertx-file/vertx-file/src/main/resources";

    @Override
    public void start(Future<Void> startFuture) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/").handler(this::indexHandler);
        router.get("/files").handler(this::listFiles);

        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8081);
        server
            .requestHandler(router::accept)
            .listen(portNumber, ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("HTTP server running on port " + portNumber);
                    startFuture.complete();
                } else {
                    LOGGER.error("Could not start a HTTP server", ar.cause());
                    startFuture.fail(ar.cause());
                }
            });
    }

    private void indexHandler(RoutingContext context) {
        context.response().end("Vertx-file index");
    }

    private void listFiles(RoutingContext context) {
        readFiles(context);
    }

    private void readFiles(RoutingContext context) {

        String path = PATH + "/files";
        vertx.fileSystem().readDir(path, null, ar -> {
            if(ar.succeeded()) {
                LOGGER.info("succeeded");
                List<String> files = ar.result();
                String res = files.stream().collect(Collectors.joining(", "));
                StringBuilder sb = new StringBuilder(res);
                List<Future> list = new ArrayList<Future>();
                for(String file :files) {
                    OpenOptions options = new OpenOptions();
                    vertx.fileSystem().open(file, options, far -> {
                        if(far.succeeded()) {
                            AsyncFile asyncFile = far.result();
                            asyncFile.close();
                            LOGGER.info(file + " opened");
                            sb.append(file + " opened");
                        } else {
                            LOGGER.info(file + " not opened");
                            sb.append(file + " not opened");
                        }
                    });
                }
                context.response().end(res);
            } else {
                LOGGER.error(ar.cause());
                context.response().end(ar.cause().getMessage());
            }
        });
    }

}
