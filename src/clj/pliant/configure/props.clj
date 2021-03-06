(ns pliant.configure.props
  (:use [pliant.configure.codec :only [decrypter decode]])
  (:use [pliant.configure.resources :only [with-resource with-resources]]))

(defn keyify
  "Provides keyword based keys for values in a properties map, without removing the original value."
  [props-map]
  (merge props-map 
         (into {} (map (fn [[k v]] [(if (keyword? k) k (keyword k)) v])  props-map))))

(defn props->map
  [stream]
  (let [props (java.util.Properties.)]
    (.load props stream)
    (keyify (into {} props))))

(defn slurp-props
  "Loads the key/values found in a properties file found on the classpath into a map"
  [path]
  (with-resource path props->map))


(defn slurp-all-props
  "Loads the key/values from all the instances of a properties file found on the classpath.  Uses the 
   ClassLoader.getResources"
  [path]
  (let [props (java.util.Properties.)]
    (with-resources path #(.load props %))
    (keyify (into {} props))))


(defn overlay-props
  "Loads key/value properties from resources on the classpath, allowing the overwriting of values from 
   earlier resources with the later ones."
  [& paths]
  (apply merge (doall (map slurp-props paths))))


(defn overlay-all-props
  "Loads key/value properties from all instances of resources on the classpath, allowing the overwriting 
   of values from earlier resources with the later ones."
  [& paths]
  (apply merge (doall (map slurp-all-props paths))))


(defn slurp-config
  "Loads and returns a configuration properties file, providing decryption services if requested."
  ([url]
    (slurp-config url nil {}))

  ([url passkey]
    (slurp-config url passkey {}))

  ([url passkey options]
    (let [props (slurp-props url)
          decr (if (nil? passkey) identity (decrypter passkey options))]
      (into {} (for [[k v] props] [k (decode v decr)])))))
