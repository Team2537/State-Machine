package lib.states

import edu.wpi.first.wpilibj2.command.button.Trigger

@FunctionalInterface
interface State<Sub : StateSubsystem<*>> {
    // TODO: context wrapper maybe?
    fun next(context: Sub): State<Sub>

    fun toTrigger(instance: Sub): Trigger {
        return Trigger { instance.state == this }
    }
}