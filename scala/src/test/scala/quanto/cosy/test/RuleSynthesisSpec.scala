package quanto.cosy.test

import quanto.cosy._
import org.scalatest.FlatSpec
import quanto.data._
import quanto.rewrite.{Matcher, Rewriter}
import quanto.util.json.Json

import scala.util.Random

/**
  * Created by hector on 28/06/17.
  */

class RuleSynthesisSpec extends FlatSpec {
  behavior of "Rule Synthesiser"

  val rg = Theory.fromFile("red_green")
  var emptyRuleList: List[Rule] = List()
  var diagramStream = ColbournReadEnum.enumerate(2, 2, 2, 2)
  var results = EquivClassRunAdjMat(numAngles = 2,
    tolerance = EquivClassRunAdjMat.defaultTolerance,
    rulesList = emptyRuleList,
    theory = rg)
  results.findEquivalenceClasses(diagramStream, "ColbournRead 2 2 2 2")

  it should "turn a class into rules" in {
    var ruleList = RuleSynthesis.graphEquivClassReduction[AdjMat](
      results.adjMatToGraph,
      results.equivalenceClasses.head,
      List()
    )
    println(ruleList.map(rule => "lhs = " + rule.lhs.toString + "\n" +
      "rhs = " + rule.rhs.toString + "\n"))
  }

  it should "make identity-wire rules" in {
    var idClass = results.equivalenceClasses.filter(e => e.centre.toString == "1 0 0 1")
    var ruleList = idClass.foldLeft(List[Rule]())(
      (a, b) => RuleSynthesis.graphEquivClassReduction[AdjMat](
        results.adjMatToGraph,
        b,
        List()
      ) ::: a
    )
    //println(idClass.head.members)
    var reductionRules = ruleList.filter(r => r.lhs > r.rhs)
    println(reductionRules.map(rule => "lhs = " + rule.lhs.toString + "\n" +
      "rhs = " + rule.rhs.toString + "\n"))
    assert(reductionRules.nonEmpty)
  }

  it should "discard reducible rules" in {
    var idClass = results.equivalenceClasses.filter(e => e.centre.toString == "1 0 0 1")
    var ruleList = idClass.foldLeft(List[Rule]())(
      (a, b) => RuleSynthesis.graphEquivClassReduction[AdjMat](
        results.adjMatToGraph,
        b,
        List()
      ) ::: a
    )
    var r1 = ruleList.head
    var m = Matcher.findMatches(r1.lhs, r1.lhs)
    var shrunkRules = RuleSynthesis.discardReducibleRules(rules = ruleList, seed = new Random(1))
    println(shrunkRules)
    assert(ruleList.length > shrunkRules.length)
  }

  behavior of "Bian and Perdrix 2Qubit system"

  it should "find small rules" in {
    var results = EquivClassRunBlockStack(1e-14)
    var rowsAllowed = BlockRowMaker(2, maxInOut = 2, BlockRowMaker.Bian2Qubit)
    var stacks = BlockStackMaker(2, rowsAllowed)
    stacks.foreach(s => results.add(s))
    results.equivalenceClassesNormalised
      .filter(cls => cls.members.map(x => x._1.toString).contains("( 1  x  1 )"))
      .foreach(cls => println(cls.members.map(x => x._1)))
  }

  behavior of "ZX Qutrit system"

  it should "find small rules" in {
    var results = EquivClassRunBlockStack(1e-14)
    var rowsAllowed = BlockRowMaker(1, maxInOut = 2, BlockRowMaker.ZXQutrit(3))
    var stacks = BlockStackMaker(2, rowsAllowed)
    stacks.foreach(s => results.add(s))
    results.equivalenceClassesNormalised
      // .filter(cls => cls.members.map(x => x._1.toString).contains("( 1  x  1 )"))
      .foreach(cls => println(cls.members.map(x => x._1)))
  }


  behavior of "ZX Qudit system"

  it should "find small rules" in {
    var results = EquivClassRunBlockStack(1e-14)
    var rowsAllowed = BlockRowMaker(1, maxInOut = 2, BlockRowMaker.ZXQudit(4,2))
    var stacks = BlockStackMaker(2, rowsAllowed)
    stacks.foreach(s => results.add(s))
    results.equivalenceClasses
      // .filter(cls => cls.members.map(x => x._1.toString).contains("( 1  x  1 )"))
      .foreach(cls => println(cls.members.map(x => x._1)))
  }

}