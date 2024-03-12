package lib.states

const val delimiter: String = "."

open class StateEvent(val subsystem: StateSubsystem<*>) {

    private var _isResponse: Boolean = false;
    val isResponse: Boolean
        get() = _isResponse
    fun respond(event: StateEvent){
        event._isResponse = true
        subsystem.receive(event)
    }
}

class StateChangeEvent<Sub : StateSubsystem<*>>(
    subsystem: Sub,
    val toState: State<Sub>,
    val fromState: State<Sub>
) : StateEvent(subsystem) {

}
