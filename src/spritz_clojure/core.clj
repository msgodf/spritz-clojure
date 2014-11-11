(ns spritz-clojure.core
  (:require [clojure.tools.logging :as log]))

;; core functions:
;;--------------------
;; initialize-state
;; absorb, requires absorb-byte
;; absorb-byte, requires absorb-nibble
;; absorb-nibble, requires shuffle, swap, and madd
;; absorb-stop, requires shuffle and madd

;; shuffle, requires whip and crush
;; whip, requires update, madd and gcd
;; crush, requires swap
;; squeeze, requires shuffle and drip
;; drip requires shuffle, update and output
;; update requires madd and swap
;; output requires madd

;; high-level functions:
;;------------------------
;; hash, requires initialize-state, absorb, absorb-state, squeeze
;; encrypt, requires key-setup, squeeze, and madd
;; decrypt, requires key-setup, squeeze, and msub
;; encrypt-with, requires key-setup, absorb-stop, absorb, squeeze, and madd
;; decrypt-with, requires key-setup, absorb-stop, absorb, squeeze, and msub
;; key-setup, requires initialize-state, and absorb

;; utility functions:
;;----------------------
;; madd
;; msub
;; low
;; high
;; swap
;; gcd
;; get-state

(def N 256)
(def N_MINUS_ONE (- N 1))
(def N_OVER_TWO_FLOOR (/ N 2))
(def TWO_N (* 2 N))

;; implement utility functions first
(defn madd
  [a b]
  (mod (+ a b) *N*))

(defn msub
  [a b]
  (madd *N* (- a b)))

(defn low
  [b]
  (bit-and b 0xf))

(defn high
  [b]
  (bit-and (bit-shift-right b 4)
           0xf))

(defn swap
  "Swap the values at positions p1 and p2 in the vector xs.

  Doesn't work on lazy sequences."
  [xs p1 p2]
  (let [value-at-p1 (nth xs p1)
        value-at-p2 (nth xs p2)]
    (-> xs
        (update-in [p1] (fn [_] value-at-p2))
        (update-in [p2] (fn [_] value-at-p1)))))

(defn gcd
  "I could use (.gcd (biginteger a) (biginteger b))"
  [a b]
  (->> (iterate (fn [[a b]] [b (mod a b)]) [a b])
       (take-while (fn [[_  b]] (not (zero? b))))
       (last)
       (last)))

;; now I can write update and output

(defn update
  "Update()
    1 i=i+w
    2 j=k+S[j+S[i]]
    3 k=i+k+S[j]
  4 Swap(S[i], S[j])"
  [{:keys [i j k w S] :as state}]
  (let [new-i (madd i w)
        new-j (madd k (nth S (madd j (nth S new-i))))
        new-k (madd (+ new-i k) (nth S new-j))
        new-S (swap S new-i new-j)]
    (merge state
           {:i new-i
            :j new-j
            :k new-k
            :S new-S})))


(defn output
  "Output()
    1 z = S[j+S[i+S[z+k]]]
    2 return z"
  [{:keys [S j i z k] :as state}]
  (merge state
         {:z (->> (madd z k)
                  (nth S)
                  (madd i)
                  (nth S)
                  (madd j)
                  (nth S))}))

;; now I can write whip and crush

(defn whip
  "Whip(r)
    1 for v=0 to r−1
    2   Update()
    3 do w=w+1
    4 until gcd(w,N)=1"
  [state r]
  ;; call update passing the result to itself, r times
  (let [state (nth (iterate update state) r)]
    (merge state
           {:w (first (drop-while (fn [w] (not= (gcd w *N*) 1))
                                  (rest (iterate (fn [x] (madd x 1))
                                                 (:w state)))))})))

(defn crush*
  [S v index]
  (if (> (nth S v)
         (nth S index))
    (swap S v index)
    S))

(defn crush
  "Crush()
    1 for v=0 to N/2−1
    2   if S[v]>S[N−1−v]
    3     Swap(S[v], S[N − 1 − v])"
  [{:keys [S] :as state}]
  (merge state
         {:S (reduce (fn [s v] (crush* s v (- *N_MINUS_ONE* v)))
                      S
                      (range 0 *N_OVER_TWO_FLOOR*))}))


;; now that I have whip and crush, I can write shuffle

(defn shuffle
  "Shuffle()
    1 Whip(2N)
    2 Crush()
    3 Whip(2N)
    4 Crush()
    5 Whip(2N)
    6 a=0"
  [state]
  (merge state
         (-> state
             (whip *TWO_N*)
             (crush)
             (whip *TWO_N*)
             (crush)
             (whip *TWO_N*))
         {:a 0}))

;; and with shuffle, I can write drip

(defn drip
  "Drip()
    1 if a>0
    2   Shuffle()
    3 Update()
    4 return Output()"
  [state]
  (let [state (->> (if (pos? (:a state)) (shuffle state) state)
                   (update)
                   (output))]
    [state (:z state)]))


;; and with drip, I can write squeeze

(defn squeeze
  "Squeeze(r)
    1 if a>0
    2   Shuffle()
    3 P = Array.New(r)
    4 for v=0 to r−1
    5   P[v] = Drip()
    6 return P"
  [state r]
  (->> (if (pos? (:a state)) (shuffle state) state)
       (iterate (fn [state] (first (drip state))))
       (rest) ;; drop the initial state
       (take r)
       (map :z)))


;; will need to create the initial state

(defn initialize-state
  "InitializeState(N)
    1 i = j = k = z = a = 0
    2 w = 1
    3 for v = 0 to N - 1
    4   S[v] = v"
  []
  {:i 0
   :j 0
   :k 0
   :z 0
   :a 0
   :w 1
   :S (into [] (range *N*))})

;; before I can implement any of the higher level functions, I need to write the absorb functions

(defn apply-swap
  "A helper function for absorb-nibble. Swaps the values at a and (N/2+x) in S (inside the state)."
  [state x]
  (merge state
         {:S (swap (:S state)
                   (:a state)
                   (madd *N_OVER_TWO_FLOOR* x))}))

(defn inc-a
  "A helper function for absorb-nibble. Increments a mod N inside the state."
  [state]
  (update-in state [:a] (partial madd 1)))

(defn absorb-nibble
  "AbsorbNibble(x)
    1 if a = N/2
    2   Shuffle()
    3 Swap(S[a],S[N/2+x])
  4 a=a+1"
  [state x]
  (-> (if (== (:a state)
              *N_OVER_TWO_FLOOR*) (shuffle state) state)
      (apply-swap x)
      (inc-a)))

(defn absorb-byte
  "AbsorbByte(b)
    1 AbsorbNibble(low(b))
    2 AbsorbNibble(high(b))"
  [state b]
  (-> state
      (absorb-nibble (low b))
      (absorb-nibble (high b))))

(defn absorb
  "Absorb(I)
    1 for v = 0 to I.length - 1
    2   AbsorbByte(I[v])"
  [state I]
  (reduce (fn [s v] (absorb-byte s v)) state I))

(defn absorb-stop
  "AbsorbStop()
    1 if a=N/2
    2   Shuffle()
    3 a=a+1"
  [state]
  (-> (if (== (:a state)
              *N_OVER_TWO_FLOOR*) (shuffle state) state)
      (inc-a)))

;; Finally, this is enough to write hash

(defn hash
  "Hash(M,r)
  1 InitializeState()
  2 Absorb(M ); AbsorbStop()
  3 Absorb(r)
  4 return Squeeze(r)"
  [M r]
  (-> (initialize-state)
      (absorb M)
      (absorb-stop)
      (absorb [(bit-and r 0xff)])
      (squeeze r)))
