(ns lexluthor.core
  (:require [clojure.string :refer [upper-case lower-case]]))

(defmacro token-matcher
  [string & forms]
  (if (empty? forms)
    nil
    (let [[regex id lex-body] (first forms)]
      `(if-let [~'% (re-find ~regex ~string)]
        {:id ~id
         :lexeme ~(if (nil? lex-body) '% lex-body)
         :literal (if (coll? ~'%) (first ~'%) ~'%)}
        (token-matcher ~string ~@(rest forms))))))

(defmacro deftokens
  "Token definition macro.
  Syntax:
  (deftokens
    [regex1 id1 body1]
    ...
    [regexN idN bodyN])

  Usage:
  deftokens is used to define a set of tokens by mapping a regular
  expression to a token. deftokens returns a function which takes a
  string as input and attempts to match the string agains every
  single regex until a match is found.
  When the match is found the body will be evaled. If no body is supplied
  then the lexeme will be the matched string. You can use :ignore as body
  if you wish that token to be ignored by the `tokenize` function.

  Example:
  (deftokens
    [#\"\\A\\{\" :OPEN-BRACKET ]
    [#\"\\A\\}\" :CLOSE-BRACKET ]
    [#\"\\A[0-9]+\" :DIGITS ]
    [#\"\\A=\" :ASSIGN ]
    [#\"\\A(int|float|long|char)\" :PRIMITIVE (second %)]
    [#\"\\Aclass\" :CLASS ]
    [#\"\\A[a-zA-Z]+\" :IDENTIFIER ]
    [#\"\\A\\s+\" :WHITESPACE :ignore])"
  [& forms]
  `(fn [string#]
    (token-matcher string# ~@forms)))

(defn- count-new-lines
  [string]
  (reduce (fn [new-lines c]
            (if (= \newline c)
              (inc new-lines)
              new-lines))
          0 string))

(defn tokenize
  "Takes a token definition function (as created by deftokens) and a string
  and returns a list of tokens by matching the whole string using a consecutive application
  of the tokens-definition function."
  [tokens-definition string]
  (loop [string string
         tokens []
         line-num 0]
    (if (empty? string)
      tokens
      (let [token-match (tokens-definition string)
            token (assoc token-match :line line-num)
            {literal :literal lexeme :lexeme id :id} token]
        (if (nil? token-match)
          {:error (str "Unexpected token '" string "' at line: " line-num)}
          (recur (.substring string (count literal))
                 (if (or (= :ignore lexeme) (= :ignore id))
                   tokens
                   (conj tokens token))
                 (+ line-num (count-new-lines literal))))))))

(defn is-error
  [tokenization]
  (string? (:error tokenization)))

(defmacro declare-is-fn
  "Expands to a function which takes a token as input and returns true
  if the token matches the given token id.
  Example: (declare-is-fn :FOO)
  Defines the is-foo function which works as follows:
  user=> (is-foo {:id :FOO})
  true
  user=> (is-foo {:id :NOT-FOO})
  false"
  [token-id]
  `(defn ~(symbol (str "is-" (lower-case (name token-id))))
     [token#]
     (= ~(->> (name token-id)
              upper-case
              keyword)
        (:id token#))))

(defmacro declare-is-fns
  "Similar to declare-is-fn but declares several functions at the same
  time (wrapped by a do block"
  [& token-ids]
  (cons `do
        (for [token-id token-ids]
          `(declare-is-fn ~token-id))))

