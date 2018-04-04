package simpledb.buffer;

import simpledb.file.*;

import java.util.ArrayList;
import java.util.Arrays;

import static simpledb.buffer.ReplacementPolicy.DEFAULT;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr implements Observer {
   private Buffer[] bufferpool;
   private ArrayList<Observable> observableList; // CS4432-Project1: List of observable buffers
   private ReplacementPolicy policy; // CS4432-Project1: Replacement policy enum
   
   /**
    * CS4432-Project1:
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    * @param replacementPolicy replacement policy to use
    */
   BasicBufferMgr(int numbuffs, String replacementPolicy) {
      if (replacementPolicy == null || replacementPolicy.isEmpty()) {
         policy = DEFAULT;
      } else {
         policy = Arrays.stream(ReplacementPolicy.values()).filter(p -> p.name().equalsIgnoreCase(replacementPolicy))
               .findFirst().orElse(DEFAULT);
      }

      bufferpool = new Buffer[numbuffs];
      for (int i=0; i<numbuffs; i++) {
         Buffer buff = new Buffer(this);
         bufferpool[i] = buff;
         observableList.add(buff);
      }

      policy.getStrategy().initStrategyFields(numbuffs, bufferpool);
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
            buff.flush();
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      return policy.getStrategy().pin(blk, buff);
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   // CS4432-Project1: Access changed from package-private to public to enforce method with interface
   public synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      return policy.getStrategy().pinNew(filename, fmtr);
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   // CS4432-Project1: Access changed from package-private to public to enforce method with interface
   public synchronized void unpin(Buffer buff) {
      policy.getStrategy().unpin(buff);
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
       return policy.getStrategy().available();
   }

   @Override
   public void update(Buffer buff) {
      policy.getStrategy().update(buff);
   }
   
   private Buffer findExistingBuffer(Block blk) {
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
   }

   @Override
   public String toString() {
      return "BasicBufferMgr{" +
            "bufferpool=" + Arrays.toString(bufferpool) +
            ", policy=" + policy.getDisplayName() +
            '}';
   }
}
