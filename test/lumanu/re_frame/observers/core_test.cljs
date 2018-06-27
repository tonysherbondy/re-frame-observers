(ns lumanu.re-frame.observers.core-test
  (:require
   [cljs.test :as test :refer-macros [is deftest async]]
   [lumanu.re-frame.observers :as obs]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [re-frame.subs :as subs]
   [re-frame.core :as re-frame]
   [re-frame.db :as db]))

(test/use-fixtures :each {:before (fn [] (subs/clear-all-handlers!))})

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
  (let [ran-obs (atom 0)
        val-obs (atom "shouldn't see this")]
    (re-frame/reg-sub-raw
     :a-sub
     (fn [db [_]] (ratom/reaction (:a @db))))

    (re-frame/reg-sub
      :b-sub
      (fn [db [_]] (:b db)))

    (obs/reg-obs
     :fire-when-b-changes
     [[:b-sub]]
     (fn [b]
       (swap! ran-obs inc)
       (reset! val-obs b)))

    (let [ran-comp  (r/atom 0)
          comp-of-a (fn []
                      (let [a (subs/subscribe [:a-sub])]
                        (swap! ran-comp inc)
                        [:div (str "div in really-simple " @a)]))]

      (with-mounted-component [obs/with-observers
                               [comp-of-a nil nil]]
                              (fn [c div]

                                ;; We run the observer initially and the inital b is nil
                                (is (= 1 @ran-obs))
                                (is (nil? @val-obs))

                                (swap! ran-comp inc)
                                (is (found-in #"div in really-simple" div))
                                (r/flush)

                                ;; No b change so observer hasn't re-run
                                (is (= 1 @ran-obs))
                                (is (nil? @val-obs))

                                (is (= 2 @ran-comp))
                                (is (nil? (:a @db/app-db)))
                                (reset! db/app-db {:a 1 :b 1})
                                (is (= 1 (:a @db/app-db)))
                                (r/flush)

                                ;; b updated to 1, so observer ran again
                                (is (= 2 @ran-obs))
                                (is (= 1 @val-obs))
                                ;; a was also updated so component reran
                                (is (= 3 @ran-comp))

                                (swap! db/app-db update :b inc)
                                (r/flush)

                                ;; b updated to 2
                                (is (= 2 @(subs/subscribe [:b-sub])))
                                ;; So observer ran third time and is set to 2
                                (is (= 3 @ran-obs))
                                (is (= 2 @val-obs))
                                ;; But component did not run because a did not change
                                (is (= 3 @ran-comp)))))))
