package events

case class Request(session: Session, timestamp: Long, url: String)
