/** Daniel Fadlon 205984958 **/

/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

    private T[] buffer;
    private int producers;
    private int outCount;
    private int inCount;
    private boolean start; // start to produce or not

    /**
     * Constructor. Allocates a buffer (an array) with the given capacity and
     * resets pointers and counters.
     * @param capacity Buffer capacity
     */
    @SuppressWarnings("unchecked")
    public SynchronizedQueue(int capacity) {
        this.buffer = (T[])(new Object[capacity]);
        this.producers = 0;
        this.inCount = 0;
        this.outCount = 0;
        this.start = false;
    }

    /**
     * Dequeues the first item from the queue and returns it.
     * If the queue is empty but producers are still registered to this queue,
     * this method blocks until some item is available.
     * If the queue is empty and no more items are planned to be added to this
     * queue (because no producers are registered), this method returns null.
     *
     * @return The first item, or null if there are no more items
     * @see #registerProducer()
     * @see #unregisterProducer()
     */
    public synchronized T dequeue() {

        while (getSize() <= 0) { // Queue is empty
            if (!start || producers != 0){ // before producer is created OR still produce
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{ // finish to produce
                notifyAll();
                return null;
            }
        }

        T temp = buffer[outCount % getCapacity()];
        outCount++;
        notifyAll();

        return  temp;
    }

    /**
     * Enqueues an item to the end of this queue. If the queue is full, this
     * method blocks until some space becomes available.
     *
     * @param item Item to enqueue
     */
    public synchronized void enqueue(T item) {

        while(getSize() >= getCapacity()){ // Queue is full
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        buffer[inCount % getCapacity()] = item;
        inCount ++;
        notifyAll();
    }

    /**
     * Returns the capacity of this queue
     * @return queue capacity
     */
    public int getCapacity() {
        return buffer.length;
    }

    /**
     * Returns the current size of the queue (number of elements in it)
     * @return queue size
     */
    public int getSize() {
        return inCount - outCount;
    }

    /**
     * Registers a producer to this queue. This method actually increases the
     * internal producers counter of this queue by 1. This counter is used to
     * determine whether the queue is still active and to avoid blocking of
     * consumer threads that try to dequeue elements from an empty queue, when
     * no producer is expected to add any more items.
     * Every producer of this queue must call this method before starting to
     * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
     * finishes to enqueue all items.
     *
     * @see #dequeue()
     * @see #unregisterProducer()
     */
    public synchronized void registerProducer() {
        //  critical section
        this.producers++;
        start = true;
    }

    /**
     * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
     *
     * @see #dequeue()
     * @see #registerProducer()
     */
    public synchronized void unregisterProducer() {
        // critical section
        notifyAll();
        this.producers--;
    }

    /**
     * Give the number of objects that dequeue from the synchronizedQueue
     *
     * @return outCount
     */
    public int getOutCount(){

        return outCount;
    }

    /**
     * Give the number of objects that enqueue to the synchronizedQueue
     * @return inCount
     */
    public int getInCount() {

        return inCount;
    }


    /**
     * print the queue elements
     */
    public synchronized void printQueue(){
        for(int i=0; i<this.getSize(); ++i){
            int idx = (i + outCount) % getCapacity();
            System.out.println(buffer[idx]);
        }
    }

}
