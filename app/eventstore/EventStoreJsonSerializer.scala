package eventstore

import java.nio.ByteBuffer
import java.nio.charset.Charset

import akka.actor.{ActorRef, ExtendedActorSystem}
import akka.persistence.eventstore.EventStoreSerializer
import akka.persistence.eventstore.snapshot.EventStoreSnapshotStore.SnapshotEvent
import akka.persistence.eventstore.snapshot.EventStoreSnapshotStore.SnapshotEvent.Snapshot
import akka.persistence.{PersistentRepr, SnapshotMetadata}
import event._
import org.joda.time.DateTime
import org.json4s.Extraction.decompose
import org.json4s.JsonAST.{JField, JString}
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

import scala.collection.generic.SeqFactory

class EventStoreJsonSerializer(val system: ExtendedActorSystem) extends EventStoreSerializer {

  import EventStoreJsonSerializer._

  implicit val formats = DefaultFormats + SnapshotSerializer + new PersistentReprSerializer(system) + ActorRefSerializer ++ JodaTimeSerializers.all + new EmployeeEventSerializer

  def identifier = Identifier

  def includeManifest = true

  def fromBinary(bytes: Array[Byte], manifestOpt: Option[Class[_]]) = {
    implicit val manifest = manifestOpt match {
      case Some(x) => Manifest.classType(x)
      case None => Manifest.AnyRef
    }

    read(new String(bytes, UTF8))
  }

  def toBinary(o: AnyRef) = write(o).getBytes(UTF8)


  def toEvent(x: AnyRef) = x match {
    case x: PersistentRepr => {
      val payload = write(x.payload.asInstanceOf[EmployeeEvent])
      val eventData = EventData(
        eventType = classFor(x).getName,
        data = Content(ByteString(payload), ContentType.Json),
        metadata = Content(ByteString(toBinary(x)), ContentType.Json)
      )
      eventData
    }

    case x: SnapshotEvent => EventData(
      eventType = classFor(x).getName,
      data = Content(ByteString(toBinary(x)), ContentType.Json)
    )

    case _ => sys.error(s"Cannot serialize $x, SnapshotEvent expected")
  }

  def fromEvent(event: Event, manifest: Class[_]) = {
    val clazz: Class[_] = classOf[PersistentRepr]
    val result = fromBinary(event.data.metadata.value.toArray, clazz)
    if (manifest.isInstance(result)) result
    else sys.error(s"Cannot deserialize event as $manifest, event: $event")
  }

  def classFor(x: AnyRef) = x match {
    case x: PersistentRepr => x.payload match {
      case e: EmployeeEvent => x.payload.getClass
      case _ => classOf[PersistentRepr]
    }
    case _ => x.getClass
  }

  object ActorRefSerializer extends Serializer[ActorRef] {
    val Clazz = classOf[ActorRef]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), JString(x)) => system.provider.resolveActorRef(x)
    }

    def serialize(implicit format: Formats) = {
      case x: ActorRef => JString(x.path.toSerializationFormat)
    }
  }

}

object EventStoreJsonSerializer {
  val UTF8: Charset = Charset.forName("UTF-8")
  val Identifier: Int = ByteBuffer.wrap("json4s".getBytes(UTF8)).getInt

  object SnapshotSerializer extends Serializer[Snapshot] {
    val Clazz = classOf[Snapshot]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), JObject(List(
      JField("data", JString(x)),
      JField("metadata", metadata)))) => Snapshot(x, metadata.extract[SnapshotMetadata])
    }

    def serialize(implicit format: Formats) = {
      case Snapshot(data, metadata) => JObject("data" -> JString(data.toString), "metadata" -> decompose(metadata))
    }
  }

  class EmployeeEventSerializer extends CustomSerializer[EmployeeEvent](format => ( {

    case JObject(List(JField("email", JString(email)), JField("givenName", JString(givenName)))) =>
      EmployeeRegistered(email, givenName)
    case JObject(List(JField("creditedLeaves", JDouble(creditedLeaves)))) =>
      LeavesCredited(creditedLeaves.toFloat)
    case JObject(List(JField("applicationId", JString(applicationId)), JField("from", JString(from)), JField("to", JString(to)), JField("isHalfDay", JBool(isHalfDay)))) =>
      LeavesApplied(applicationId, DateTime.parse(from), DateTime.parse(to), isHalfDay = isHalfDay)
  }, {
    case EmployeeRegistered(email, givenName) =>
      JObject(List(JField("email", JString(email)), JField("givenName", JString(givenName))))
    case LeavesCredited(creditedLeaves) =>
      JObject(List(JField("creditedLeaves", JDouble(creditedLeaves.toDouble))))
    case LeavesApplied(applicationId, from, to, isHalfDay) =>
      JObject(List(JField("applicationId", JString(applicationId)), JField("from", JString(from.toString)), JField("to", JString(to.toString)), JField("isHalfDay", JBool(isHalfDay))))
  }))

  class PersistentReprSerializer(system: ExtendedActorSystem) extends Serializer[PersistentRepr] {
    val Clazz = classOf[PersistentRepr]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), json) =>

        val x = json.extract[Mapping]

        PersistentRepr(
          payload = x.payload,
          sequenceNr = x.sequenceNr,
          persistenceId = x.persistenceId,
          manifest = x.manifest,
          writerUuid = x.writerUuid
        )
    }

    def serialize(implicit format: Formats) = {
      case x: PersistentRepr =>
        val payload = x.payload match {
          case e: EmployeeEvent => e
        }
        val mapping = Mapping(
          payload = payload,
          sequenceNr = x.sequenceNr,
          persistenceId = x.persistenceId,
          manifest = x.payload.getClass.getName,
          writerUuid = x.writerUuid
        )
        decompose(mapping)
    }
  }

  case class Mapping(
                      payload: EmployeeEvent,
                      sequenceNr: Long,
                      persistenceId: String,
                      manifest: String,
                      writerUuid: String
                    )

}
