package com.anjunar.jcommander.commands

trait Command {
  
  def canExecute : Boolean
  
  def execute(): Unit

}
