(ns GMP.tests
  (:use clojure.contrib.test-is))
  
(import GMP)
(import java.util.Random)
(import java.math.BigInteger)

(def a (new GMP "23"))
(def b (new GMP "100"))
(def res (new GMP))

(deftest test-plus
     (.add a b res) 
     (is (= res (new GMP "123")))
     (.add b a res) 
     (is (= res (new GMP "123")))
     (.add a a res) 
     (is (= res (new GMP "46")))
     (.add b b res) 
     (is (= res (new GMP "200")))
)

(deftest test-minus
  (.subtract a b res)
  (is (= res (new GMP -77)))
)

(deftest test-mul
  (.multiply a b res)
  (is (= res 2300))
)

(deftest test-div
     (.divide a b res) 
     (is (= res 0))
     (.divide b a res) 
     (is (= 4 res))
)

(deftest test-rem
  (.remainder a b res)
  (is (= 23 res))
  (.remainder b a res)
  (is (= 8 res))
)

(deftest test-mod
  (.mod a a res)
  (is (= 0 res))

  (.mod b a res)
  (is (= 8 res))
)

(deftest test-div-rem
  (def q (new GMP))
  (def r (new GMP))
  (.quotientAndRemainder b a q r)
  (are (= _1 _2)
       q (new GMP "4") 
       r (new GMP "8"))
)

(deftest test-pow
  (.pow a 0 res)
  (is (= 1 res))
  (.pow a 2 res)
  (def c (new GMP))
  (.multiply a a c)
  (is (= c res))
)

(deftest test-mod
  (.mod a a res)
  (is (= 0 res))
  (.mod b a res)
  (is (= 8 res))
)

(deftest test-mod-inverse
  (.modInverse a b res)
  (is (= 87 res))
  (.mod b b res)
  (is (= 0 res))
)

(deftest test-mod-pow
  (.modPow (new GMP "12") a a res)
  (is (= 12 res))
  
  (.modPow (new GMP "112") b a res)
  (is (= 3 res))

  (.modPow (new GMP "37") b a res)
  (is (= 9 res))
)
 
(deftest test-gcd
  (.gcd (new GMP "12") a res)
  (is (= 1 res))
  
  (.gcd (new GMP -300) b res)
  (is (= 100 res))
  
  (.gcd (new GMP 100) b res)
  (is (= 100 res))
)

(deftest test-test-primality
  (is (= 0 (.testPrimality (GMP. 25) 22)))
  (is (< 0 (.testPrimality a 22)))
  (is (< 0 (.testPrimality (GMP. "1166727135273561043807935164711") 22)))
) 

;; (deftest test-compare-to
;; (are (= _1 _2)
;;      -1 (.compareTo a b) 
;;      -1 (.compareTo (.negate b ) a)))


(deftest test-shift-left
  (.shiftLeft a 2 res)
  (is (= 92 res))

  (.shiftLeft a 0 res)
  (is (= a res))
)

(deftest test-shift-right
  (.shiftLeft b 2 res)
  (is (= 400 res))

  (.shiftLeft a 0 res)
  (is (= a res))
)

(deftest test-to-string
(are (= _1 _2)
     "23" (.toString a)
     "1100100" (.toString b 2 ))
)

(deftest test-int-value
(are (= _1 _2)
     23 (.intValue a)
     100 (.intValue b ))
)

(deftest test-long-value
(are (= _1 _2)
     4294967297 (.longValue (new GMP "4294967297"))
     100 (.longValue b ))
)

(deftest test-equals
(are (= _1 _2)
     false (.equals b (new GMP "4294967297"))
     true (.equals a a)
     false (.equals 23 a)))

(deftest test-double-value
(are (= _1 _2)
     42949.0 (.doubleValue (new GMP "42949"))
     100.0 (.doubleValue b )))

(deftest test-float-value
(are (= _1 _2)
     42949.0 (.floatValue (new GMP "42949"))
     100.0 (.floatValue b )))

(deftest test-abs
  (.abs a res)
  (is (= 23 res))
  
  (.abs (new GMP -231) res)
  (is (= 231 res))
)

(deftest test-negate
  (.negate a res)
  (is (= -23 res))
  
  (.negate (new GMP -231) res)
  (is (= 231 res))
)

(deftest test-to-bit-length
(are (= _1 _2)
     5 (.bitLength a)
     8 (.bitLength (new GMP "-129")))
)

(deftest test-to-byte-array
  (def c (new BigInteger "23"))
  (def d (new BigInteger "100"))
  (def r (make-array Byte/TYPE 1))
  (.toByteArray a r)
  (are (= _1 _2)
       (last r) (first (.toByteArray c)))
)

(deftest test-and
  (.and a (new GMP "0") res)
  (is (= 0 res))
  
  (.and b b res)
  (is (= 100 res))
)

(deftest test-or
  (.or a (new GMP "0") res)
  (is (= 23 res))
  
  (.or a b res)
  (is (= 119 res))
)

(deftest test-xor
  (.xor a (new GMP "0") res)
  (is (= 23 res))
  
  (.xor a b res)
  (is (= 115 res))
)

(deftest test-not
  (.not a  res)
  (is (= -24 res))
  
  (.not b res)
  (is (= -101 res))
)

(deftest test-and-not
  (.andNot a b res)
  (is (= 19 res))
  
  (.andNot b a res)
  (is (= 96 res))
)

(deftest test-set-bit
   ; "1100100"
  (.setBit b 2 true res)
  (is (= 100 res))
  
  (.setBit b 0 true res)
  (is (= 101 res))
)

(deftest test-test-bit
     ; "1100100"

  true (.testBit b 2) 
  false (.testBit b 0)
)

(deftest test-flip-bit
     ; "1100100"
  (.flipBit b 2 res)
  (is (= 96 res))

  (.flipBit b 4 res)
  (is (= 116 res)) 
)

(deftest test-get-lowest-set-bit
     ; "1100100"
  (is (= 7 (.lowestSetBit (new GMP "128"))))
  (is (= 0 (.lowestSetBit (new GMP "129"))))
)

(deftest test-bit-count
     ; "1100100"
  (is (= 1 (.bitCount (new GMP "128"))))
  (is (= 7 (.bitCount (new GMP -127))))
)

;; (deftest test-hashcode )

;; run the tests
(run-tests 'GMP.tests)
  