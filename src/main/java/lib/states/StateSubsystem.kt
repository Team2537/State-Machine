package lib.states

import edu.wpi.first.wpilibj2.command.Subsystem
import edu.wpi.first.wpilibj2.command.button.Trigger

interface StateSubsystem<This : StateSubsystem<This>> : Subsystem, StateEventListener, Map<String, State<This>> {
    /**
     * Gets the current state of the subsystem
     *
     * @return the current state of this subsystem
     */
    val state: State<This>

    /**
     * Checks if this subsystem is in its terminal state
     */
    val isTerminal: Boolean

    /**
     * Checks if this subsystem has a possible terminal state
     */
    val hasTerminal: Boolean

    /**
     * Gives this subsystem an event to process.
     *
     * When this method is called by the [StateEventBroker], it is delegated to a worker thread from a
     * [cachedThreadPool][java.util.concurrent.Executors.newCachedThreadPool].
     *
     * @param event The event to send
     *
     * @see StateEventBroker
     */
    override fun receive(event: StateEvent)

    val triggers: Set<Trigger>
}
