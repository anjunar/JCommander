package com.anjunar.javafx.dsl

import com.anjunar.javafx.dsl.traits.IsNode
import javafx.scene.Node

trait NodeBuilder[N <: Node] extends ElementBuilder[N], IsNode