package com.anjunar.javafx.dsl

import scala.collection.mutable

class BuildContext {

  val stack: mutable.Stack[ElementBuilder[?]] = mutable.Stack()
  
}