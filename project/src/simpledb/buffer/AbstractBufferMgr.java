package simpledb.buffer;

/**
 * CS4432-Project1: Abstract class containing methods shared among buffer manager implementations
 * @param <BufferType> Buffer or subclass of Buffer
 */
public abstract class AbstractBufferMgr<BufferType extends Buffer> implements IBufferMgr<BufferType> {

   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    * @param bufferpool array of buffers
    */
   public synchronized void flushAll(int txnum, BufferType[] bufferpool) {
      for (BufferType buff : bufferpool)
         if (buff.isModifiedBy(txnum))
            buff.flush();
   }
}
