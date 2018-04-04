package simpledb.buffer;

import simpledb.server.SimpleDB;
import simpledb.file.*;

import java.time.Instant;

/**
 * An individual buffer.
 * A buffer wraps a page and stores information about its status,
 * such as the disk block associated with the page,
 * the number of times the block has been pinned,
 * whether the contents of the page have been modified,
 * and if so, the id of the modifying transaction and
 * the LSN of the corresponding log record.
 * @author Edward Sciore
 */
public class Buffer implements Observable {
   private Page contents = new Page();
   private Block blk = null;
   private int pins = 0;
   private int modifiedBy = -1;  // negative means not modified
   private int logSequenceNumber = -1; // negative means no corresponding log record
   // CS4432-Project1: metadata for lru replacement policy; null indicates unused field
   private Instant lastUsed = null;
   // CS4432-Project1: metadata for clock replacement policy; negative one indicates unused field
   private int refBit= -1;
   // CS4432-Project1: buffer manager responsible for maintaining data structures for replacement policies
   private Observer observer;
   /**
    * Creates a new buffer, wrapping a new 
    * {@link simpledb.file.Page page}.  
    * This constructor is called exclusively by the 
    * class {@link BasicBufferMgr}.   
    * It depends on  the 
    * {@link simpledb.log.LogMgr LogMgr} object 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * That object is created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param o Observer to notify of changes or accesses
    */
   // CS4432-Project1: Modified construct to accept observer
   public Buffer(Observer o) {
      observer = o;
   }
   
   /**
    * Returns the integer value at the specified offset of the
    * buffer's page.
    * If an integer was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset of the page
    * @return the integer value at that offset
    */
   public int getInt(int offset) {
      // CS4432-Project1: notify buffer manager about access
      notifyObserver();
      return contents.getInt(offset);
   }

   /**
    * Returns the string value at the specified offset of the
    * buffer's page.
    * If a string was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset of the page
    * @return the string value at that offset
    */
   public String getString(int offset) {
      // CS4432-Project1: notify buffer manager about access
      notifyObserver();
      return contents.getString(offset);
   }

   /**
    * Writes an integer to the specified offset of the
    * buffer's page.
    * This method assumes that the transaction has already
    * written an appropriate log record.
    * The buffer saves the id of the transaction
    * and the LSN of the log record.
    * A negative lsn value indicates that a log record
    * was not necessary.
    * @param offset the byte offset within the page
    * @param val the new integer value to be written
    * @param txnum the id of the transaction performing the modification
    * @param lsn the LSN of the corresponding log record
    */
   public void setInt(int offset, int val, int txnum, int lsn) {
      modifiedBy = txnum;
      if (lsn >= 0)
	      logSequenceNumber = lsn;
      contents.setInt(offset, val);
      // CS4432-Project1: notify buffer manager about modification
      notifyObserver();
   }

   /**
    * Writes a string to the specified offset of the
    * buffer's page.
    * This method assumes that the transaction has already
    * written an appropriate log record.
    * A negative lsn value indicates that a log record
    * was not necessary.
    * The buffer saves the id of the transaction
    * and the LSN of the log record.
    * @param offset the byte offset within the page
    * @param val the new string value to be written
    * @param txnum the id of the transaction performing the modification
    * @param lsn the LSN of the corresponding log record
    */
   public void setString(int offset, String val, int txnum, int lsn) {
      modifiedBy = txnum;
      if (lsn >= 0)
	      logSequenceNumber = lsn;
      contents.setString(offset, val);
      // CS4432-Project1: notify buffer manager about modification
      notifyObserver();
   }

   /**
    * Returns a reference to the disk block
    * that the buffer is pinned to.
    * @return a reference to a disk block
    */
   public Block block() {
      return blk;
   }

   /**
    * Writes the page to its disk block if the
    * page is dirty.
    * The method ensures that the corresponding log
    * record has been written to disk prior to writing
    * the page to disk.
    */
   void flush() {
      if (modifiedBy >= 0) {
         SimpleDB.logMgr().flush(logSequenceNumber);
         contents.write(blk);
         modifiedBy = -1;
      }
   }

   /**
    * Increases the buffer's getBufferToPin count.
    */
   void pin() {
      pins++;
      // CS4432-Project1: notify buffer manager about modification
      notifyObserver();
   }

   /**
    * Decreases the buffer's getBufferToPin count.
    */
   void unpin() {
      pins--;
      // CS4432-Project1: notify buffer manager about modification
      notifyObserver();
   }

   /**
    * Returns true if the buffer is currently pinned
    * (that is, if it has a nonzero getBufferToPin count).
    * @return true if the buffer is pinned
    */
   boolean isPinned() {
      return pins > 0;
   }

   /**
    * Returns true if the buffer is dirty
    * due to a modification by the specified transaction.
    * @param txnum the id of the transaction
    * @return true if the transaction modified the buffer
    */
   boolean isModifiedBy(int txnum) {
      return txnum == modifiedBy;
   }

   /**
    * Reads the contents of the specified block into
    * the buffer's page.
    * If the buffer was dirty, then the contents
    * of the previous page are first written to disk.
    * @param b a reference to the data block
    */
   void assignToBlock(Block b) {
      flush();
      blk = b;
      contents.read(blk);
      pins = 0;
   }

   /**
    * Initializes the buffer's page according to the specified formatter,
    * and appends the page to the specified file.
    * If the buffer was dirty, then the contents
    * of the previous page are first written to disk.
    * @param filename the name of the file
    * @param fmtr a page formatter, used to initialize the page
    */
   void assignToNew(String filename, PageFormatter fmtr) {
      flush();
      fmtr.format(contents);
      blk = contents.append(filename);
      pins = 0;
   }

   public Instant getLastUsed() {
      return lastUsed;
   }

   public void setLastUsed(Instant lastUsed) {
      this.lastUsed = lastUsed;
   }

   public int getRefBit() {
      return refBit;
   }

   public void setRefBit(int refBit) {
      this.refBit = refBit;
   }

   @Override
   public String toString() {
      String lastUsedStr = lastUsed != null ? ", lastUsed=" + getLastUsed().toString() : "";
      String refBitStr = refBit >= 0 ? ", refBit=" + refBit : "";
      return "Buffer{" +
            "id=" + this.hashCode() +
            "blk=" + blk +
            ", pins=" + pins +
            lastUsedStr +
            refBitStr +
            '}';
   }

   @Override
   public void notifyObserver() {
      observer.update(this);
   }
}