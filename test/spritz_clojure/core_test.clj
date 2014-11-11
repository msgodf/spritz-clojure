(ns spritz-clojure.core-test
  (:use [midje.sweet])
  (:require [spritz-clojure.core :as spritz]))

(facts "About spritz"

       (fact "initialize-state should be correct"
             (spritz/initialize-state) => (contains {:a 0
                                                     :w 1
                                                     :i 0
                                                     :j 0
                                                     :k 0
                                                     :S (range 0 256)}))

       (fact "state should be changed by absorb"
             (-> (spritz/initialize-state)
                 (spritz/absorb [0 1 2 3])) =not=> (contains {:S (range 0 256)}))

       (fact "absorb-nibble absorbs 1 correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb-nibble 1)) => (contains {:a 1
                                                         :w 1
                                                         :S (concat [129]
                                                                    (range 1 129)
                                                                    [0]
                                                                    (range 130 256))}))

       (fact "absorb-nibble absorbs 1 then 4 correctly"

             (-> (spritz/initialize-state)
                 (spritz/absorb-nibble 1)
                 (spritz/absorb-nibble 4)) => (contains {:a 2
                                                         :w 1
                                                         :S (concat [129 132]
                                                                    (range 2 129)
                                                                    [0 130 131 1]
                                                                    (range 133 256))}))

       (fact "absorb-byte absorbs 65 correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb-byte 65)) => (contains {:a 2
                                                        :w 1
                                                        :S (concat [129 132]
                                                                   (range 2 129)
                                                                   [0 130 131 1]
                                                                   (range 133 256))}))

       (fact "absorb-byte absorbs 65 then 66 correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb-byte 65)
                 (spritz/absorb-byte 66)) => (contains {:a 4 :w 1 :S (concat [129 132 130 1]
                                                                             (range 4 129)
                                                                             [0 2 131 3]
                                                                             (range 133 256))}))

       (fact "absorb-byte absorbs 65 then 66 then 67 correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb-byte 65)
                 (spritz/absorb-byte 66)
                 (spritz/absorb-byte 67)) => (contains {:a 6
                                                        :w 1
                                                        :S (concat [129 132 130 1 131 3]
                                                                   (range 6 129)
                                                                   [0 2 4 5]
                                                                   (range 133 256))}))
       (fact "absorb absorbs 'ABC' correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb [65 66 67])) => (contains {:a 6
                                                           :w 1
                                                           :S (concat [129 132 130 1 131 3]
                                                                      (range 6 129)
                                                                      [0 2 4 5]
                                                                      (range 133 256))}))

       (fact "whip works correctly after 'ABC' is absorbed"
             (-> (spritz/initialize-state)
                 (spritz/absorb [65 66 67])
                 (spritz/whip spritz/TWO_N)) => (contains {:a 6
                                                           :k 139
                                                           :i 0
                                                           :j 48
                                                           :w 3
                                                           :z 0
                                                           :S [107,117,173,5,68,101,84,146,17,197,109,227,181,56,244,231,162,239,171,122,167,218,52,196,11,24,124,71,172,114,48,202,65,140,123,246,211,228,178,23,192,6,194,102,166,247,176,106,45,249,250,54,238,251,28,73,222,131,82,234,133,200,135,157,41,94,203,168,112,219,43,89,47,18,163,74,7,215,221,179,253,79,204,149,220,32,184,19,130,209,116,86,34,36,208,148,132,30,53,129,0,62,39,88,188,144,55,21,127,15,174,67,190,254,50,230,152,138,92,160,44,69,115,128,111,93,139,156,4,72,118,13,214,134,233,252,108,236,113,78,90,147,161,145,104,240,91,121,193,95,3,243,35,110,103,151,206,76,99,20,98,63,136,77,159,97,229,1,33,255,96,120,242,25,9,37,42,10,143,61,29,66,182,46,26,169,12,100,216,195,226,217,59,51,81,119,248,165,49,16,245,241,186,126,183,105,85,70,87,235,205,60,27,185,58,191,170,189,40,125,158,64,150,83,210,141,224,2,177,57,212,142,237,232,8,38,153,137,187,14,207,199,75,80,155,213,198,225,154,223,201,31,22,175,180,164]}))

       (fact "update works having 'ABC' being absorbed correctly"
             (-> (spritz/initialize-state)
                 (spritz/absorb [65 66 67])
                 (spritz/update)) => (contains {:a 6
                                                :i 1
                                                :k 4
                                                :j 5
                                                :w 1
                                                :S (concat [129 3 130 1 131 132]
                                                           (range 6 129)
                                                           [0 2 4 5]
                                                           (range 133 256))}))

       (fact "drip should return the expected bytes having absorbed 'ABC'"
             (->> (spritz/absorb (spritz/initialize-state)
                                 (into [] (map byte "ABC")))

                  ;; repeatedly apply drip to get 8 bytes
                  (iterate (fn [state] (first (spritz/drip state))))
                  (drop 1)
                  (take 8)
                  (map :z)) => [0x77 0x9a 0x8e 0x01 0xf9 0xe9 0xcb 0xc0])

       (fact "drip should return the expected bytes having absorbed 'spam'"
             (->> (spritz/absorb (spritz/initialize-state)
                                 (into [] (map byte "spam")))

                  ;; repeatedly apply drip to get 8 bytes
                  (iterate (fn [state] (first (spritz/drip state))))
                  (drop 1)
                  (take 8)
                  (map :z)) => [0xf0 0x60 0x9a 0x1d 0xf1 0x43 0xce 0xbf])

       (fact "squeeze should return the expected leading hash bytes having absorbed message 'ABC'"
             (take 8 (-> (spritz/initialize-state)
                         (spritz/absorb (map byte "ABC"))
                         (spritz/absorb-stop)
                         (spritz/absorb [(bit-and 0x20 0xff)])
                         (spritz/squeeze (int 0x20)))) => [0x02 0x8f 0xa2 0xb4 0x8b 0x93 0x4a 0x18])

       (fact "hash should return the same result as the initialize-state/absorb/absorb-stop/squeeze steps with 'ABC'"
             (take 8
                   (spritz/hash (map byte "ABC")
                                0x20)) => [0x02 0x8f 0xa2 0xb4 0x8b 0x93 0x4a 0x18])

       (fact "encrypt should return a ciphertext C of the same length as the plaintext M"
             (spritz/encrypt [65 66 67] [68 69 70]) => (three-of number?))

       (fact "encrypt should not return the same message that went in"
             (spritz/encrypt [65 66 67] [68 69 70]) =not=> [68 69 70])

       (fact "encrypt should return same the result as the JavaScript implementation"
             (spritz/encrypt [66 67 68] [1 2 3 4 5 6 7]) => [27 29 243 162 65 4 218])

       (fact "decrypt should return a plaintext M of the same length as the ciphertext C"
             (spritz/decrypt [65 66 67] [68 69 70]) => (three-of number?))

       (fact "decrypt should not return the same message that went in"
             (spritz/decrypt [65 66 67] [68 69 70]) =not=> [68 69 70])

       (fact "decrypt should return same the result as the JavaScript implementation"
             (spritz/decrypt [66 67 68] [27 29 243 162 65 4 218]) => [1 2 3 4 5 6 7])

       (fact "decrypt(K, encrypt(K, M) should equal the original M"
             (->> [1 2 3 4 5 6 7]
                  (spritz/encrypt [66 67 68])
                  (spritz/decrypt [66 67 68])) => [1 2 3 4 5 6 7])

       (fact "decrypt(K1, encrypt(K2, M) should not equal the original M"
             (->> [1 2 3 4 5 6 7]
                  (spritz/encrypt [66 67 68])
                  (spritz/decrypt [66 67 70])) =not=> [1 2 3 4 5 6 7])

       (fact "encrypt-with-iv should return a ciphertext C of the same length as the plaintext M"
             (spritz/encrypt-with-iv [65 66 67] [1 2 3] [68 69 70]) => (three-of number?))

       (fact "encrypt-with-iv should not return the same message that went in"
             (spritz/encrypt-with-iv [65 66 67] [1 2 3] [68 69 70]) =not=> [68 69 70])

       (fact "encrypt-with-iv should not return the same ciphertext as encrypt (without IV)"
             (spritz/encrypt-with-iv [65 66 67]
                                     [1 2 3]
                                     [68 69 70]) =not=> (spritz/encrypt [65 66 67] [68 69 70]))

       (fact "encrypt-with-iv should return the same result as the JavaScript implementation"
             (spritz/encrypt-with-iv [66 67 68]
                                     [1 2 3]
                                     [1 2 3 4 5 6 7]) => [30 186  207 41 27 253 188])

       (fact "decrypt-with-iv(K, IV, encrypt-with-iv(K, IV, M) should equal the original M"
             (->> [1 2 3 4 5 6 7]
                  (spritz/encrypt-with-iv [1 2 3] [66 67 68])
                  (spritz/decrypt-with-iv [1 2 3] [66 67 68])) => [1 2 3 4 5 6 7])

       (fact "decrypt-with-iv(K, IV, encrypt(K, M) should not equal the original M"
             (->> [1 2 3 4 5 6 7]
                  (spritz/encrypt [66 67 68])
                  (spritz/decrypt-with-iv [1 2 3] [66 67 70])) =not=> [1 2 3 4 5 6 7])

       (fact "decrypt-with-iv should return the same result as the JavaScript implementation"
             (spritz/decrypt-with-iv [66 67 68]
                                     [1 2 3]
                                     [30 186  207 41 27 253 188]) => [1 2 3 4 5 6 7]))
