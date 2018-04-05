package simpledb.buffer;

import simpledb.file.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;
/**
 * CS4432-Project1: Manages the pinning and unpinning of buffers to blocks using the LRU replacement policy.
 */

class LruStrategy implements ReplacementStrategy {
   // CS4432-Project1: unpinned priority queue for lru replacement policy
   private PriorityQueue<Buffer> unpinnedBufferPool;

   /**
    * CS4432-Project1:  Get least recently used unpinned buffer and remove from queue
    * @return
    */
   public synchronized Buffer getBufferToPinNew() {
      return unpinnedBufferPool.poll();
   }

   public synchronized void incrementAvailable(Buffer buff) {
      // CS4432-Project1: Do nothing; number of unpinned buffers is tracked by priority queue
   }

   public synchronized void decrementAvailable(Buffer buff) {
      // CS4432-Project1: Do nothing; number of unpinned buffers is tracked by priority queue
   }

   /**
    * CS4432-Project1: Return size of unpinned buffer priority queue
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   public int available() {
      return unpinnedBufferPool.size();
   }

   /**
    * CS4432-Project1:
    * @param numbuffs
    * @param bufferpool
    */
   public void initStrategyFields(int numbuffs, Buffer[] bufferpool) {
      unpinnedBufferPool = new PriorityQueue<>(numbuffs, Comparator.comparing(Buffer::getLastUsed));
      for (int i=0; i<numbuffs; i++) {
         // CS4432-Project1: Initialize last used field with current time
         bufferpool[i].setLastUsed(Instant.now());
         // CS4432-Project1: Store reference to Buffer object in unpinnedBufferPool priority queue
         unpinnedBufferPool.add(bufferpool[i]);
      }
   }

   @Override
   public void update(Buffer buff) {
      // Update access time
      buff.setLastUsed(Instant.now());

      if (!buff.isPinned()) {
         if (unpinnedBufferPool.contains(buff)) {
            // update placement of buffer in priority queue
            unpinnedBufferPool.remove(buff);
            unpinnedBufferPool.add(buff);
         } else {
            unpinnedBufferPool.add(buff);
         }
      }
   }
   public String toString() { return ""; };
}
