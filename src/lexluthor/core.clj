(ns lexluthor.core)

(defmacro token-matcher
  [string & forms]
  (if (empty? forms)
    nil
    (let [[regex id lex-body] (first forms)]
      `(if-let [~'% (re-find ~regex ~string)]
        {:id ~id
         :lexeme ~lex-body
         :literal (if (coll? ~'%) (first ~'%) ~'%)}
        (token-matcher ~string ~@(rest forms))))))

(defmacro deftokens
  [& forms]
  `(fn [string#]
    (token-matcher string# ~@forms)))

(defn tokenize
  [tokenize-chunk string]
  (loop [string string
         tokens []]
    (if (empty? string)
      tokens
      (let [token (tokenize-chunk string)
            {literal :literal lexeme :lexeme} token]
        (recur (.substring string (count literal))
               (if (= :ignore lexeme)
                 tokens
                 (conj tokens token)))))))

