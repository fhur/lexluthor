(ns lexluthor.core-test
  (:require [clojure.test :refer :all]
            [lexluthor.core :refer :all]
            [presto.core :refer :all]))

(def calc-tokens
  (deftokens
    [#"\A[0-9]+"    :DIGIT %]
    [#"\A[a-zA-Z]+" :VAR %]
    [#"\A[\+-\\*/]" :OP %]
    [#"\A\s+"       :WS " "]))

(def java-tokens
  (deftokens
    [#"\A\{" :OPEN-BRACKET %]
    [#"\A\}" :CLOSE-BRACKET %]
    [#"\A[0-9]+" :DIGITS %]
    [#"\A=" :ASSIGN %]
    [#"\A(int|float|long|char)" :PRIMITIVE (second %)]
    [#"\Aclass" :CLASS %]
    [#"\A[a-zA-Z]+" :IDENTIFIER %]
    [#"\A\s+" :WHITESPACE :ignore]))

(expected-when "calc-tokens should be a function that matches tokens" calc-tokens
 :when ["123 + 7"] = {:id :DIGIT :literal "123" :lexeme "123"}
 :when [" + 7"] = {:id :WS :literal " " :lexeme " "}
 :when ["+ 7"] = {:id :OP :literal "+" :lexeme "+"})

(expected-when "java-tokens should tokenize a tiny subset of the java lang" (comp :id java-tokens)
  :when ["{"] = :OPEN-BRACKET
  :when ["}"] = :CLOSE-BRACKET
  :when ["int"] = :PRIMITIVE
  :when ["long"] = :PRIMITIVE
  :when ["long x = 5"] = :PRIMITIVE
  :when ["x = 5"] = :IDENTIFIER
  :when ["= 5"] = :ASSIGN
  :when ["Foo"] = :IDENTIFIER)

(expected-when "tokenize should match all tokens" #(map :id (tokenize java-tokens %))
  :when ["long x = 123"] = [:PRIMITIVE :IDENTIFIER :ASSIGN :DIGITS]
  :when ["int foo = 1500"] = [:PRIMITIVE :IDENTIFIER :ASSIGN :DIGITS]
  :when ["class Foo {}"] = [:CLASS :IDENTIFIER :OPEN-BRACKET :CLOSE-BRACKET]
  :when ["{ float fuel = 10 }"] = [:OPEN-BRACKET :PRIMITIVE :IDENTIFIER :ASSIGN :DIGITS :CLOSE-BRACKET]
  :when ["int foo = class A {}"] = [:PRIMITIVE :IDENTIFIER :ASSIGN :CLASS :IDENTIFIER :OPEN-BRACKET :CLOSE-BRACKET])






