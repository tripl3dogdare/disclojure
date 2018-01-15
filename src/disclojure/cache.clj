(ns disclojure.cache
  "A simple cache implementation for Disclojure.")

(defn create-cache
  "Creates a new cache."
  []
  (atom
    { :channel {}
      :message {}
      :guild {}
      :user {}
      :role {} }))

(defn insert
  "Creates or updates an entry in the cache, overwriting any existing entry if it exists.

    Parameters:

    - `cache` The cache to update.
    - `type` The type of object to insert.
    - `id` The ID of the object.
    - `value` The object to insert.

    Returns: The inserted value."
  [cache type id value]
  (swap! cache assoc type (assoc (@cache type) id value))
  value)

(defn retrieve
  "Retrieves an object from the cache by type and ID, or `nil` if it does not exist.

   Parameters:

   - `cache` The cache to retrieve from.
   - `type` The type of object to retrieve.
        - Valid values: `[:channel :message :guild :user :role]`
   - `id` The ID of the desired object.

   Returns: The retrieved value, or `nil` if the ID does not exist in the cache."
   [cache type id]
   (get (@cache type) id))

(defn through
  "Retrieves an object from the cache by type and ID, inserting `else` if it does not exist.

   Parameters:

   - `cache` The cache to retrieve from.
   - `type` The type of object to retrieve.
        - Valid values: `[:channel :message :guild :user :role]`
   - `id` The ID of the desired object.
   - `else` The value to return if the object doesn't exist in the cache.
      If a function is passed, it will be called and the return value will be used.

   Returns: The retrieved value, or `else` / `(else)` if the ID does not exist in the cache."
  [cache type id else]
  (or
    (retrieve cache type id)
    (insert cache type id
      (if (fn? else) (else) else))))
