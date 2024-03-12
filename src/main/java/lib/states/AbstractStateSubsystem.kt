package lib.states

import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.button.Trigger

abstract class AbstractStateSubsystem<This : AbstractStateSubsystem<This>>
    : SubsystemBase(), StateSubsystem<This> {
    private lateinit var mutState: State<This>

    private var stateMap: MutableMap<String, State<This>>

    // TODO: broker queue to manage event listening

    override fun receive(event: StateEvent){
        if(event is StateChangeEvent<*> && event.subsystem::javaClass == javaClass){
            @Suppress("UNCHECKED_CAST")
            val sce = event as StateChangeEvent<This>
            onStateChange(sce)
        } else {
            onEvent(event)
        }
    }

    /**
     * Called when this subsystem receives an event via the [StateEventBroker].
     *
     * This method is simply called by [receive] if no other, more specific
     * onEvent method is found to fit. For more specific methods, such as
     * [onStateChange], see their specification.
     *
     * Note that this method will *not* be called when a more specific
     * event method is found. This is to avoid doubly activating event
     * responses. If this method is required to receive more specific
     * types of events, onEvent should be called directly *once* through
     * either a direct call or a super call if the default implementation
     * simply redirects to this method.
     *
     * @param event The event that had happened. For already checked and
     *  type-casted event parameters, see their specific methods
     *
     * @author Matthew Clark
     *
     * @see onStateChange
     *
     * @since 2024-03-05
     */
    protected abstract fun onEvent(event: StateEvent)

    protected open fun onStateChange(event: StateChangeEvent<This>) {
        onEvent(event)
    }

    /**
     * Creates a new state out of a [lambda][java.util.function.Function] as its [State.next]
     * implementation.
     *
     * This function primarily exists to avoid boilerplate in kotlin code that
     * cannot easily convert a lambda to a [FunctionalInterface]
     *
     * @param name The name of the state as returned by [get]
     * @param nextFun The implementation of [State.next]
     *
     * @return An anonymous [State] object that uses the given function
     *
     * @author Matthew Clark
     *
     * @since 2024-03-05
     */
    protected fun makeState(name: String, nextFun: (This) -> State<This>): State<This> {
        val state = object : State<This> {
            override fun next(context: This): State<This> = nextFun(context)
        }

        stateMap[name] = state

        return state
    }

    protected fun put(name: String, state: State<This>){
        stateMap[name] = state
    }

    /**
     * Creates a [Trigger] that is `true` when this subsystem is in the given state,
     * and `false` otherwise.
     *
     * @param state The state for comparison
     *
     * @return A trigger for that state
     *
     * @since 2024-03-05
     */
    protected fun trigger(state: State<This>): Trigger = Trigger { this.state == state }

    // Prevent potential garbage collection
//    @Volatile
//    private var antiGC: Array<out State<This>>
    override val state: State<This>
        get() = mutState

    init {
        StateEventBroker.subscribe(This())
        stateMap = HashMap()
//        antiGC = states
    }
    
    protected fun setInitialState(state: State<This>){
        mutState = state
    }

    protected fun setInitialState(name: String){
        setInitialState(this[name])
    }

    protected fun updateState(){
        val newState = mutState.next(This())

        if(newState != mutState){
            mutState = newState
            StateEventBroker.put(
                StateChangeEvent(
                    This(),
                    mutState,
                    newState,
                )
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun This(): This = this as This

    /**
     * Returns a read-only [Set] of all key/value pairs in this map.
     */
    override val entries: Set<Map.Entry<String, State<This>>>
        get() = stateMap.entries

    /**
     * Returns a read-only [Set] of all keys in this map.
     */
    override val keys: Set<String>
        get() = stateMap.keys

    /**
     * Returns the number of key/value pairs in the map.
     */
    override val size: Int
        get() = stateMap.size

    /**
     * Returns a read-only [Collection] of all values in this map. Note that this collection may contain duplicate values.
     */
    override val values: Collection<State<This>>
        get() = stateMap.values

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean = stateMap.isEmpty()

    /**
     * Returns the value corresponding to the given [key], or [defaultValue] if such a key is not present in the map.
     *
     * @since JDK 1.8
     */
    override fun getOrDefault(key: String, defaultValue: State<This>): State<This> =
        stateMap.getOrDefault(key, defaultValue)

    /**
     * Returns the value corresponding to the given [key].
     *
     * @param key the name of the state to find
     *
     * @throws NoSuchElementException If no state of the given name is found
     */
    override fun get(key: String): State<This> {
        return stateMap[key] ?: throw NoSuchElementException("Could not find state with name \"$key\"")
    }

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    override fun containsValue(value: State<This>): Boolean {
        return stateMap.containsValue(value)
    }

    /**
     * Returns `true` if the map contains the specified [key].
     */
    override fun containsKey(key: String): Boolean {
        return stateMap.containsKey(key)
    }
}