import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    //context.system.shutdown
    Logger.info("Application shutdown...")
  }

}