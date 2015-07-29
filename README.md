# lexluthor

A simple lexical analysis engine.

[![Clojars Project](http://clojars.org/lexluthor/latest-version.svg)](http://clojars.org/lexluthor)

## Usage

To tokenize a string you first need to define the list of possible
tokens using the `deftokens` macro.

```clojure
(def calculator-tokens
  (deftokens
    [#"\A[0-9]+"    :DIGIT]
    [#"\A[a-zA-Z]+" :VAR]
    [#"\A[\+-\\*/]" :OP]
    [#"\A\s+"       :WS :ignore]))
```

`deftokens` takes a list of `regex id body` vectors as argument and returns a
function which takes a string as argument and returns the first token
that matches.

```clojure
user=> (calculator-tokens "500 + 1")
{:lexeme "500", :id :DIGIT, :literal "500"}
```

To match a whole string of text to a list of tokens use the `tokenize`
function as follows:

```clojure
user=> (tokenize calculator-tokens "123 + 7 * 2")
[{:lexeme "123", :id :DIGIT, :literal "123"}
 {:lexeme "+", :id :OP, :literal "+"}
 {:lexeme "7", :id :DIGIT, :literal "7"}
 {:lexeme "*", :id :OP, :literal "*"}
 {:lexeme "2", :id :DIGIT, :literal "2"}]
```

### Error Handling
If the there is a string that does not match to any token then an error
object is returned. The error object has the following form:

```clojure
[:error "There was an error ..."]
```

## License

Copyright © 2015 Fernando Hurtado

Distributed under the Eclipse Public License either version 1.0.
