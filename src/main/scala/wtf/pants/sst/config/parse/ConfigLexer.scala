package wtf.pants.sst.config.parse

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

object ConfigLexer extends RegexParsers {

  sealed trait Token

  case class IDENTIFIER(str: String) extends Token

  case class LITERAL(str: String) extends Token

  case class INDENTATION(spaces: Int) extends Token

  case object COLON extends Token

  case object EQUALS extends Token

  case object INDENT extends Token

  case object DEDENT extends Token

  case object KEY extends Token

  case object FILE extends Token

  override def skipWhitespace = true

  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def identifier: Parser[IDENTIFIER] = {
    "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }

  def literal: Parser[LITERAL] = {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      LITERAL(content)
    }
  }

  private def processIndentations(tokens: List[Token],
                                  indents: List[Int] = List(0)): List[Token] = {
    tokens.headOption match {

      // if there is an increase in indentation level, we push this new level into the stack
      // and produce an INDENT
      case Some(INDENTATION(spaces)) if spaces > indents.head =>
        INDENT :: processIndentations(tokens.tail, spaces :: indents)

      // if there is a decrease, we pop from the stack until we have matched the new level,
      // producing a DEDENT for each pop
      case Some(INDENTATION(spaces)) if spaces < indents.head =>
        val (dropped, kept) = indents.partition(_ > spaces)
        (dropped map (_ => DEDENT)) ::: processIndentations(tokens.tail, kept)

      // if the indentation level stays unchanged, no tokens are produced
      case Some(INDENTATION(spaces)) if spaces == indents.head =>
        processIndentations(tokens.tail, indents)

      // other tokens are ignored
      case Some(token) =>
        token :: processIndentations(tokens.tail, indents)

      // the final step is to produce a DEDENT for each indentation level still remaining, thus
      // "closing" the remaining open INDENTS
      case None =>
        indents.filter(_ > 0).map(_ => DEDENT)

    }
  }

  def indentation: Parser[INDENTATION] = {
    "\n[ ]*".r ^^ { whitespace =>
      val nSpaces = whitespace.length - 1
      INDENTATION(nSpaces)
    }
  }

  def colon = ":" ^^ (_ => COLON)

  def equals = "=" ^^ (_ => EQUALS)

  def file = "$FILE$" ^^ (_ => FILE)

  def key = "$KEY$" ^^ (_ => KEY)

  def tokens: Parser[List[Token]] = {
    phrase(rep1(colon | equals | file | key | literal | identifier | indentation)) ^^ { rawTokens =>
      processIndentations(rawTokens)
    }
  }


  trait ConfigCompilationError

  case class ConfigLexerError(msg: String) extends ConfigCompilationError

  def apply(code: String): Either[ConfigLexerError, List[Token]] = {
    parse(tokens, code) match {
      case NoSuccess(msg, next) => Left(ConfigLexerError(msg))
      case Success(result, next) => Right(result)
    }
  }

  class ConfigTokenReader(tokens: Seq[Token]) extends Reader[Token] {
    override def first: Token = tokens.head
    override def atEnd: Boolean = tokens.isEmpty
    override def pos: Position = NoPosition
    override def rest: Reader[Token] = new ConfigTokenReader(tokens.tail)
  }

  sealed trait ConfigAST
  case class AndThen(step1: ConfigAST, step2: ConfigAST) extends ConfigAST
  case class ReadInput(inputs: Seq[String]) extends ConfigAST
  case class CallService(serviceName: String) extends ConfigAST
  case class Choice(alternatives: Seq[ConditionThen]) extends ConfigAST
  case object Exit extends ConfigAST

  sealed trait ConditionThen { def thenBlock: ConfigAST }
  case class IfThen(predicate: Condition, thenBlock: ConfigAST) extends ConditionThen
  case class OtherwiseThen(thenBlock: ConfigAST) extends ConditionThen

  sealed trait Condition
  case class Equals(factName: String, factValue: String) extends Condition

}
