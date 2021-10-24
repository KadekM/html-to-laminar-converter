package simer.html.converter


sealed trait ConverterType {
  val attributePrefix: String
  val nodePrefix: String
  val newLineAttributes: Boolean

  val attrRenames: Map[String, String]
  val tagsRenames: Map[String, String]
  val nodeNamePrefixer: Map[String, String]

  def wordReplace(s: String): String
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

  val nodeNamePrefixer: Map[String, String] = Map(
    "svg" -> "svg.",
    "path" -> "svg."
  )

  // last phase
  private val wordReplace: Map[String, String] = Map(
    "strokelinecap" -> "strokeLineCap",
    "strokelinejoin" -> "strokeLineJoin"
  )
  def wordReplace(s: String): String = wordReplace.foldLeft(s) { case (acc,(from, to)) =>
    acc.replaceAll("(?i)".appendedAll(from), to)
  }
}
