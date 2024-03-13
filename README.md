# State-Machine
Code that will hopefully make writing subsystems as State Machines easier

<!-- TOC -->
* [State-Machine](#state-machine)
* [Quick Start](#quick-start)
  * [Creating a State-Based Subsystem](#creating-a-state-based-subsystem)
<!-- TOC -->

# Quick Start

## Creating a State-Based Subsystem

A state-based subsystem really is just a fancy subsystem. That's why I made you
a special interface for it. Creating one of these is just as simple as creating
a subsystem as normal, except that the state-based version requires its own type
as a type-parameter. It will look like the following:

---

Java:

```java
import lib.states.*;

public final class ExampleSubsystem implements StateSubsystem<ExampleSubsystem> {
    /* ... */
}
```

---

Kotlin:

```kotlin
import lib.states.*

object ExampleSubsystem : StateSubsystem<ExampleSubsystem> {
    /* ... */
}
```

---

However, this interface still requires quite a bit of boilerplate
in order to actually set up the state machine. So I wrote that for
you as well. Instead of trying to implement the state subsystem
yourself, you can simply extend off of an abstract state subsystem:


---

Java:

```java
import lib.states.*;

public final class ExampleSubsystem implements AbstractStateSubsystem<ExampleSubsystem> {
    /* ... */
}
```

---

Kotlin:

```kotlin
import lib.states.*

object ExampleSubsystem : AbstractStateSubsystem<ExampleSubsystem>() {
    /* ... */
}
```

---

Now you have only a few things to worry about. Some basic information
and event handling mostly, but that's easy enough.

# Terminal States

Sometimes, state machines will have a *Terminal State*,
or a state that, once reached, signals the end of the
machine's processing.

There are two information methods relating to terminal states for
state subsystems, `isTerminal` and `isTerminable`.
