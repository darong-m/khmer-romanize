(ns khlp.spell)

(defn words [text] (re-seq #"[a-z]+" (.toLowerCase text)))

(defn train [features]
  (let [toGroupByCountFeature (fn [model feature]
                                (assoc model feature (inc (get model feature 1))))]
    (reduce toGroupByCountFeature {} features)))

(def *nwords* (train (words (slurp "resources/training-data/en/big.txt"))))

(defn edits1 [word]
  (let [alphabet "abcdefghijklmnopqrstuvwxyz", n (count word)]
    (distinct (concat
      (for [i (range n)] (str (subs word 0 i) (subs word (inc i))))
      (for [i (range (dec n))]
        (str (subs word 0 i) (nth word (inc i)) (nth word i) (subs word (+ 2 i))))
      (for [i (range n) c alphabet] (str (subs word 0 i) c (subs word (inc i))))
      (for [i (range (inc n)) c alphabet] (str (subs word 0 i) c (subs word i)))))))

(defn known [words nwords] (let [result (set (for [w words :when (nwords w)]  w))]
                             (if (empty? result)
                             nil
                             result)))

(defn known-edits2 [word nwords] (set (for [e1 (edits1 word) e2 (edits1 e1) :when (nwords e2)]  e2)))

(defn correct [word nwords]
  (let [candidates (or (known [word] nwords) (known (edits1 word) nwords)
                       (known-edits2 word nwords) [word])]
    (apply max-key #(get nwords % 1) candidates)))
