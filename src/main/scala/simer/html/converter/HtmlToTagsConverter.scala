package simer.html.converter

import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.html.TextArea
import org.scalajs.dom.raw.{DOMParser, NamedNodeMap, Node}

import com.raquo.laminar.api.L._
import org.scalajs.dom.{document, window}

import scala.scalajs.js.timers.setTimeout
import scala.util.Random
import com.raquo.laminar.api.L._


import scala.scalajs.js

object HtmlToTagsConverter {

  def main(args: Array[String]): Unit = {
    val template = HTMLTemplate.template

    documentEvents.onDomContentLoaded.foreach { _ =>
      val el = dom.document.getElementById("content")
      render(el, template)
    }(unsafeWindowOwner)
  }

  def runConverter(converterType: ConverterType): String => String = { (htmlCode: String) =>
    val parsedHtml = new DOMParser().parseFromString(htmlCode, "text/html")
    val rootChildNodes = removeGarbageChildNodes(parsedHtml)
    //having more then one HTML tree causes the DOMParser to generate an incorrect tree.
    val scalaCodes = rootChildNodes.map(toScalaTags(_, converterType))
    val scalaCode =
      if (scalaCodes.size > 1) {
        val fixMe = s"""//FIXME - MULTIPLE HTML TREES PASSED TO THE CONVERTER. THIS MIGHT GENERATE UNEXPECTED SCALATAGS CODE. Check <!DOCTYPE html> is not in the input HTML."""
        fixMe + "\n" + scalaCodes.mkString(", ")
      } else
        scalaCodes.mkString(", ")

    removeTagsFromScalaCode(htmlCode, scalaCode, "html", "head", "body")
  }

  def removeGarbageChildNodes(node: Node): List[Node] =
    node.childNodes.filterNot(isGarbageNode).toList

  def isGarbageNode(node: Node): Boolean =
    js.isUndefined(node) || node.nodeName == "#comment" || (node.nodeName == "#text" && node.nodeValue.trim.isEmpty)

  /**
    * Recursively generates the output Scalatag's code for each HTML node and it's children.
    *
    * Filters out comments and empty text's nodes (garbage nodes :D) from the input HTML before converting to Scala.
    */
  def toScalaTags(node: Node, converterType: ConverterType, nestingLevel: Int = 0): String = {
    //removes all comment and empty text nodes.
    val childrenWithoutGarbageNodes: Seq[Node] = removeGarbageChildNodes(node)

    val children = childrenWithoutGarbageNodes
      .map(toScalaTags(_, converterType, nestingLevel + 1))
      .mkString(",\n")

    toScalaTag(node, converterType, childrenWithoutGarbageNodes, children, nestingLevel)
  }

  /**
    * Converts a single HTML node, given it's child nodes's (@param children) already converted.
    */
  private def toScalaTag(node: Node,
                         converterType: ConverterType,
                         childrenWithoutGarbageNodes: Seq[Node],
                         children: String,
                         nestingLevel: Int): String = {

    val scalaAttrList = toScalaAttributes(attributes = node.attributes, converterType, nestingLevel + 1)

    val name = converterType.tagsRenames.get(node.nodeName.toLowerCase) match {
      case Some(value) => value
      case None => node.nodeName.toLowerCase
    }

    name match {
      case "#text" =>
        tripleQuote(node.nodeValue)

      case _ if scalaAttrList.isEmpty && children.isEmpty =>
        s"${pad(nestingLevel)}${converterType.nodePrefix + name}()"

      case _ =>
        val scalaAttrString =
          if (scalaAttrList.isEmpty)
            ""
          else if (!converterType.newLineAttributes)
            scalaAttrList.mkString("\n", ",\n", "")
          else
            scalaAttrList.mkString(", ")

        val data =
          if (children.isEmpty)
            ""
          else {
            //text child nodes can be a part of the same List as the attribute List. They don't have to go to a new line.
            val isChildNodeATextNode = childrenWithoutGarbageNodes.headOption.exists(_.nodeName == "#text")
            val commaMayBe = if (scalaAttrString.isEmpty) "" else ","
            val startNewLineMayBe = if (isChildNodeATextNode && (converterType.newLineAttributes || scalaAttrString.isEmpty)) "" else "\n"
            //add a newLine at the end if this node has more then one child nodes
            val endNewLineMayBe = if (isChildNodeATextNode && childrenWithoutGarbageNodes.size <= 1) "" else ""
            s"$commaMayBe$startNewLineMayBe$children$endNewLineMayBe"
          }

        s"${pad(nestingLevel)}${converterType.nodePrefix + name}($scalaAttrString$data)"

    }
  }

  private def pad(level: Int) = List.fill(level * 2)(" ").mkString("")

  def toScalaAttributes(attributes: NamedNodeMap,
                        converterType: ConverterType,
                        nestingLevel: Int): Iterable[String] =
    if (js.isUndefined(attributes) || attributes.isEmpty)
      List.empty
    else
      attributes.flatMap {
        case (attrKey, attrValue) =>
          val attrValueString = attrValue.value
          val escapedValue = tripleQuote(attrValueString)
          val attrs = attrKey match {
            case "style" =>
              val attributeKeyAndValue = attrValueString.split(";")
              attributeKeyAndValue.map {
                string =>
                  val styleKeyValue = string.split(":")
                  s"""customStyle("${styleKeyValue.head.trim}") := "${styleKeyValue.last.trim}""""
              }.toList

            case x if x.startsWith("data-") =>
              val after = x.stripPrefix("data-")
              List(s"""dataAttr("$after") <-- ???""")

            case x if converterType.attrRenames.contains(x) =>
              val key = converterType.attrRenames(x)
              List(s"""$key := "$attrValueString"""")

            case x if x.startsWith("on") => // events
              val capitalized = x.stripPrefix("on").capitalize
              List(s"on$capitalized --> ???")

            case _ =>
              val name = s"${converterType.attributePrefix}$attrKey"
              val upperCased =
                if (name.contains("-")) {
                  name.split("-").toList match {
                    case Nil => ""
                    case x :: Nil => x
                    case x :: xs => (x :: xs.map(_.capitalize)).mkString("")
                  }
                }
                else name
              List(s"$upperCased := $escapedValue")
          }
          attrs.map(x => s"${pad(nestingLevel)}$x")
      }

  /**
    * Javascript html parser seems to add <html>, <head> and <body> tags the parsed tree by default.
    * This remove the ones that are not in the input HTML.
    *
    * Might not work for tags other then html, head and body. Have not looked into others, didn't need them so far.
    */
  def removeTagsFromScalaCode(htmlCode: String, scalaCode: String, tagsToRemove: String*): String =
    tagsToRemove.foldLeft(scalaCode) {
      case (newScalaCode, tagToRemove) =>
        s"(?i)<$tagToRemove".r.findFirstMatchIn(htmlCode) match {
          case None =>
            val scalaCodeWithoutTag = newScalaCode.replaceFirst(s".*$tagToRemove.+", "").trim
            if (tagToRemove == "head") //If head if head is empty in html. Result Scalatag would be head() in Scala.
              scalaCodeWithoutTag
            else
              scalaCodeWithoutTag.dropRight(1) //remove the closing ')' for the tag if it's not head.

          case Some(_) =>
            newScalaCode
        }
    }

  def tripleQuote(string: String): String =
    string.trim match {
      case string if string.contains("\"") || string.contains("\n") || string.contains("\\") =>
        s"""\"\"\"$string\"\"\""""
      case string =>
        s""""$string""""
    }
}
