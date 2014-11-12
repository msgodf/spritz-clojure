# spritz-clojure

This is a Clojure port of [therealjamper's JavaScript implementation](https://github.com/therealjampers/spritzjs) of Rivest and Schuldt's Spritz cipher.

The `hash`, `encrypt`, `decrypt`, `encrypt-with-iv`, and `decrypt-with-iv` high level functions are implemented.

The functions are written to mimic their presentation in the [original paper](http://people.csail.mit.edu/rivest/pubs/RS14.pdf), as is the case with the JavaScript implementation.

I still have a few things to work out about how best to manage the state of the cipher. At the moment most functions take the state and return a modified version (all of them are pure).

## Usage

All of these examples can be run from a Clojure REPL assuming that spritz-clojure has been `require`d with the alias `spritz` (i.e. with `(require '[spritz-clojure.core :as spritz])`).

```clojure
(let [message (map byte "ABC")
      hash-length 32]
    (spritz/hash message hash-length))
```

```clojure
(let [key [115 101 116 101 99 45 97 115 116 114 111 110 111 109 121]
      message [84 104 97 110 107 32 121 111 117 32 102 111 114 32 100 101 99 111 100 105 110 103 32 116 104 105 115 32 112 108 97 105 110 116 101 120 116 32 97 116 32 108 101 97 115 116 44 32 73 32 104 111 112 101 32 121 111 117 32 119 105 108 108 32 116 114 121 32 115 112 114 105 116 122 106 115 33]]
    (spritz/encrypt key message))
```

```clojure
(let [key [115 101 116 101 99 45 97 115 116 114 111 110 111 109 121]
      message [84 104 97 110 107 32 121 111 117 32 102 111 114 32 100 101 99 111 100 105 110 103 32 116 104 105 115 32 112 108 97 105 110 116 101 120 116 32 97 116 32 108 101 97 115 116 44 32 73 32 104 111 112 101 32 121 111 117 32 119 105 108 108 32 116 114 121 32 115 112 114 105 116 122 106 115 33]]
    (->> message
         (spritz/encrypt key)
         (spritz/decrypt key)))
```
