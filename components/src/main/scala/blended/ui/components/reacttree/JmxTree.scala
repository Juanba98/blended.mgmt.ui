package blended.ui.components.reacttree

import blended.jmx.JmxObjectName

sealed trait JmxNodeType

case object RootNode extends JmxNodeType
case class DomainNode(domain : String) extends JmxNodeType
case class ObjNameNode(name : JmxObjectName, parentName: JmxObjectName) extends JmxNodeType

object JmxTree extends ReactTree[JmxNodeType]

object JmxTreeHelper {

  private def findGroup(objName : JmxObjectName, names: List[JmxObjectName]): JmxObjectName = {

    def differentiatingKey(
      objName : JmxObjectName,
      names: List[JmxObjectName]
    ) : Option[String] = names match {
      case Nil => None
      case head :: tail =>
        if (head.properties.size == objName.properties.size) {
          head.differingKeys(objName) match {
            case k :: Nil => Some(k)
            case _ => differentiatingKey(objName, tail)
          }
        } else {
          differentiatingKey(objName, tail)
        }
    }

    differentiatingKey(objName, names.filterNot(_.equals(objName))) match {
      case None => objName
      case Some(k) => JmxObjectName(objName.domain, objName.properties.filterKeys(_ != k))
    }
  }

  private def nameNodes(names : List[JmxObjectName], parentName: JmxObjectName) : List[JmxTree.TreeNode] = names match {
    case Nil => Nil
    case head :: tail =>
      if (parentName.isParent(head)) {
        JmxTree.TreeNode(ObjNameNode(head, parentName)) :: nameNodes(tail, parentName)
      } else {

        val groupName : JmxObjectName = findGroup(head, names)

        val reducedGroup : JmxObjectName = groupName.differingKeys(parentName).tail.foldLeft(groupName){ case (c,k) =>
          JmxObjectName(c.domain, c.properties.filterKeys(_ != k))
        }

        val ancestor : JmxObjectName => Boolean = n => reducedGroup.isAncestor(n)
        val children = nameNodes(names.filter(ancestor), groupName)
        JmxTree.TreeNode(ObjNameNode(groupName, parentName), children) :: nameNodes(names.filterNot(ancestor), parentName)
      }
  }

  def treeModel(names : List[JmxObjectName]) : JmxTree.TreeNode = {

    val objNameNodes : List[JmxObjectName] => String => List[JmxTree.TreeNode] = n => d =>
      nameNodes(n.filter(_.domain == d), JmxObjectName(d, Map.empty))

    val domainNodes : List[JmxTree.TreeNode] = names.map(_.domain).distinct
      .map(d => JmxTree.TreeNode(DomainNode(d), objNameNodes(names)(d)))

    JmxTree.TreeNode(
      RootNode, domainNodes
    )
  }
}
