(defproject lumanu.re-frame/observers "0.0.1-SNAPSHOT"
  :description  "A re-frame observer pattern for side-effects only subscriber."
  :url          "https://github.com/tonysherbondy/re-frame-observers.git"
  :license      {:name "MIT"}
  :dependencies [[org.clojure/clojure        "1.8.0"  :scope "provided"]
                 [org.clojure/clojurescript  "1.9.89" :scope "provided"]
                 [re-frame                   "0.10.5" :scope "provided"]]

  :profiles {:debug {:debug true}
             :dev   {:dependencies [[karma-reporter     "3.1.0"]
                                    [binaryage/devtools "0.9.10"]]
                     :plugins      [[lein-ancient       "0.6.15"]
                                    [lein-cljsbuild     "1.1.7"]
                                    [lein-npm           "0.6.2"]
                                    [lein-shell         "0.5.0"]]}}

  :clean-targets  [:target-path "run/compiled"]

  :resource-paths ["run/resources"]
  :jvm-opts       ["-Xmx1g" "-XX:+UseConcMarkSweepGC"]
  :source-paths   ["src"]
  :test-paths     ["test"]

  :shell          {:commands {"open" {:windows ["cmd" "/c" "start"]
                                      :macosx  "open"
                                      :linux   "xdg-open"}}}

  :deploy-repositories [["releases"  {:sign-releases false :url "https://clojars.org/repo"}]
                        ["snapshots" {:sign-releases false :url "https://clojars.org/repo"}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :npm {:devDependencies [[karma                 "1.0.0"]
                          [karma-cljs-test       "0.1.0"]
                          [karma-chrome-launcher "0.2.0"]
                          [karma-junit-reporter  "0.3.8"]]}

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["test" "src"]
                        :compiler     {:preloads             [devtools.preload]
                                       :external-config      {:devtools/config {:features-to-install [:formatters :hints]}}
                                       :output-to     "run/compiled/browser/test.js"
                                       :source-map    true
                                       :output-dir    "run/compiled/browser/test"
                                       :optimizations :none
                                       :source-map-timestamp true
                                       :pretty-print  true}}
                       {:id           "karma"
                        :source-paths ["test" "src"]
                        :compiler     {:output-to     "run/compiled/karma/test.js"
                                       :source-map    "run/compiled/karma/test.js.map"
                                       :output-dir    "run/compiled/karma/test"
                                       :optimizations :whitespace
                                       :main          "re-frame-observers.test_runner"
                                       :pretty-print  true}}]}

  :aliases {"test-once"   ["do" "clean," "cljsbuild" "once" "test," "shell" "open" "test/test.html"]
            "test-auto"   ["do" "clean," "cljsbuild" "auto" "test,"]
            "karma-once"  ["do" "clean," "cljsbuild" "once" "karma,"]})
