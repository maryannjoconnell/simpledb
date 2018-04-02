package simpledb.buffer;

import simpledb.file.Block;

/**
 * CS4432-Project1: Interface containing methods used by BufferMgr for specific buffer managers to implement
 * @param <BufferType> Buffer or subclass of Buffer
 */
interface IBufferMgr<BufferType extends Buffer> {
   void flushAll(int txnum);
   // CS4432-Project1: Overloaded method to support shared implementation in abstract class
   void flushAll(int txnum, BufferType[] bufferpool);
   BufferType pin(Block blk);
   BufferType pinNew(String filename, PageFormatter fmtr);
   void unpin(Buffer buff);
   int available();
}

