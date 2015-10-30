package verticles
/**
 * @author Thomas Segismont
 */

void vertxStart() {
  vertx.eventBus().consumer("testSubject", { message ->
    Map body = message.body() as Map
    Thread.sleep(body.sleep as long)
    if (body.fail) {
      throw new ExpectedException()
    }
  })
}

class ExpectedException extends RuntimeException {}