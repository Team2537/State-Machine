package lib.states

import edu.wpi.first.wpilibj2.command.button.Trigger
import java.util.function.Consumer
import java.util.function.Predicate

fun stateTrigger(predicate: Predicate<StateEvent>): Trigger {
    val triggerImpl = StateTriggerImpl(null)

    StateEventBroker.subscribe(triggerImpl)

    return Trigger { if(triggerImpl.event == null) false else predicate.test(triggerImpl.event!!) }
}

private class StateTriggerImpl(var event: StateEvent?) : StateEventListener {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    override fun receive(t: StateEvent) {
        event = t
    }

}