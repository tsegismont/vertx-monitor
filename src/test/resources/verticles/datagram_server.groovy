package verticles
/**
 * @author Thomas Segismont
 */

void vertxStart() {
  def config = vertx.getOrCreateContext().config()
  vertx.createDatagramSocket().listen(config.port as int, config.host as String, { res ->
  })
}
