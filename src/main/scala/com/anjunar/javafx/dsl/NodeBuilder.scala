package com.anjunar.javafx.dsl

import com.anjunar.javafx.dsl.traits.HasNode
import javafx.scene.Node

trait NodeBuilder[N <: Node] extends ElementBuilder[N], HasNode