package events

object EventStreamSample extends App {

  val stream = new EventStream(5)

  for {
    i <- 1 to 20
    requests = stream.tick
  } println(requests)
}
