package com.lncosie.ankorm

import kotlin.Int

enum class A(val atom:Expr)
{

}



open data class Atom():Expr()
open data class Number():Atom()
open data class Int():Number()
open data class Double():Number()

open data class Expr()
open data class Multi(val left:Expr,right:Expr):Expr()
open data class Div(val left:Expr,val right:Expr):Expr()
{

}
data class Add(var left:Expr,var right:Expr)
open data class Invoke():Expr()

fun case()
{
    val a=Add(Invoke(),Atom())
    var (left,right)=a

    when(a){
        is Add->{val (left,right) = a;left;right;}
        is Add->{val (left,right) = a;left;right;}

    }



}
