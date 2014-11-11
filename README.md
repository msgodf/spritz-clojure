# spritz-clojure

This is a Clojure port of [therealjamper's JavaScript implementation](https://github.com/therealjampers/spritzjs) of Rivest and Schuldt's Spritz cipher.

The `hash`, `encrypt`, `decrypt`, `encrypt-with-iv`, and `decrypt-with-iv` high level functions are implemented.

The functions are written to mimic their presentation in the [original paper](http://people.csail.mit.edu/rivest/pubs/RS14.pdf), as is the case with the JavaScript implementation.

I still have a few things to work out about how best to manage the state of the cipher. At the moment most functions take the state and return a modified version (all of them are pure).

## Usage

```clojure
(hash message hash-length)
```

```clojure
(encrypt key plaintext)
```

```clojure
(decrypt key ciphertext)
```
