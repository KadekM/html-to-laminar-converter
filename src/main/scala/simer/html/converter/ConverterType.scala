package simer.html.converter


sealed trait ConverterType {
  val attributePrefix: String
  val nodePrefix: String
  val newLineAttributes: Boolean

  val attrRenames: Map[String, String]
  val tagsRenames: Map[String, String]
}

case object LaminarTagsConverter extends ConverterType {
  val attributePrefix: String = ""
  val nodePrefix: String = ""
  val newLineAttributes: Boolean = false

  val attrRenames: Map[String, String] = Map(
    "id" -> "idAttr",
    "list" -> "listId",
    "class" -> "cls",
    "for" -> "forId",
    "type" -> "tpe",
  )

  val tagsRenames: Map[String, String] = Map(
    "datalist" -> "dataList",
  )
}
