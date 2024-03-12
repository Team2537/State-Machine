package lib.states;

@FunctionalInterface
interface StateEventListener : AutoCloseable {
    fun receive(event: StateEvent)

    /**
     * Unsubscribes this listener from receiving events via the [StateEventBroker].
     *
     * If this listener was not subscribed, this method does nothing.
     *
     * Losing a reference to a listener while it is still subscribed will
     * pose a memory leak, as the broker will maintain the garbage reference.
     */
    override fun close() {
        StateEventBroker.unsubscribe(this)
    }
}
