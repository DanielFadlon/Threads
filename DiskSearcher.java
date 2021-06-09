/** Daniel Fadlon 205984958 **/
import java.io.File;

public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int MAX_NUM_FILES_TO_COPY = 10;
    public static final int MAX_NUM_DIRECTORIES_TO_SCOUT = 10;

    public DiskSearcher(){}


    /**
     * Main method. Reads arguments from command line and starts the search.
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        long startTime = System.nanoTime();
        boolean isMilestones;
        int thread_id = 1;
        SynchronizedQueue<String> milestonesQueue;

        String extension = args[1];

        File root = new File(args[2]);

        if (Boolean.parseBoolean(args[0])) {
            isMilestones = true;
            /** max_size_milestones = first line + num_of directories(Scouter) + num_of files(searcher) +  max_files_to_copy(Copier)*/
            milestonesQueue = new SynchronizedQueue<String>(1 + MAX_NUM_DIRECTORIES_TO_SCOUT + (2 * MAX_NUM_FILES_TO_COPY) + 10 );
            milestonesQueue.enqueue("General, program has started the search");
        }else{
            isMilestones = false;
            milestonesQueue = null;
        }

        File destination = new File(args[3]);

        int num_searcher = Integer.parseInt(args[4]);
        int num_copiers = Integer.parseInt(args[5]);

        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);

        Thread scouter = new Thread(new Scouter(thread_id++, directoryQueue, root, milestonesQueue, isMilestones));
        scouter.start();

        Thread[] searchers = new Thread[num_searcher];
        for(int i = 0; i < num_searcher; ++i){
            searchers[i] = new Thread(new Searcher(thread_id++, extension, directoryQueue, resultsQueue, milestonesQueue, isMilestones));
            searchers[i].start();
        }


        Thread[] copiers = new Thread[num_copiers];
        for(int i = 0; i < num_copiers; ++i){
            copiers[i] = new Thread(new Copier(thread_id++, destination, resultsQueue, milestonesQueue, isMilestones));
            copiers[i].start();
        }

        scouter.join();

        for( int i=0; i<num_searcher; ++i){
            searchers[i].join();
        }

        for( int i=0; i<num_copiers; ++i){
            copiers[i].join();
        }

        if(isMilestones) {
            milestonesQueue.printQueue();
        }

        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + (double)timeElapsed / 1000000);
    }
}
