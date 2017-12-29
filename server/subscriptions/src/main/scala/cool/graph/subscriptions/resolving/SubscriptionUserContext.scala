package cool.graph.subscriptions.resolving

import cool.graph.shared.models.Project
import sangria.ast.Document

case class SubscriptionUserContext(
    nodeId: String,
    project: Project,
    requestId: String,
    clientId: String,
    log: Function[String, Unit],
    queryAst: Option[Document] = None
)
