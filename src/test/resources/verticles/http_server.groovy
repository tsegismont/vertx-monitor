package verticles

/**
 * @author Thomas Segismont
 */

void vertxStart() {
  def config = vertx.getOrCreateContext().config()
  httpServer = vertx.createHttpServer()
  httpServer.requestHandler({ req ->
    // Timer as artificial processing time
    vertx.setTimer(config.requestDelay as long, { handler ->
      req.response().setChunked(true).putHeader('Content-Type', 'text/plain').write(config.content as String).end()
    })
  }).listen(config.port as int, config.host as String, { res ->
  })
}
