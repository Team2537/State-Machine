package lib.states

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.function.Consumer

object StateEventBroker : Runnable {
    // puts last and takes first for FiLo
    private val deque = LinkedBlockingDeque<StateEvent>()

    private val threadPool = Executors.newCachedThreadPool()

    private val subscribers = ArrayList<StateEventListener>()

    private val thread: Thread = Thread(this, "State Event Broker Management Thread") // reference to thread

    init {
        thread.isDaemon = true
        thread.start()
    }

    @Synchronized
    fun subscribe(sub: StateEventListener){
        subscribers.add(sub)
    }

    @Synchronized
    fun subscribe(receiver: Consumer<StateEvent>){
        // For some reason, kotlin tries so very hard to completely defeat the entire purpose of functional interfaces
        subscribers.add({it: StateEvent -> receiver.accept(it) } as StateEventListener)
    }

    @Synchronized
    fun unsubscribe(sub: StateEventListener){
        subscribers.remove(sub)
    }

    override fun run() {
        while (true) {
            val event = deque.takeFirst()

            // Prevent un/subscribing from messing up event submission
            val copy: ArrayList<StateEventListener>
            synchronized(subscribers) {
                copy = ArrayList(subscribers)
            }
            for(listener in copy){
                threadPool.submit { listener.receive(event) }
            }

        }
    }

    @Synchronized
    fun put(event: StateEvent){
        try {
            deque.putLast(event)
        } catch (e: InterruptedException){
            e.printStackTrace()
        }
    }

    /**
     * Gives an event to the broker for distribution without waiting for space.
     *
     * Because the implementing deque has a capacity of [Integer.MAX_VALUE]
     */
    @Synchronized
    fun offer(event: StateEvent): Boolean {
        return deque.offerLast(event)
    }
}