/** Daniel Fadlon 205984958 **/

import java.io.File;
/**
 *  A scouter thread This thread lists all sub-directories from a given root path.
 *  Each sub-directory is enqueued to be searched for files by Searcher threads.
 *  */

public class Scouter implements Runnable{

    private int id;
    private SynchronizedQueue<File> directoryQueue;
    private File root;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

     /**
     * Construnctor. Initializes the scouter with a queue for the directories to be searched and a root directory to start from.
      *
     * @param id - the id of the thread running the instance
     * @param directoryQueue  - A queue for directories to be searched
     * @param root  - Root directory to start from
     * @param milestonesQueue - a synchronizedQueue to write milestones
     * @param isMilestones - indicating whether or not the running thread should write to the milestonesQueue
     */
    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Starts the scouter thread.
     * Lists directories under root directory and adds them to queue, then lists directories in the next level and enqueues them and so on.
     * This method begins by registering to the directory queue as a producer and when finishes, it unregisters from it.
     * If the isMilestones was set in the constructor(and therefore the milstonesQueue was sent to it as well, it should write every "important" action to this queue.
     */
    @Override
    public void run() {
        directoryQueue.registerProducer();
        if (isMilestones) {
            milestonesQueue.registerProducer();
        }
        // insert the sub directories
        list_directories(root);

        directoryQueue.unregisterProducer();
        if(isMilestones){
            milestonesQueue.unregisterProducer();
        }
    }

    /**
     * list all the directories from root and enqueue them to directoryQueue
     */
    private void list_directories(File current_root){
        if(directoryQueue.getInCount() >= DiskSearcher.MAX_NUM_DIRECTORIES_TO_SCOUT) return;

        directoryQueue.enqueue(current_root);
        if(isMilestones){
            milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named "+ current_root.getName() + " was scouted");
        }

        File[] dirs = current_root.listFiles(File::isDirectory);
        for(File dir : dirs){
            list_directories(dir);
        }
    }
}
