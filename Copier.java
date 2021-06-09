/** Daniel Fadlon 205984958 **/

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A copier thread.
 * Reads files to copy from a queue and copies them to the given destination.
 */

public class Copier implements Runnable{

    public static final int COPY_BUFFER_SIZE = 4096;

    private int id;
    private File destination;
    private SynchronizedQueue<File> resultsQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    /**
     *Constructor. Initializes the worker with a destination directory and a queue of files to copy.
     *
     * @param id - the id of the thread running the specific instance,
     * @param destination - destination directory
     * @param resultsQueue -  A queue for files found (to be copied
     * @param milestonesQueue -  a synchronizedQueue to write milestones
     * @param isMilestones - indicating whether or not the running thread should write to the milestonesQueue
     */
    public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.destination = destination;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Copy all the files from the resultQueue to the given destination
     */

    @Override
    public void run() {
        if(isMilestones){
            milestonesQueue.registerProducer();
        }
        File file;

        // while there are more producers or files to copy AND the number of copied files <= max number of files to copy
        while((file = resultsQueue.dequeue()) != null && resultsQueue.getOutCount() <= DiskSearcher.MAX_NUM_FILES_TO_COPY){
            String file_name = file.getName();
            File current_destination = new File(destination, file.getName());
                try {
                    Files.copy(file.toPath(), current_destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (isMilestones) {
                        milestonesQueue.enqueue("Copier from thread id " + id + ": file named " + file_name + " was copied");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if(isMilestones){
            milestonesQueue.unregisterProducer();
        }
    }
}
