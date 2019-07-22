package ejektaflex.kotdot.generator.json.reg

abstract class SimpleRegistry<K : Any, V : Any>(protected val delegate: MutableMap<K, V> = HashMap()) :
    Map<K, V> by delegate {

    fun insert(key: K, item: V): K {
        delegate[key] = item
        return key
    }

    fun restore(items: Map<K, V>) {
        for (key in delegate.keys) {
            delegate.remove(key)
        }
        for (item in items) {
            delegate[item.key] = item.value
        }
    }


}