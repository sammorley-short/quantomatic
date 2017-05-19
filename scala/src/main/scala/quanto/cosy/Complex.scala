package quanto.cosy

/* Complex numbers for use in Tensor
 *
 */

class Complex(

               val re: Double,
               val im: Double) {
  override def toString =
    re + (if (im < 0) "-" + -im else "+" + im) + "i"

  def this(re: Double) = this(re, 0)

  def +(that: Complex) =
    new Complex(re + that.re, im + that.im)

  def *(that: Complex) =
    new Complex(re * that.re - im * that.im, re * that.im + im * that.re)

  def +(d: Double) = new Complex(re + d, im)

  def *(d: Double) = new Complex(re * d, im * d)


  override def equals(other: Any): Boolean =
    other match {
      case that: Complex =>
        (that canEqual this) &&
          this.re == that.re &&
          this.im == that.im
      case that: Double =>
        this.re == that
      case _ => false
    }

  def canEqual(other: Any): Boolean =
    other.isInstanceOf[Complex]

  override def hashCode(): Int = ((41 * this.im) + this.re).toInt
}

object Complex {
  val one = new Complex(1, 0)

  val zero = new Complex(0, 0)

  def apply(r: Double, i: Double) = new Complex(r,i)
  def apply(r: Int, i: Int) = new Complex(r,i)
}