package quanto.layout

import quanto.data.{Derivation, DSName, VName}

import scala.collection.mutable

/// Builds a LiftedDStepLayoutDerivationLayout object containing the computed layout for a derivation.
/// Lifts a DStepLayoutStrategy to a DerivationLayoutStrategy.
class LiftedDStepLayoutDerivationLayoutStrategy extends DerivationLayoutStrategy {
  private[this] var strategy : DStepLayoutStrategy = null;
  private[this] var derivation : Derivation = null;

  /// Secondary constructor, which allows the DStepLayoutStrategy to be provided at construction.
  def this(strategy : DStepLayoutStrategy) {
    this();
    this.setStrategy(strategy)
  }

  /// Secondary constructor, which allows the DStepLayoutStrategy and Derivation to be provided at construction.
  def this(strategy : DStepLayoutStrategy, derivation : Derivation) {
    //this(derivation); // Secondary constructors cannot call superclass constructors
    this();
    this.setDerivation(derivation);
    this.setStrategy(strategy)
  }

  /// Provide a DStepLayoutStrategy to lift when computing LiftedDStepLayoutDerivationLayoutStrategy.layout().
  /// @param strategy the DStepLayoutStrategy to lift
  def setStrategy(strategy : DStepLayoutStrategy) : Unit = {
    this.strategy = strategy;
  }

  /// Provide a Derivation for which the layout can be computed with LiftedDStepLayoutDerivationLayoutStrategy.layout().
  /// @param derivation the Derivation that is to be laid out
  def setDerivation(derivation : Derivation) : Unit = {
    this.derivation = derivation;
  }

  /// Create an object of type LiftedDStepLayoutDerivationLayout containing the data of the new derivation layout.
  /// A Derivation must have been previously provided to the LiftedDStepLayoutDerivationLayoutStrategy (by constructing with a Derivation or by calling LiftedDStepLayoutDerivationLayoutStrategy.setLayout).
  /// @return a LiftedDStepLayoutDerivationLayout corresponding to the previously provided Derivation
  def layout() : DerivationLayout = {
    var layouts = new mutable.HashMap[Option[DSName], GraphLayoutData]();
    // Lay out each step, in breadth-first order.
    for (ds <- breadthFirstDSteps()) {
      val parentDSO = derivation.parentMap.get(ds);
      // Special case for first iteration, when no graph layout exists for the root of the derivation.
      if (parentDSO.isEmpty && !layouts.contains(parentDSO)) {
        strategy.setStep(derivation.steps(ds), derivation.root);
        layouts(Some(ds)) = strategy.layoutOutput();
        layouts(None) = strategy.layoutSource();
      }
      else {
        strategy.setStep(derivation.steps(ds), layouts(parentDSO).asGraph());
        layouts(Some(ds)) = strategy.layoutOutput();
      }
    }
    return new LiftedDStepLayoutDerivationLayout(derivation, layouts);
  }

  /// Get a list of the DStep names of a Derivation in breadth-first order, excluding the root node.
  /// @param derivation
  /// @return the DSNames in breadth-first order
  private[this] def breadthFirstDSteps() : mutable.Queue[DSName] = {
    val out = new mutable.Queue[DSName]()
    val q = new mutable.Queue[DSName]()
    q ++= derivation.firstSteps
    while (!q.isEmpty) {
      val ds = q.dequeue()
      out += ds
      q ++= derivation.children(ds) // This assumes that the children are unordered and that Derivation objects are trees (hence do not contain cycles and therefore will never revisit an already-seen vertex).
    }
    return out
  }
}

/// Represents the layout of all the graphs in a Derivation.
/// Users do not construct objects of this class directly, but use the LiftedDStepLayoutDerivationLayout instead.
class LiftedDStepLayoutDerivationLayout private extends DerivationLayout {
  private[this] var _derivation : Derivation = null
  private[this] var layouts : mutable.Map[Option[DSName], GraphLayoutData] = null

  /// Not part of the public interface. Constructs a LiftedDStepLayoutDerivationLayout.
  private[layout] def this(derivation : Derivation, layouts : mutable.Map[Option[DSName], GraphLayoutData]) = {
    this()
    this._derivation = derivation
    this.layouts = layouts
  }

  /// Get the layout's coordinates for the vertex named v in: the root graph if dso is None, or the graph generated by derivation step ds if dso is Some(ds)
  /// @param dso None to query the root graph, otherwise the DSName of the DStep generating the graph to query
  /// @param v the name of the vertex in the graph selected by dso
  /// @return the layout's coordinates for the queried vertex.
  def getCoords(dso : Option[DSName], v: VName) : (Double, Double) = {
    return layouts(dso).getCoords(v)
  }

  /// The Derivation that this layout was computed for.
  def derivation : Derivation = {
    return _derivation
  }
}
