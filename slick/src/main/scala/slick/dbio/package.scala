package slick

/** The `dbio` package contains the Database I/O Action implementation.
  * See [[DBIOAction]] for details. */
package object dbio {
  /** Simplified type for a streaming [[DBIOAction]] without effect tracking */
  type StreamingDBIO[+R, +T] = DBIOAction[Streaming[T], Effect.All, R]

  /** Simplified type for a [[DBIOAction]] without streaming or effect tracking */
  type DBIO[+R] = DBIOAction[NoStream, Effect.All, R]
  val DBIO = DBIOAction
}
