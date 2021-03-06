package com.prisma.api.database.mutactions.mutactions

import java.sql.SQLException

import com.prisma.api.database._
import com.prisma.api.database.mutactions.{ClientSqlDataChangeMutaction, ClientSqlStatementResult}
import com.prisma.api.mutations.NodeSelector
import com.prisma.api.schema.APIErrors
import com.prisma.gc_values.{NullGCValue, _}
import com.prisma.shared.models.Project

import scala.concurrent.Future

case class VerifyWhere(project: Project, where: NodeSelector) extends ClientSqlDataChangeMutaction {

  override def execute: Future[ClientSqlStatementResult[Any]] = {
    Future.successful(ClientSqlStatementResult(sqlAction = DatabaseMutationBuilder.whereFailureTrigger(project, where)))
  }

  override def handleErrors = {Some({ case e: SQLException if e.getErrorCode == 1242 && causedByThisMutaction(e.getCause.toString) => throw APIErrors.NodeNotFoundForWhereError(where)})}

  private def dateTimeFromISO8601(v: Any) = {
    val string = v.toString
    //"2017-12-05T12:34:23.000Z" to "2017-12-05T12:34:23.000" which MySQL will accept
    string.replace("Z", "")
  }
  
  def causedByThisMutaction(cause: String) = {
    val parameterString = where.fieldValue match {
      case StringGCValue(x) => s"parameters ['$x',"
      case IntGCValue(x) => s"parameters [$x,"
      case FloatGCValue(x) => s"parameters [$x,"
      case BooleanGCValue(false) => s"parameters [0,"
      case BooleanGCValue(true) => s"parameters [1,"
      case GraphQLIdGCValue(x) => s"parameters ['$x',"
      case EnumGCValue(x) => s"parameters ['$x',"
      case DateTimeGCValue(x) => s"parameters ['${dateTimeFromISO8601(x)}',"  // Todo
      case JsonGCValue(x) => s"parameters ['$x',"                             // Todo
      case ListGCValue(_) => sys.error("Not an acceptable Where")
      case RootGCValue(_) => sys.error("Not an acceptable Where")
      case NullGCValue => sys.error("Not an acceptable Where")
    }

    val modelString = s"`${where.model.name}` where `${where.field.name}` ="

  cause.contains(modelString) && cause.contains(parameterString)
  }
}
