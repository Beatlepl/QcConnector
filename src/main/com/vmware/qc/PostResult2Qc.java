/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maintains a queue through which test results are added.
 * It spawns a separate thread to monitor the queue to receive the test results and post them into QC.
 * It is a singleton class.
 */
public class PostResult2Qc
{
   private final BlockingQueue<TestRunInfo> queue;
   private final PostResult2QcThreadExecutor postResult2QcThreadExecutor;
   private static PostResult2Qc postResult2Qc = new PostResult2Qc();
   private final static Logger log = LoggerFactory.getLogger(PostResult2Qc.class);

   /**
    * Private constructor.
    */
   private PostResult2Qc()
   {
      queue = new LinkedBlockingQueue<TestRunInfo>();
      postResult2QcThreadExecutor = this.new PostResult2QcThreadExecutor(queue);
   }

   /**
    * Returns a singleton instance of this class.
    */
   public static PostResult2Qc getInstance()
   {
      return postResult2Qc;
   }

   /**
    * Starts PostResult2Qc thread executor.
    */
   public void start()
   {
      log.info("Starting PostResult2Qc thread executor .....");
      postResult2QcThreadExecutor.start();
      //Add ShutdownHook which is invoked when application is shut down, either normally or abnormally.
      Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run()
         {
            //Stop PostResult2Qc thread gracefully.
            if (postResult2QcThreadExecutor.isAlive()) {
               log.warn("PostResult2Qc thread executor is still running and request to stop thread is initiated.");
               PostResult2Qc.getInstance().stop();
            }
         }
      });
   }

   /**
    * Adds a test run result into queue.
    *
    * @param testrunInfo - test run result.
    */
   public void addToQueue(TestRunInfo testrunInfo)
   {
      try {
         if (!postResult2QcThreadExecutor.stop) {
            queue.put(testrunInfo);
            log.info("Added test run result into queue :" + testrunInfo);
         } else {
            log.warn("Queue is closed, no more result is added to queue");
         }
      } catch(Exception ex) {
         log.error("Got an exception while putting testInstance into queue :", ex);
      }
   }

   /**
    * Stops PostResult2Qc thread.
    */
   public void stop()
   {
      log.info("Stopping PostResult2Qc thread executor.....");
      try {
         postResult2QcThreadExecutor.interrupt();
         postResult2QcThreadExecutor.join();
         log.info("PostResult2Qc thread executor stopped successfully");
      } catch (Exception ex) {
         log.error("Got an exception while stopping the thread executor :", ex);
      }
   }

   /**
    * Gets the POST status of test runs in QC.
    *
    * @return status map [key = testRun object | Value = Update Status (true / false )]
    */
   public Map<TestRunInfo, Boolean> getTestRunsPostedStatus()
   {
      return Collections.unmodifiableMap(postResult2QcThreadExecutor.testRunsPostedStatus);
   }

   /**
    * This thread executor monitors test run results that are added in the queue
    * and spawns a new thread for each test run result and delegates result
    * posting task to the new thread.
    */
   private class PostResult2QcThreadExecutor extends Thread
   {
      private final ExecutorService executorService;
      private final BlockingQueue<TestRunInfo> queue;
      private final Hashtable<TestRunInfo, Boolean> testRunsPostedStatus;
      private boolean stop = false;
      private final Logger log = LoggerFactory.getLogger(PostResult2QcThreadExecutor.class);

      public PostResult2QcThreadExecutor(final BlockingQueue<TestRunInfo> queue)
      {
         this.queue = queue;
         this.executorService = Executors.newCachedThreadPool();
         this.testRunsPostedStatus = new Hashtable<TestRunInfo, Boolean>();
      }

      /**
       * Takes test run result when added into queue and passes the result to a new thread.
       */
      public void run()
      {
         do {
            try {
               log.info("Waiting to receive test run data in queue....");
               TestRunInfo testrunInfo = queue.take();
               log.info("Taking test run from queue :" + testrunInfo);
               executorService.execute(new PostResult2QcThread(testrunInfo));
            } catch(InterruptedException ie) {
               stop = true;
            } catch(Exception ex) {
               log.error("Got an exception while processing test run result from queue :", ex);
            }
         } while(!(stop && queue.isEmpty()));

         executorService.shutdown();
         log.info("ThreadExecutor is shutdown");
         try {
            executorService.awaitTermination(30, TimeUnit.MINUTES);
            log.info("All result posting tasks have been completed successfully");
         } catch(Exception ex) {
            log.error("Got exception in awaitTermination :", ex);
         }
      }

      /**
       * This task thread processes a test run result and posts it into QC.
       */
      private class PostResult2QcThread implements Runnable
      {
         private final QcConnector qcConnector;
         private TestRunInfo testRunInfo;
         private final Logger log = LoggerFactory.getLogger(PostResult2QcThread.class);

         public PostResult2QcThread(TestRunInfo testRunInfo)
         {
            this.qcConnector = new QcConnector();
            this.testRunInfo = testRunInfo;
         }

         /**
          * Drives the over-all result posting task through other supporting functionalities.
          */
         public void run()
         {
            try {
               /*
                * Ignore test status if test already passed and current status is not PASS.
                */
               TestInstanceInfo testInstanceInfo = null;
               boolean canOverwriteStatus = true;
               if (!QcConstants.OVERWRITE_PASS_STATUS) {
                  testInstanceInfo = qcConnector.getTestInstance(testRunInfo.getTestInstanceId());
                  if (testInstanceInfo.getStatus() == QcTestStatus.PASSED
                           && !testRunInfo.getStatus().equals(
                                    QcTestStatus.PASSED)) {
                     canOverwriteStatus = false;
                  }
               }
               if (canOverwriteStatus) {
                  boolean success = post2Qc();
                  testRunsPostedStatus.put(testRunInfo, success);
               } else {
                  log.warn("Ignoring current test status as the test already passed & current status is not PASS:"
                           + testInstanceInfo.getTestName());
               }
            } catch (Exception ex) {
               log.error("Got an exception while processing test run result into QC :", ex);
            }
         }

         /**
          * Posts test run result and test log files to QC by invoking QCConnector
          *
          * @param testrunInfo - test run information.
          * @return true if posting to QC is successful, else returns false.
          */
         private boolean post2Qc()
         {
            boolean uploaded = true;
            List<String> logFilePaths = testRunInfo.getClientLogFilePaths();
            TestRunInfo newTestRun = null;
            try {
               newTestRun = qcConnector.postResult2Qc(testRunInfo);
               if (newTestRun != null) {
                  log.info("Successfully posted test run result into QC :" + newTestRun.getId());
               } else {
                  log.error("Failed to post test result into QC");
               }
            } catch(Exception ex) {
               log.error("Got an exception when posting test run result into QC",
                        ex);
            }
            try {
               if (newTestRun != null && logFilePaths != null && !logFilePaths.isEmpty()) {
                  for(String logFilePath : logFilePaths) {
                     if (qcConnector.uploadLogFile2Qc(newTestRun.getId(), logFilePath) != null) {
                        log.info("Test logs are successfully uploaded into QC :" + logFilePaths);
                     } else {
                        log.error("Failed to upload test logs into QC :" + logFilePaths);
                        uploaded = false;
                     }
                  }
               }
            } catch(Exception ex) {
               log.error("Got an exception when uploading testlog files into QC", ex);
               uploaded = false;
            }
            return newTestRun != null && uploaded;
         }

      }
   }

}
