(ns lumanu.re-frame.observers.core-test
  (:require
   [cljs.test :as test :refer-macros [is deftest async]]
   [lumanu.re-frame.observers :as obs]
   [reagent.core :as r]
   [reagent.ratom :as ratom ]
   [re-frame.subs :as subs]
   [re-frame.core :as re-frame]
   [re-frame.db :as db]))

(test/use-fixtures :each {:before (fn [] (subs/clear-all-handlers!))})

(def tests-done (atom {}))

(defn with-mounted-component [comp f]
  (let [div (.createElement js/document "div")]
    (try
      (let [c (r/render comp div)]
        (f c div))
      (finally
       (r/unmount-component-at-node div)
       (r/flush)))))

(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))

;; Test basic subscription
(deftest test-reg-sub
  (re-frame/reg-sub-raw
    :test-sub
    (fn [db [_]] (ratom/reaction (deref db))))

  (let [test-sub (subs/subscribe [:test-sub])]
    (is (= @db/app-db @test-sub))
    (reset! db/app-db 1)
    (is (= 1 @test-sub))))

(deftest with-observers-test
  (let [ran-comp      (r/atom 0)
        ran-observer  (r/atom 0)
        really-simple (fn []
                        (swap! ran-comp inc)
                        [:div "div in really-simple"])]
    (with-mounted-component [really-simple nil nil]
                            (fn [c div]
                              (swap! ran-comp inc)
                              (is (found-in #"div in really-simple" div))
                              (r/flush)
                              (is (= 2 @ran-comp))
                              (r/flush)
                              (is (= 2 @ran-comp))))
    (is (= 2 @ran-comp))))
