# Alfred Manages Leave

### Status
> [![Build Status](https://travis-ci.org/NayabSiddiqui/alfred-manages-leave.svg?branch=master)](https://travis-ci.org/NayabSiddiqui/alfred-manages-leave)


## What is this?
A CQRS and EventSourced driven leave management api written using [Play](https://www.playframework.com/).
The backend store used for persisting Events is [EventStore](https://geteventstore.com/) and serves as the write model
for aggregates. The write model, i.e., `Command` side of `CQRS` is implemented using
[Akka Persistence](http://doc.akka.io/docs/akka/current/scala/persistence.html)
