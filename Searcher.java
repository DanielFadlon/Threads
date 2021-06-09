/** Daniel Fadlon 205984958 **/

import java.lang.String;
import java.io.File;
/**
 * A searcher thread.
 * Searches for files containing a given pattern and that end with a specific extension in all directories listed in a directory queue.
 */
public class Searcher implements Runnable {
    private int id;
    private String extension;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<File> resultsQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    /**
     * Constructor. Initializes the searcher thread.
     *
     * @param id              - unique id of the thread running the instance
     * @param extension       - wanted extension
     * @param directoryQueue  - A queue with directories to search in (as listed by the scouter)
     * @param resultsQueue    - A queue for files found (to be copied by a copier)
     * @param milestonesQueue - a synchronizedQueue to write milestones
     * @param isMilestones    - indicating whether or not the running thread should write to the milestonesQueue
     */
    public Searcher(int id, java.lang.String extension, SynchronizedQueue<File> directoryQueue, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Runs the searcher thread.
     * Thread will fetch a directory to search in from the directory queue, then search all files inside it(but will not recursively search subdirectories!).
     * Files that a contain the pattern and have the wanted extension are enqueued to the results queue.
     * This method begins by registering to the results queue as a producer and when finishes, it unregisters from it.
     * If the isMilestones was set in the constructor (and therefore the milstonesQueue was sent to it as well, it should write every "important" action to this queue.
     */
    @Override
    public void run() {
        resultsQueue.registerProducer();
        if (isMilestones) {
            milestonesQueue.registerProducer();
        }

        File dir;

        // while there are more producers or directories to search in AND the number of founded files <= max number of files to copy
         while((dir = directoryQueue.dequeue()) != null && resultsQueue.getInCount() <= DiskSearcher.MAX_NUM_FILES_TO_COPY){
             for (File file : dir.listFiles(File::isFile)) {

                    if(resultsQueue.getInCount() >= DiskSearcher.MAX_NUM_FILES_TO_COPY) break;

                    if (file.getName().endsWith(extension)) {
                        resultsQueue.enqueue(file);
                        if (isMilestones) {
                            milestonesQueue.enqueue("Searcher on thread id " + id + ": file named " + file.getName() + " was found");
                        }
                    }
                }
        }

        resultsQueue.unregisterProducer();
        if (isMilestones) {
            milestonesQueue.unregisterProducer();
        }
    }
}
